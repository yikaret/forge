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

    private static volatile Status status = new Status(
            Phase.STARTING, "Unknown", BuildInfo.getVersionString(), 0, "Engine process created");

    private ForgeEngineFacade() {
    }

    public static Status getStatus() {
        return status;
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
}
