var basisuModule = null;
console.log("Loading LibGDX Basis Universal native library...");
createBasisuGdxModule().then(loadedModule => {
    basisuModule = loadedModule;
    const event = document.createEvent("HTMLEvents");
    event.initEvent("basisuModuleLoaded", true, true);
    event.eventName = "basisuModuleLoaded";
    element.dispatchEvent(event);
});
fun isBasisuModuleLoaded() {
    return basisuModule !== null;
}