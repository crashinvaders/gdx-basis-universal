#include <iostream>
#include <cstdio>

#include "test.h"

int32_t Test::getValue() {
    std::cout << "Hello world from C++" << "\n";

    return this->value;
}



#include "basisu_transcoder.h"

using namespace basist;

static uint8_t const srcRgb[] = {
    #include "testcard.basis.inc"
};

static etc1_global_selector_codebook* s_globalCodebook = NULL;

void testBasis() {
    std::cout << "Basisu test begin." << "\n";

    basisu_transcoder_init();
    if (!s_globalCodebook) {
         s_globalCodebook = new etc1_global_selector_codebook(g_global_selector_cb_size, g_global_selector_cb);
    }
    basisu_transcoder transcoder(s_globalCodebook);
    if (transcoder.validate_header(srcRgb, sizeof srcRgb)) {
        std::cout << "Basis file data is valid!" << "\n";
        basisu_file_info fileInfo;
        if (transcoder.get_file_info(srcRgb, sizeof srcRgb, fileInfo)) {
            std::cout << "File info extracted." << "\n";
            basisu_image_info info;
            if (transcoder.get_image_info(srcRgb, sizeof srcRgb, info, 0)) {
                printf("Success (file w: %d, h: %d, mips: %d)\n",
                    info.m_width, info.m_height, info.m_total_levels);
            }
        }
    }

    std::cout << "Basisu test end." << "\n";
}