/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.monitor;

import com.azure.management.monitor.implementation.MonitorManager;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.apigeneration.LangMethodDefinition;
import com.microsoft.azure.management.apigeneration.Method;
import com.azure.management.monitor.implementation.AutoscaleSettingResourceInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;

import java.util.List;
import java.util.Map;

/**
 * An immutable client-side representation of an Azure autoscale setting.
 */
@Fluent
public interface AutoscaleSetting extends
    GroupableResource<MonitorManager, AutoscaleSettingResourceInner>,
    Refreshable<AutoscaleSetting>,
    Updatable<AutoscaleSetting.Update> {

    /**
     * Get the resource identifier of the resource that the autoscale setting should be added to.
     *
     * @return the targetResourceUri value.
     */
    @LangMethodDefinition(AsType = LangMethodDefinition.LangMethodType.Property)
    String targetResourceId();

    /**
     * Gets the autoscale profiles in the current autoscale setting.
     *
     * @return autoscale profiles in the current autoscale setting, indexed by name
     */
    @LangMethodDefinition(AsType = LangMethodDefinition.LangMethodType.Property)
    Map<String, AutoscaleProfile> profiles();

    /**
     * Get the enabled flag. Specifies whether automatic scaling is enabled for the resource. The default value is 'true'.
     *
     * @return the enabled value.
     */
    @LangMethodDefinition(AsType = LangMethodDefinition.LangMethodType.Property)
    boolean autoscaleEnabled();

    /**
     * Get a value indicating whether to send email to subscription administrator.
     *
     * @return the sendToSubscriptionAdministrator value.
     */
    @LangMethodDefinition(AsType = LangMethodDefinition.LangMethodType.Property)
    boolean adminEmailNotificationEnabled();

    /**
     * Get a value indicating whether to send email to subscription co-administrators.
     *
     * @return the sendToSubscriptionCoAdministrators value
     */
    @LangMethodDefinition(AsType = LangMethodDefinition.LangMethodType.Property)
    boolean coAdminEmailNotificationEnabled();

    /**
     * Get the custom e-mails list. This value can be null or empty, in which case this attribute will be ignored.
     *
     * @return the customEmails value.
     */
    @LangMethodDefinition(AsType = LangMethodDefinition.LangMethodType.Property)
    List<String> customEmailsNotification();

    /**
     * Get the service address to receive the notification.
     *
     * @return the serviceUri value.
     */
    @LangMethodDefinition(AsType = LangMethodDefinition.LangMethodType.Property)
    String webhookNotification();

    /**
     * The entirety of an autoscale setting definition.
     */
    interface Definition extends
        DefinitionStages.Blank,
        DefinitionStages.WithGroup,
        DefinitionStages.WithCreate,
        DefinitionStages.DefineAutoscaleSettingResourceProfiles,
        DefinitionStages.WithAutoscaleSettingResourceTargetResourceUri,
        DefinitionStages.WithAutoscaleSettingResourceEnabled {
    }

    /**
     * Grouping of autoscale setting definition stages.
     */
    interface DefinitionStages {

        /**
         * The first stage of autoscale setting definition.
         */
        interface Blank extends DefinitionWithRegion<WithGroup> {
        }

        /**
         * The stage of the definition which selects resource group.
         */
        interface WithGroup extends GroupableResource.DefinitionStages.WithGroup<WithAutoscaleSettingResourceTargetResourceUri> {
        }

        /**
         * The stage of the definition which selects target resource.
         */
        interface WithAutoscaleSettingResourceTargetResourceUri {
            /**
             * Set the resource identifier of the resource that the autoscale setting should be added to.
             *
             * @param targetResourceId the targetResourceUri value to set
             * @return the next stage of the definition.
             */
            DefineAutoscaleSettingResourceProfiles withTargetResource(String targetResourceId);
        }

        /**
         * The stage of the definition which specifies autoscale profile.
         */
        interface DefineAutoscaleSettingResourceProfiles {
            /**
             * Starts the definition of automatic scaling profiles that specify different scaling parameters for different time periods. A maximum of 20 profiles can be specified.
             *
             * @param name name of the autoscale profile.
             * @return the next stage of the definition.
             */
            AutoscaleProfile.DefinitionStages.Blank defineAutoscaleProfile(String name);
        }

        /**
         * The stage of the definition which specifies autoscale notifications.
         */
        interface DefineAutoscaleSettingResourceNotifications {
            /**
             * Specifies that an email should be send to subscription administrator.
             *
             * @return the next stage of the definition.
             */
            @Method
            WithCreate withAdminEmailNotification();

            /**
             * Specifies that an email should be send to subscription co-administrator.
             *
             * @return the next stage of the definition.
             */
            @Method
            WithCreate withCoAdminEmailNotification();

            /**
             * Specifies that an email should be send to custom email addresses.
             *
             * @param customEmailAddresses list of the emails that should receive the notification.
             * @return the next stage of the definition.
             */
            WithCreate withCustomEmailsNotification(String... customEmailAddresses);

            /**
             * Set the service address to receive the notification.
             *
             * @param serviceUri the serviceUri value to set.
             * @return the next stage of the definition.
             */
            WithCreate withWebhookNotification(String serviceUri);
        }

        /**
         * The stage of the definition which specifies if the current autoscale setting should be disabled upon creation.
         */
        interface WithAutoscaleSettingResourceEnabled {
            /**
             * Set the current autoscale in the disabled state.
             *
             * @return the next stage of the definition.
             */
            @Method
            WithCreate withAutoscaleDisabled();
        }

        /**
         * The stage of the definition which allows autoscale setting creation.
         */
        interface WithCreate extends
            Creatable<AutoscaleSetting>,
            DefineAutoscaleSettingResourceProfiles,
            DefineAutoscaleSettingResourceNotifications,
            WithAutoscaleSettingResourceEnabled {
        }
    }

    /**
     * Grouping of autoscale setting update stages.
     */
    interface UpdateStages {
        /**
         * The stage of the update which adds or updates autoscale profiles in the current setting.
         */
        interface DefineAutoscaleSettingProfiles {
            /**
             * Starts definition of automatic scaling profiles that specify different scaling parameters for different time periods. A maximum of 20 profiles can be specified.
             *
             * @param name name of the profile.
             * @return the next stage of autoscale setting update.
             */
            AutoscaleProfile.UpdateDefinitionStages.Blank defineAutoscaleProfile(String name);

            /**
             * Starts the update of automatic scaling profiles.
             *
             * @param name name of the profile.
             * @return the next stage of autoscale setting update.
             */
            AutoscaleProfile.Update updateAutoscaleProfile(String name);

            /**
             * Removes the specified profile from the current setting.
             *
             * @param name name of the profile.
             * @return the next stage of autoscale setting update.
             */
            Update withoutAutoscaleProfile(String name);
        }

        /**
         * The stage of the update which updates current autoscale setting.
         */
        interface UpdateAutoscaleSettings {
            /**
             * Sets current autoscale setting to the enabled state.
             *
             * @return the next stage of autoscale setting update.
             */
            @Method
            Update withAutoscaleEnabled();

            /**
             * Sets current autoscale setting to the disabled state.
             *
             * @return the next stage of autoscale setting update.
             */
            @Method
            Update withAutoscaleDisabled();

            /**
             * Specifies that an email should be send to subscription administrator.
             *
             * @return the next stage of autoscale setting update.
             */
            @Method
            Update withAdminEmailNotification();

            /**
             * Specifies that an email should be send to subscription co-administrator.
             *
             * @return the next stage of autoscale setting update.
             */
            @Method
            Update withCoAdminEmailNotification();

            /**
             * Specifies that an email should be send to custom email addresses.
             *
             * @param customEmailAddresses list of the emails that should receive the notification.
             * @return the next stage of autoscale setting update.
             */
            Update withCustomEmailsNotification(String... customEmailAddresses);

            /**
             * Set the service address to receive the notification.
             *
             * @param serviceUri the serviceUri value to set.
             * @return the next stage of autoscale setting update.
             */
            Update withWebhookNotification(String serviceUri);

            /**
             * Removes email notification to subscription administrator.
             *
             * @return the next stage of autoscale setting update.
             */
            @Method
            Update withoutAdminEmailNotification();

            /**
             * Removes email notification to subscription co-administrator.
             *
             * @return the next stage of autoscale setting update.
             */
            @Method
            Update withoutCoAdminEmailNotification();

            /**
             * Removes email notification to custom email addresses.
             *
             * @return the next stage of autoscale setting update.
             */
            @Method
            Update withoutCustomEmailsNotification();

            /**
             * Removes service from autoscale notification.
             *
             * @return the next stage of autoscale setting update.
             */
            @Method
            Update withoutWebhookNotification();
        }
    }

    /**
     * Grouping of autoscale setting update stages.
     */
    interface Update extends
            Appliable<AutoscaleSetting>,
            Resource.UpdateWithTags<Update>,
            UpdateStages.DefineAutoscaleSettingProfiles,
            UpdateStages.UpdateAutoscaleSettings {
    }

}