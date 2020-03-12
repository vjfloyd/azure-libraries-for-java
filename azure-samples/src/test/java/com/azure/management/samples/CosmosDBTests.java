/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.samples;

import com.azure.management.cosmosdb.samples.CreateCosmosDBWithEventualConsistency;
import com.azure.management.cosmosdb.samples.CreateCosmosDBWithIPRange;
import com.azure.management.cosmosdb.samples.CreateCosmosDBWithKindMongoDB;
import com.azure.management.cosmosdb.samples.CreateCosmosDBTableWithVirtualNetworkRule;
import com.azure.management.cosmosdb.samples.ManageHACosmosDB;
import com.microsoft.azure.management.samples.SamplesTestBase;
import org.junit.Assert;
import org.junit.Test;

public class CosmosDBTests extends SamplesTestBase {

    @Test
    public void testCreateCosmosDBWithEventualConsistency() {
        // Skip test in "playback" mode due to HTTP calls made outside of the management plane which can not be recorded at this time
        if (!isPlaybackMode()) {
            Assert.assertTrue(CreateCosmosDBWithEventualConsistency.runSample(azure, "clientId"));
        }
    }

    @Test
    public void testCreateCosmosDBWithIPRange() {
        Assert.assertTrue(CreateCosmosDBWithIPRange.runSample(azure, "clientId"));
    }

    @Test
    public void testCreateCosmosDBTableWithVirtualNetworkRule() {
        Assert.assertTrue(CreateCosmosDBTableWithVirtualNetworkRule.runSample(azure, "clientId"));
    }

    @Test
    public void testCreateCosmosDBWithKindMongoDB() {
        // Skip test in "playback" mode due to HTTP calls made outside of the management plane which can not be recorded at this time
        if (!isPlaybackMode()) {
            Assert.assertTrue(CreateCosmosDBWithKindMongoDB.runSample(azure, "clientId"));
        }
    }

    @Test
    public void testManageHACosmosDB() {
        // Skip test in "playback" mode due to HTTP calls made outside of the management plane which can not be recorded at this time
        if (!isPlaybackMode()) {
            Assert.assertTrue(ManageHACosmosDB.runSample(azure, "clientId"));
        }
    }
}
