# GDX Basis Universal Demo

A simple libGDX application to test the library capabilities in runtime.

### Configuration
Demo projects' Gradle configuration offers two ways to reference the `gdx-basis-universal` libs.
In `demo/build.gradle` set `ext.useGdxBasisuMavenArtifacts` to: 
1. `false` to use assembled native and jar libs directly from the project modules. You need to build and assemble them first (read Build & Run section below). This case works best during development, where you want the changes to the core libs to be reflected instantly in the demo app.
2. `true` to imitate real life scenario where you use `gdx-basis-universal` as a remotely hosted library. For that you need to install the Maven artifacts locally using `installAll` Gradle task. 

### Build & Run

In order to run the demo app, you need to pack the natives into a JAR file locally (for when `ext.useGdxBasisuMavenArtifacts = false`).
Simply call (example for desktop natives):

```shell
./gradlew jnigenJarNativesDesktop
```

And then to run the demo:

```shell
./gradlew demo:desktop:run
```