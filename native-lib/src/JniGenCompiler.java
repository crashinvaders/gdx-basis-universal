import com.badlogic.gdx.jnigen.*;
import com.badlogic.gdx.jnigen.BuildTarget.TargetOs;

public class JniGenCompiler {
    private static final String[] HEADERS = {
            "src",
    };
    private static final String[] EXCLUDES = {
            "test/**",
            ".vscode/**",
            "build/**"
    };

    public static void main(String[] args) throws Exception {
        PatchedNativeCodeGenerator jnigen = new PatchedNativeCodeGenerator();
        jnigen.generate("src", "jni", new String[]{"**/Main.java"}, null);


        BuildTarget win32 = prepare(BuildTarget.newDefaultTarget(TargetOs.Windows, false));
        win32.compilerPrefix = "mingw32-";
        win32.compilerSuffix = ".exe";
        BuildTarget win64 = prepare(BuildTarget.newDefaultTarget(TargetOs.Windows, true));
        win64.compilerSuffix = ".exe";
        BuildTarget linux32 = prepare(BuildTarget.newDefaultTarget(TargetOs.Linux, false));
        BuildTarget linux64 = prepare(BuildTarget.newDefaultTarget(TargetOs.Linux, true));
        BuildTarget mac = prepare(BuildTarget.newDefaultTarget(TargetOs.MacOsX, true));

        new AntScriptGenerator().generate(new BuildConfig("my-native-lib"), win32, win64);

        executeAnt("jni/build-windows32.xml", "-v", "-Drelease=true", "clean", "postcompile");
        executeAnt("jni/build-windows64.xml", "-v", "-Drelease=true", "clean", "postcompile");
//        executeAnt("jni/build-linux32.xml", "-v", "-Drelease=true", "clean", "postcompile");
//        executeAnt("jni/build-linux64.xml", "-v", "-Drelease=true", "clean", "postcompile");
//        executeAnt("jni/build-macosx32.xml", "-v", "-Drelease=true", "clean", "postcompile");
        executeAnt("jni/build.xml", "-v", "pack-natives");
    }

    private static BuildTarget prepare(BuildTarget target) {
        target.headerDirs = HEADERS;
        target.cExcludes = EXCLUDES;
        target.cppExcludes = EXCLUDES;

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