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

import forge.util.BuildInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Stable, UI-independent entry point for native clients of the Forge engine.
 *
 * The facade only publishes immutable snapshots. Platform UIs must not retain
 * mutable engine objects or reach into the game model across thread boundaries.
 */
public final class ForgeEngineFacade {
    public enum Phase {
        STARTING,
        LOADING_CARD_DATABASE,
        READY,
        FAILED
    }

    /** Immutable lifecycle data safe to copy across a platform bridge. */
    public static final class Status {
        private final Phase phase;
        private final String platform;
        private final String version;
        private final int cardCount;
        private final String detail;

        private Status(final Phase phase0, final String platform0, final String version0,
                final int cardCount0, final String detail0) {
            phase = phase0;
            platform = platform0;
            version = version0;
            cardCount = cardCount0;
            detail = detail0;
        }

        public Phase getPhase() {
            return phase;
        }

        public String getPlatform() {
            return platform;
        }

        public String getVersion() {
            return version;
        }

        public int getCardCount() {
            return cardCount;
        }

        public String getDetail() {
            return detail;
        }
    }

    /** Immutable deck metadata safe to copy across a platform bridge. */
    public static final class DeckSummary {
        private final String id;
        private final String name;
        private final String category;
        private final String path;
        private final int mainCount;
        private final int sideboardCount;
        private final int commanderCount;

        public DeckSummary(final String id0, final String name0, final String category0,
                final String path0, final int mainCount0, final int sideboardCount0,
                final int commanderCount0) {
            id = normalize(id0);
            name = normalize(name0);
            category = normalize(category0);
            path = normalizePath(path0);
            mainCount = Math.max(0, mainCount0);
            sideboardCount = Math.max(0, sideboardCount0);
            commanderCount = Math.max(0, commanderCount0);
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getCategory() {
            return category;
        }

        public String getPath() {
            return path;
        }

        public int getMainCount() {
            return mainCount;
        }

        public int getSideboardCount() {
            return sideboardCount;
        }

        public int getCommanderCount() {
            return commanderCount;
        }
    }

    /** Immutable result of importing a deck through a platform integration. */
    public static final class DeckImportResult {
        private final boolean success;
        private final String deckName;
        private final String category;
        private final String detail;
        private final boolean renamed;

        private DeckImportResult(final boolean success0, final String deckName0,
                final String category0, final String detail0, final boolean renamed0) {
            success = success0;
            deckName = normalize(deckName0);
            category = normalize(category0);
            detail = normalize(detail0);
            renamed = renamed0;
        }

        public static DeckImportResult imported(final String deckName,
                final String category, final boolean renamed) {
            return new DeckImportResult(true, deckName, category, "Deck imported", renamed);
        }

        public static DeckImportResult failed(final String detail) {
            return new DeckImportResult(false, "Unknown", "Unknown", detail, false);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getDeckName() {
            return deckName;
        }

        public String getCategory() {
            return category;
        }

        public String getDetail() {
            return detail;
        }

        public boolean isRenamed() {
            return renamed;
        }
    }

    private static volatile Status status = new Status(
            Phase.STARTING, "Unknown", BuildInfo.getVersionString(), 0, "Engine process created");
    private static volatile List<DeckSummary> decks = Collections.emptyList();

    private ForgeEngineFacade() {
    }

    public static Status getStatus() {
        return status;
    }

    public static List<DeckSummary> getDecks() {
        return decks;
    }

    public static void publishDecks(final Collection<DeckSummary> summaries) {
        final List<DeckSummary> snapshot = new ArrayList<>();
        if (summaries != null) {
            snapshot.addAll(summaries);
            snapshot.removeAll(Collections.singleton(null));
        }
        decks = Collections.unmodifiableList(snapshot);
    }

    public static void beginInitialization(final String platform) {
        status = new Status(Phase.LOADING_CARD_DATABASE, normalize(platform),
                BuildInfo.getVersionString(), 0, "Loading card database");
    }

    public static void markReady(final int cardCount) {
        final Status previous = status;
        status = new Status(Phase.READY, previous.platform, previous.version,
                Math.max(0, cardCount), "Engine ready");
    }

    public static void markFailed(final String detail) {
        final Status previous = status;
        status = new Status(Phase.FAILED, previous.platform, previous.version,
                previous.cardCount, normalize(detail));
    }

    private static String normalize(final String value) {
        return value == null || value.trim().isEmpty() ? "Unknown" : value.trim();
    }

    private static String normalizePath(final String value) {
        return value == null ? "" : value.trim();
    }
}
