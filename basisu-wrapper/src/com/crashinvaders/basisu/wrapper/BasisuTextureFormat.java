package com.crashinvaders.basisu.wrapper;

/** Direct mapping of <code>basist::basis_tex_format</code> enum constants. */
public enum BasisuTextureFormat implements UniqueIdValue {
    ETC1S(0),
    UASTC4x4(1),
    ;

    private final int id;

    BasisuTextureFormat(int id) {
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }
}
