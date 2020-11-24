package com.crashinvaders.basisu.wrapper.build;

import com.badlogic.gdx.jnigen.*;
import com.badlogic.gdx.jnigen.BuildTarget.TargetOs;

public class BasisUniversalBuild {

    public static void main(String[] args) throws Exception {
        PatchedNativeCodeGenerator jnigen = new PatchedNativeCodeGenerator();
        jnigen.generate("src", "jni", new String[]{"**/com/crashinvaders/basisu/wrapper/*.java"}, null);


        BuildTarget win32 = prepare(BuildTarget.newDefaultTarget(TargetOs.Windows, false));
        win32.compilerPrefix = "mingw32-";
        win32.compilerSuffix = ".exe";
        BuildTarget win64 = prepare(BuildTarget.newDefaultTarget(TargetOs.Windows, true));
        win64.compilerSuffix = ".exe";
        BuildTarget linux32 = prepare(BuildTarget.newDefaultTarget(TargetOs.Linux, false));
        BuildTarget linux64 = prepare(BuildTarget.newDefaultTarget(TargetOs.Linux, true));
        BuildTarget mac64 = prepare(BuildTarget.newDefaultTarget(TargetOs.MacOsX, true));
        BuildTarget android = prepare(BuildTarget.newDefaultTarget(TargetOs.Android, false));
        BuildTarget ios = prepare(BuildTarget.newDefaultTarget(TargetOs.IOS, false));
        ios.cppFlags += " -stdlib=libc++ ";

//        new AntScriptGenerator().generate(new BuildConfig("gdx-basis-universal"), win64);
//        new AntScriptGenerator().generate(new BuildConfig("gdx-basis-universal"), mac64);
        new AntScriptGenerator().generate(new BuildConfig("gdx-basis-universal"), linux64);

//        executeAnt("jni/build-windows32.xml", "-v", "-Drelease=true", "clean", "postcompile");
//        executeAnt("jni/build-windows64.xml", "-v", "-Drelease=true", "clean", "postcompile");
//        executeAnt("jni/build-linux32.xml", "-v", "-Drelease=true", "clean", "postcompile");
        executeAnt("jni/build-linux64.xml", "-v", "-Drelease=true", "clean", "postcompile");
//        executeAnt("jni/build-macosx64.xml", "-v", "-Drelease=true", "clean", "postcompile");
//        executeAnt("jni/build-ios32.xml", "-v", "-Drelease=true", "clean", "postcompile");
        executeAnt("jni/build.xml", "-v", "pack-natives");
    }

    private static BuildTarget prepare(BuildTarget target) {
        final String[] headers = {
                "src",
        };
        final String[] excludes = {
                "test/**",
                ".vscode/**",
                "build/**",
        };

        target.headerDirs = headers;
        target.cExcludes = excludes;
        target.cppExcludes = excludes;

        target.cFlags += " -fvisibility=hidden ";
        target.cppFlags += " -std=c++11 -fvisibility=hidden ";
        target.linkerFlags += " -fvisibility=hidden ";

        return target;
    }

    private static void executeAnt(String buildFile, String... params) {
        if (!PatchedBuildExecutor.executeAnt(buildFile, params))
            throw new RuntimeException("Failed to execute " + buildFile + " Ant script.");
    }
}