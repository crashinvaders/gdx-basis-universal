package com.crashinvaders.basisu;

/**
 * Direct mapping of <code>basist::basis_texture_type</code> enum constants.
 * <p/>
 * The image type field attempts to describe how to interpret the image data in a Basis file.
 * The encoder library doesn't really do anything special or different with these texture types, this is mostly here for the benefit of the user.
 * We do make sure the various constraints are followed (2DArray/cubemap/videoframes/volume implies that each image has the same resolution and # of mipmap levels, etc., cubemap implies that the # of image slices is a multiple of 6)
 */
public enum BasisuTextureType implements UniqueIdValue {
    REGULAR_2D(0),          // An arbitrary array of 2D RGB or RGBA images with optional mipmaps, array size = # images, each image may have a different resolution and # of mipmap levels
    REGULAR_2D_ARRAY(1),    // An array of 2D RGB or RGBA images with optional mipmaps, array size = # images, each image has the same resolution and mipmap levels
    CUBEMAP_ARRAY(2),       // an array of cubemap levels, total # of images must be divisable by 6, in X+, X-, Y+, Y-, Z+, Z- order, with optional mipmaps
    VIDEO_FRAMES(3),        // An array of 2D video frames, with optional mipmaps, # frames = # images, each image has the same resolution and # of mipmap levels
    VOLUME(4),              // A 3D texture with optional mipmaps, Z dimension = # images, each image has the same resolution and # of mipmap levels
    ;

    private final int id;

    BasisuTextureType(int id) {
        this.id = id;
    }

    @Override
    public int getId() {
        return id;
    }
}
