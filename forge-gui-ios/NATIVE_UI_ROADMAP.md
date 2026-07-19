# Native iOS direction

## Decision

Ship the existing Forge engine on iOS first, then replace user-facing screens
incrementally with SwiftUI. Do not rewrite the rules engine, AI, or card-script
corpus in Swift.

The current MobiVM build is already an ahead-of-time compiled iOS executable;
it does not download or embed a desktop JVM. Its screens are still rendered by
libGDX, however, so it is native code at runtime but not yet a native iOS user
interface.

The target architecture is:

```text
SwiftUI navigation and iOS integrations
                |
        Objective-C/C bridge
                |
       ForgeEngineFacade (Java)
                |
  Forge rules + AI + card scripts (Java)

Existing libGDX battlefield remains available as a UIKit child view while
individual screens are migrated.
```

## Implemented first slice

The first vertical slice now proves the complete bridge:

- `ForgeEngineFacade` publishes an immutable lifecycle snapshot from
  `forge-core`, with transition tests;
- mobile startup publishes loading and ready states plus the loaded card count;
- iOS Settings exposes a platform capability instead of importing iOS code;
- RoboVM calls an Objective-C-visible Swift presenter; and
- SwiftUI renders a native diagnostics/about sheet using the facade snapshot
  and real `UIDevice` / `NSProcessInfo` data.

The pipeline compiles the Swift source into device or universal simulator
static archives automatically. Gameplay and all existing navigation remain on
the working libGDX path.

This preserves Forge's mature game behavior and keeps upstream merges
possible. A clean-room Swift rewrite would have to reproduce thousands of
interacting rules, the AI, serialization, networking, and more than 33,000
card scripts before it reached feature parity.

## Delivery phases

### 1. Stabilize the existing iOS port

- Keep the iOS compatibility audit green after every upstream merge.
- Produce repeatable simulator and unsigned IPA artifacts in CI.
- Verify startup, a complete offline match, save/resume, audio, downloads,
  memory warnings, and device rotation on supported iPhone and iPad versions.
- Establish an app-owned bundle identifier, icons, display name, privacy
  declarations, signing, and crash-reporting policy.

This phase produces a usable beta without waiting for a UI rewrite.

### 2. Create a UI-independent engine facade

Add a small, versioned API that exposes immutable data instead of Forge UI
objects. Its first contract should cover:

- engine initialization and progress;
- deck listing, validation, import, and export;
- match configuration and creation;
- a serializable game snapshot;
- the legal actions for the current priority holder;
- submitting one selected action or choice; and
- a stream of state/progress/error events.

All engine work stays on one serial executor. The bridge publishes immutable
snapshots on the main thread, and Swift never retains mutable Java game
objects. This boundary is useful even if the current libGDX UI remains the
only client at first, because it can be covered by deterministic replay tests.

### 3. Add the native shell

Compile a small Swift static library as part of the iOS pipeline and expose its
entry points through an Objective-C-compatible shim. Host SwiftUI from the
existing UIKit application delegate so the Forge AOT runtime remains the app
entry point.

Migrate low-risk screens first:

1. launch/loading and error recovery;
2. settings, licenses, and diagnostics;
3. deck import/export and document-picker integration;
4. deck library and deck details;
5. match setup.

Use native facilities for document picking, share sheets, accessibility,
Dynamic Type, haptics, background downloads, and secure storage.

### 4. Decide how far to migrate gameplay

Keep the current libGDX battlefield until the engine facade and snapshot model
have survived real matches. It can be embedded behind the SwiftUI navigation
shell. Later, either retain it permanently or replace one battlefield surface
at a time with SwiftUI/Metal-backed views.

A full native battlefield is the most expensive UI phase because it must
handle targeting, priority, the stack, combat assignment, card zoom, modal
choices, animations, and multiplayer timing without changing game semantics.

## Release gates

The first public beta should require:

- a clean Java/MobiVM compatibility audit;
- a reproducible simulator build and unsigned IPA;
- automated facade/replay tests for representative matches;
- at least one complete on-device match on both an iPhone and an iPad;
- no launch watchdog termination or memory-pressure crash; and
- documented source/build availability for the GPL-3.0 distribution.

App Store distribution also needs a separate review of GPL obligations and of
the rights to the app name, card data, symbols, and artwork. Until that review
is complete, development builds should use an original name and icon and avoid
bundling third-party card artwork.
