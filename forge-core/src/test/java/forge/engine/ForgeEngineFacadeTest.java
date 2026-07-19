/*
 * Forge: Play Magic: the Gathering.
 * Copyright (C) 2011  Forge Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package forge.engine;

import java.util.ArrayList;
import java.util.List;

import org.testng.Assert;
import org.testng.annotations.Test;

public class ForgeEngineFacadeTest {
    @Test
    public void publishesImmutableLifecycleSnapshots() {
        ForgeEngineFacade.beginInitialization(" iOS ");
        final ForgeEngineFacade.Status loading = ForgeEngineFacade.getStatus();

        ForgeEngineFacade.markReady(33290);
        final ForgeEngineFacade.Status ready = ForgeEngineFacade.getStatus();

        Assert.assertEquals(loading.getPhase(), ForgeEngineFacade.Phase.LOADING_CARD_DATABASE);
        Assert.assertEquals(loading.getPlatform(), "iOS");
        Assert.assertEquals(loading.getCardCount(), 0);
        Assert.assertEquals(ready.getPhase(), ForgeEngineFacade.Phase.READY);
        Assert.assertEquals(ready.getCardCount(), 33290);
        Assert.assertNotSame(loading, ready);
    }

    @Test
    public void preservesContextWhenPublishingFailure() {
        ForgeEngineFacade.beginInitialization("iOS");
        ForgeEngineFacade.markFailed("Database unavailable");

        final ForgeEngineFacade.Status failed = ForgeEngineFacade.getStatus();
        Assert.assertEquals(failed.getPhase(), ForgeEngineFacade.Phase.FAILED);
        Assert.assertEquals(failed.getPlatform(), "iOS");
        Assert.assertEquals(failed.getDetail(), "Database unavailable");
    }

    @Test
    public void publishesDefensiveDeckSummarySnapshots() {
        final List<ForgeEngineFacade.DeckSummary> source = new ArrayList<>();
        source.add(new ForgeEngineFacade.DeckSummary(
                "Commander||Alela", "Alela", "Commander", "", 99, 10, 1));

        ForgeEngineFacade.publishDecks(source);
        final List<ForgeEngineFacade.DeckSummary> snapshot = ForgeEngineFacade.getDecks();
        source.clear();

        Assert.assertEquals(snapshot.size(), 1);
        Assert.assertEquals(snapshot.get(0).getName(), "Alela");
        Assert.assertEquals(snapshot.get(0).getMainCount(), 99);
        Assert.assertEquals(snapshot.get(0).getSideboardCount(), 10);
        Assert.assertEquals(snapshot.get(0).getCommanderCount(), 1);
        Assert.expectThrows(UnsupportedOperationException.class, snapshot::clear);

        ForgeEngineFacade.publishDecks(null);
        Assert.assertTrue(ForgeEngineFacade.getDecks().isEmpty());
    }
}
