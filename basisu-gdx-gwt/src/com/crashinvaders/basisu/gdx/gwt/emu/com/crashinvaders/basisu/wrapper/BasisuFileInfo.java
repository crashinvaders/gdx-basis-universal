package com.crashinvaders.basisu.wrapper;

public class BasisuFileInfo {

    public BasisuTextureType getTextureType() {
        throw new UnsupportedOperationException("Not yet implemented for GWT");
        // (int)getWrapped(addr)->m_tex_type;
    }

    public BasisuTextureFormat getTextureFormat() {
        throw new UnsupportedOperationException("Not yet implemented for GWT");
        // (int)getWrapped(addr)->m_tex_format;
    }

    public int getVersion() {
        throw new UnsupportedOperationException("Not yet implemented for GWT");
        // getWrapped(addr)->m_version;
    }

    public int getTotalHeaderSize() {
        throw new UnsupportedOperationException("Not yet implemented for GWT");
        // getWrapped(addr)->m_total_header_size;
    }

    public int getTotalSelectors() {
        throw new UnsupportedOperationException("Not yet implemented for GWT");
        // getWrapped(addr)->m_total_selectors;
    }

    public int getSelectorCodebookSize() {
        throw new UnsupportedOperationException("Not yet implemented for GWT");
        // getWrapped(addr)->m_selector_codebook_size;
    }

    public int getTotalEndpoints() {
        throw new UnsupportedOperationException("Not yet implemented for GWT");
        // getWrapped(addr)->m_total_endpoints;
    }

    public int getEndpointCodebookSize() {
        throw new UnsupportedOperationException("Not yet implemented for GWT");
        // getWrapped(addr)->m_endpoint_codebook_size;
    }

    public int getTablesSize() {
        throw new UnsupportedOperationException("Not yet implemented for GWT");
        // getWrapped(addr)->m_tables_size;
    }

    public int getSlicesSize() {
        throw new UnsupportedOperationException("Not yet implemented for GWT");
        // getWrapped(addr)->m_slices_size;
    }

    public int getUsPerFrame() {
        throw new UnsupportedOperationException("Not yet implemented for GWT");
        // getWrapped(addr)->m_us_per_frame;
    }

    /** Total number of images. */
    public int getTotalImages() {
        throw new UnsupportedOperationException("Not yet implemented for GWT");
        // getWrapped(addr)->m_total_images;
    }

    /** The number of mipmap levels for each image. */
    public int[] getImageMipmapLevels() {
        throw new UnsupportedOperationException("Not yet implemented for GWT");
        // std::vector<uint32_t> imageLevels = getWrapped(addr)->m_image_mipmap_levels;
    }

    public int getUserdata0() {
        throw new UnsupportedOperationException("Not yet implemented for GWT");
        // getWrapped(addr)->m_userdata0;
    }

    public int getUserdata1() {
        throw new UnsupportedOperationException("Not yet implemented for GWT");
        // getWrapped(addr)->m_userdata1;
    }

    public boolean isFlippedY() {
        throw new UnsupportedOperationException("Not yet implemented for GWT");
        // getWrapped(addr)->m_y_flipped;
    }

    public boolean isEtc1s() {
        throw new UnsupportedOperationException("Not yet implemented for GWT");
        // getWrapped(addr)->m_etc1s;
    }

    public boolean hasAlphaSlices() {
        throw new UnsupportedOperationException("Not yet implemented for GWT");
        // getWrapped(addr)->m_has_alpha_slices;
    }
}
