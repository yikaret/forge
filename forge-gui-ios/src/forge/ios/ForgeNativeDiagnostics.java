/*
 * Forge: Play Magic: the Gathering.
 * Copyright (C) 2011  Forge Team
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package forge.ios;

import org.robovm.apple.foundation.NSObject;
import org.robovm.objc.ObjCRuntime;
import org.robovm.objc.annotation.Method;
import org.robovm.objc.annotation.NativeClass;
import org.robovm.rt.bro.annotation.Library;

/** RoboVM binding for the SwiftUI diagnostics presenter. */
@Library(Library.INTERNAL)
@NativeClass
final class ForgeNativeDiagnostics extends NSObject {
    static {
        ObjCRuntime.bind(ForgeNativeDiagnostics.class);
    }

    private ForgeNativeDiagnostics() {
    }

    @Method(selector = "presentWithVersion:phase:cardCount:platform:details:")
    static native void present(String version, String phase, String cardCount,
            String platform, String details);
}
