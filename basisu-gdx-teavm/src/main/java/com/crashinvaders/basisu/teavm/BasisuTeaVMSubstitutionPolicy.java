package com.crashinvaders.basisu.teavm;

import org.teavm.extension.spi.substitution.SimpleSubstitutionPolicy;
import org.teavm.extension.spi.substitution.SubstitutionSink;

import java.util.function.Predicate;

/**
 * Routes {@code com.crashinvaders.basisu.*} classes to their {@code emu.com.crashinvaders.basisu.*}
 * TeaVM-specific counterparts (see the sibling classes in this module) whenever such a counterpart
 * exists on the classpath - e.g. {@code BasisuWrapper}, which talks to the Emscripten/Wasm module
 * through TeaVM's JS interop instead of a JNI call. Classes with no such counterpart (enums,
 * exceptions, the rest of {@code basisu-gdx}) are left untouched.
 * <p/>
 * Registered via {@code META-INF/services/org.teavm.extension.spi.substitution.SubstitutionPolicy}.
 */
public class BasisuTeaVMSubstitutionPolicy extends SimpleSubstitutionPolicy {
    @Override
    public void contribute(SubstitutionSink sink) {
        Predicate<String> basisuClasses = inPackage("com.crashinvaders.basisu", true);
        sink.selectClasses(basisuClasses).packagePrefix("emu");
    }
}
