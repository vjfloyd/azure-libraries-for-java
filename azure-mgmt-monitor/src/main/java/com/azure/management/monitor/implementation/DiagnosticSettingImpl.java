/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.monitor.implementation;

import com.azure.management.monitor.LogSettings;
import com.azure.management.monitor.MetricSettings;
import com.azure.management.monitor.CategoryType;
import com.azure.management.monitor.DiagnosticSetting;
import com.azure.management.monitor.DiagnosticSettingsCategory;
import com.azure.management.monitor.RetentionPolicy;
import com.azure.management.resources.fluentcore.model.implementation.CreatableUpdatableImpl;
import org.joda.time.Period;
import rx.Observable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

/**
 * The Azure metric definition entries are of type DiagnosticSetting.
 */
class DiagnosticSettingImpl
        extends CreatableUpdatableImpl<DiagnosticSetting, DiagnosticSettingsResourceInner, DiagnosticSettingImpl>
        implements
            DiagnosticSetting,
            DiagnosticSetting.Definition,
            DiagnosticSetting.Update {
    public static final String DIAGNOSTIC_SETTINGS_URI = "/providers/microsoft.insights/diagnosticSettings/";

    private String resourceId;
    private TreeMap<String, MetricSettings> metricSet;
    private TreeMap<String, LogSettings> logSet;
    private final MonitorManager myManager;

    DiagnosticSettingImpl(String name,
                          DiagnosticSettingsResourceInner innerModel,
                          final MonitorManager monitorManager) {
        super(name, innerModel);
        this.myManager = monitorManager;
        initializeSets();
    }

    @Override
    public DiagnosticSettingImpl withResource(String resourceId) {
        this.resourceId = resourceId;
        return this;
    }

    @Override
    public DiagnosticSettingImpl withStorageAccount(String storageAccountId) {
        this.inner().withStorageAccountId(storageAccountId);
        return this;
    }

    @Override
    public DiagnosticSettingImpl withLogAnalytics(String workspaceId) {
        this.inner().withWorkspaceId(workspaceId);
        return this;
    }

    @Override
    public DiagnosticSettingImpl withoutLogAnalytics() {
        this.inner().withWorkspaceId(null);
        return this;
    }

    @Override
    public DiagnosticSettingImpl withoutStorageAccount() {
        this.inner().withStorageAccountId(null);
        return this;
    }

    @Override
    public DiagnosticSettingImpl withEventHub(String eventHubAuthorizationRuleId) {
        this.inner().withEventHubAuthorizationRuleId(eventHubAuthorizationRuleId);
        return this;
    }

    @Override
    public DiagnosticSettingImpl withEventHub(String eventHubAuthorizationRuleId, String eventHubName) {
        this.withEventHub(eventHubAuthorizationRuleId);
        this.inner().withEventHubName(eventHubName);
        return this;
    }

    @Override
    public DiagnosticSettingImpl withoutEventHub() {
        this.inner().withEventHubAuthorizationRuleId(null);
        this.inner().withEventHubName(null);
        return this;
    }

    @Override
    public DiagnosticSettingImpl withMetric(String category, Period timeGrain, int retentionDays) {
        MetricSettings nm = new MetricSettings();
        nm.withCategory(category);
        nm.withEnabled(true);
        nm.withRetentionPolicy(new RetentionPolicy());
        nm.retentionPolicy().withDays(retentionDays);
        if (retentionDays > 0) {
            nm.retentionPolicy().withEnabled(true);
        }
        nm.withTimeGrain(timeGrain);
        this.metricSet.put(category, nm);
        return this;
    }

    @Override
    public DiagnosticSettingImpl withLog(String category, int retentionDays) {
        LogSettings nl = new LogSettings();
        nl.withCategory(category);
        nl.withEnabled(true);
        nl.withRetentionPolicy(new RetentionPolicy());
        nl.retentionPolicy().withDays(retentionDays);
        if (retentionDays > 0) {
            nl.retentionPolicy().withEnabled(true);
        }
        this.logSet.put(category, nl);
        return this;
    }

    @Override
    public DiagnosticSettingImpl withLogsAndMetrics(List<DiagnosticSettingsCategory> categories, Period timeGrain, int retentionDays) {
        for (DiagnosticSettingsCategory dsc : categories) {
            if (dsc.type() == CategoryType.METRICS) {
                this.withMetric(dsc.name(), timeGrain, retentionDays);
            } else if (dsc.type() == CategoryType.LOGS) {
                this.withLog(dsc.name(), retentionDays);
            } else {
                throw new UnsupportedOperationException(dsc.type().toString() + " is unsupported.");
            }
        }
        return this;
    }

    @Override
    public DiagnosticSettingImpl withoutMetric(String category) {
        this.metricSet.remove(category);
        return this;
    }

    @Override
    public DiagnosticSettingImpl withoutLog(String category) {
        this.logSet.remove(category);
        return this;
    }

    @Override
    public DiagnosticSettingImpl withoutLogs() {
        this.logSet.clear();
        return this;
    }

    @Override
    public DiagnosticSettingImpl withoutMetrics() {
        this.metricSet.clear();
        return this;
    }

    @Override
    public String id() {
        return this.inner().id();
    }

    @Override
    public String resourceId() {
        return this.resourceId;
    }

    @Override
    public String storageAccountId() {
        return this.inner().storageAccountId();
    }

    @Override
    public String eventHubAuthorizationRuleId() {
        return this.inner().eventHubAuthorizationRuleId();
    }

    @Override
    public String eventHubName() {
        return this.inner().eventHubName();
    }

    @Override
    public List<MetricSettings> metrics() {
        if (this.inner().metrics() == null) {
            return null;
        }
        return Collections.unmodifiableList(this.inner().metrics());
    }

    @Override
    public List<LogSettings> logs() {
        if (this.inner().logs() == null) {
            return null;
        }
        return Collections.unmodifiableList(this.inner().logs());
    }

    @Override
    public String workspaceId() {
        return this.inner().workspaceId();
    }

    @Override
    public MonitorManager manager() {
        return this.myManager;
    }

    @Override
    public boolean isInCreateMode() {
        return this.inner().id() == null;
    }

    @Override
    public Observable<DiagnosticSetting> createResourceAsync() {
        this.inner().withLogs(new ArrayList<>(logSet.values()));
        this.inner().withMetrics(new ArrayList<>(metricSet.values()));
        return this.manager().inner().diagnosticSettings().createOrUpdateAsync(this.resourceId, this.name(), this.inner())
                .map(innerToFluentMap(this));
    }

    @Override
    protected Observable<DiagnosticSettingsResourceInner> getInnerAsync() {
        return this.manager().inner().diagnosticSettings().getAsync(this.resourceId, this.name());
    }

    @Override
    public void setInner(DiagnosticSettingsResourceInner inner) {
        super.setInner(inner);
        initializeSets();
        this.metricSet.clear();
        this.logSet.clear();
        if (!isInCreateMode()) {
            this.resourceId = inner.id().substring(0,
                    this.inner().id().length() - (DiagnosticSettingImpl.DIAGNOSTIC_SETTINGS_URI + this.inner().name()).length());
            for (MetricSettings ms : this.inner().metrics()) {
                this.metricSet.put(ms.category(), ms);
            }
            for (LogSettings ls : this.inner().logs()) {
                this.logSet.put(ls.category(), ls);
            }
        }
    }

    private void initializeSets() {
        if (this.metricSet == null) {
            this.metricSet = new TreeMap<>();
        }
        if (this.logSet == null) {
            this.logSet = new TreeMap<>();
        }
    }
}
