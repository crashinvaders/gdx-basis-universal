var basisuModule = null;
console.log("Loading LibGDX Basis Universal native library...");
createBasisuGdxModule().then(loadedModule => {
    basisuModule = loadedModule;
    const event = new Event("basisuModuleLoaded", {
        "bubbles": true,
        "basisuModule": basisuModule
    });
    window.dispatchEvent(event);
});
function isBasisuModuleLoaded() {
    return basisuModule !== null;
}