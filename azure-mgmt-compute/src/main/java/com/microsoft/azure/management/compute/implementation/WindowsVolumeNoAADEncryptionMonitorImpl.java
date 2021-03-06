/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.compute.DiskInstanceView;
import com.microsoft.azure.management.compute.DiskVolumeEncryptionMonitor;
import com.microsoft.azure.management.compute.EncryptionStatus;
import com.microsoft.azure.management.compute.InstanceViewStatus;
import com.microsoft.azure.management.compute.InstanceViewTypes;
import com.microsoft.azure.management.compute.OperatingSystemTypes;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import rx.Observable;
import rx.functions.Func1;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * The implementation for DiskVolumeEncryptionStatus for Windows virtual machine.
 * This implementation monitor status of encrypt-decrypt through new NoAAD encryption extension.
 */
@LangDefinition
class WindowsVolumeNoAADEncryptionMonitorImpl implements DiskVolumeEncryptionMonitor {
    private final String rgName;
    private final String vmName;
    private final ComputeManager computeManager;
    private VirtualMachineInner virtualMachine;

    WindowsVolumeNoAADEncryptionMonitorImpl(String virtualMachineId, ComputeManager computeManager) {
        this.rgName = ResourceUtils.groupFromResourceId(virtualMachineId);
        this.vmName = ResourceUtils.nameFromResourceId(virtualMachineId);
        this.computeManager = computeManager;
    }

    @Override
    public OperatingSystemTypes osType() {
        return OperatingSystemTypes.WINDOWS;
    }

    @Override
    public String progressMessage() {
        return String.format("OSDisk: %s DataDisk: %s", osDiskStatus(), dataDiskStatus());
    }

    @Override
    public EncryptionStatus osDiskStatus() {
        if (virtualMachine.instanceView() == null || virtualMachine.instanceView().disks() == null) {
            return EncryptionStatus.UNKNOWN;
        }
        for (DiskInstanceView diskInstanceView : virtualMachine.instanceView().disks()) {
            // encryptionSettings will be set only for OSDisk
            if (diskInstanceView.encryptionSettings() != null) {
                for (InstanceViewStatus status : diskInstanceView.statuses()) {
                    EncryptionStatus encryptionStatus = encryptionStatusFromCode(status.code());
                    if (encryptionStatus != null) {
                        return encryptionStatus;
                    }
                }
                break;
            }
        }
        return EncryptionStatus.UNKNOWN;
    }

    @Override
    public EncryptionStatus dataDiskStatus() {
        if (virtualMachine.instanceView() == null || virtualMachine.instanceView().disks() == null) {
            return EncryptionStatus.UNKNOWN;
        }
        HashSet<EncryptionStatus> encryptStatuses = new HashSet<>();
        for (DiskInstanceView diskInstanceView : virtualMachine.instanceView().disks()) {
            // encryptionSettings will be null for data disks and non-null for os disk.
            if (diskInstanceView.encryptionSettings() != null) {
                continue;
            }
            for (InstanceViewStatus status : diskInstanceView.statuses()) {
                EncryptionStatus encryptionStatus = encryptionStatusFromCode(status.code());
                if (encryptionStatus != null) {
                    encryptStatuses.add(encryptionStatus);
                    break;
                }
            }
        }
        // Derive an overall encryption status for all data disks.
        // --------------

        if (encryptStatuses.isEmpty()) {
            // Zero disks or disks without encryption state status.
            return EncryptionStatus.UNKNOWN;
        } else if (encryptStatuses.size() == 1) {
            // Single item indicate all disks are of the same encryption state.
            //
            return encryptStatuses.iterator().next();
        }
        //
        // More than one encryptStatuses indicates multiple disks with different encryption states.
        // The precedence is UNKNOWN > NOT_MOUNTED > ENCRYPTION_INPROGRESS > VM_RESTART_PENDING
        if (encryptStatuses.contains(EncryptionStatus.UNKNOWN)) {
            return EncryptionStatus.UNKNOWN;
        } else if (encryptStatuses.contains(EncryptionStatus.NOT_MOUNTED)) {
            return EncryptionStatus.NOT_MOUNTED;
        } else if (encryptStatuses.contains(EncryptionStatus.ENCRYPTION_INPROGRESS)) {
            return EncryptionStatus.ENCRYPTION_INPROGRESS;
        } else if (encryptStatuses.contains(EncryptionStatus.VM_RESTART_PENDING)) {
            return EncryptionStatus.VM_RESTART_PENDING;
        } else {
            return EncryptionStatus.UNKNOWN;
        }
    }

    @Beta
    @Override
    public Map<String, InstanceViewStatus> diskInstanceViewEncryptionStatuses() {
        if (virtualMachine.instanceView() == null || virtualMachine.instanceView().disks() == null) {
            return new HashMap<String, InstanceViewStatus>();
        }
        //
        HashMap<String, InstanceViewStatus> div = new HashMap<String, InstanceViewStatus>();
        for (DiskInstanceView diskInstanceView : virtualMachine.instanceView().disks()) {
            for (InstanceViewStatus status : diskInstanceView.statuses()) {
                if (encryptionStatusFromCode(status.code()) != null) {
                    div.put(diskInstanceView.name(), status);
                    break;
                }
            }
        }
        return div;
    }

    @Override
    public DiskVolumeEncryptionMonitor refresh() {
        return refreshAsync().toBlocking().last();
    }

    @Override
    public Observable<DiskVolumeEncryptionMonitor> refreshAsync() {
        final WindowsVolumeNoAADEncryptionMonitorImpl self = this;
        // Refreshes the cached virtual machine and installed encryption extension
        //
        return retrieveVirtualMachineAsync()
                .flatMap(new Func1<VirtualMachineInner, Observable<DiskVolumeEncryptionMonitor>>() {
                    @Override
                    public Observable<DiskVolumeEncryptionMonitor> call(VirtualMachineInner virtualMachine) {
                        self.virtualMachine = virtualMachine;
                        return Observable.<DiskVolumeEncryptionMonitor>just(self);
                    }
                });
    }

    /**
     * Retrieve the virtual machine.
     * If the virtual machine does not exists then an error observable will be returned.
     *
     * @return the retrieved virtual machine
     */
    private Observable<VirtualMachineInner> retrieveVirtualMachineAsync() {
        return this.computeManager
                .inner()
                .virtualMachines()
                .getByResourceGroupAsync(rgName, vmName, InstanceViewTypes.INSTANCE_VIEW)
                .flatMap(new Func1<VirtualMachineInner, Observable<VirtualMachineInner>>() {
                    @Override
                    public Observable<VirtualMachineInner> call(VirtualMachineInner virtualMachine) {
                        if (virtualMachine == null) {
                            return Observable.error(new Exception(String.format("VM with name '%s' not found (resource group '%s')",
                                    vmName, rgName)));
                        }
                        return Observable.just(virtualMachine);
                    }
                });
    }

    /**
     * Given disk instance view status code, check whether it is encryption status code if yes map it to EncryptionStatus.
     *
     * @param code the encryption status code
     * @return mapped EncryptionStatus if given code is encryption status code, null otherwise.
     */
    private static EncryptionStatus encryptionStatusFromCode(String code) {
        if (code != null && code.toLowerCase().startsWith("encryptionstate")) {
            // e.g. "code": "EncryptionState/encrypted"
            //      "code": "EncryptionState/notEncrypted"
            String[] parts = code.split("/", 2);
            if (parts.length != 2) {
                return EncryptionStatus.UNKNOWN;
            } else {
                return EncryptionStatus.fromString(parts[1]);
            }
        }
        return null;
    }
}
