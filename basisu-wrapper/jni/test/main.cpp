#include <string>
#include <iostream>

#include "file_utils.h"
#include "basisu_transcoder.h"
#include "basisu_wrapper.h"
#include "basisu_native_utils.h"

#define LOG_TAG "jni-test"

int main(int, char**) {
    basisu::vector<uint8_t> basisData = fileUtils::readFile("../../test/resources/screen_stuff.uastc.ktx2");
    if (basisData.size() == 0) {
        basisuUtils::logError(LOG_TAG, "An error occurred during reading the file.");
        return 1;
    }
    std::cout << "File was successfully read. Size: " << basisData.size() << "\n";

    basisuWrapper::ktx2_file_info fileInfo;
    if (!basisuWrapper::ktx2::getFileInfo(fileInfo, basisData.data(), basisData.size())) {
        basisuUtils::logError(LOG_TAG, "Cannot get file info from the KTX2 file.");
        return 2;
    }

    basisu::vector<uint8_t> rgba;
    if (!basisuWrapper::ktx2::transcode(rgba, basisData.data(), basisData.size(), 0, 0, basist::transcoder_texture_format::cTFRGBA4444)) {
        basisuUtils::logError(LOG_TAG, "Error during image transcoding!");
        return 3;
    }

    int mipmapLevel = 0;
    basist::ktx2_image_level_info levelInfo;
    if (!basisuWrapper::ktx2::getImageLevelInfo(levelInfo, basisData.data(), basisData.size(), 0, mipmapLevel)) {
        basisuUtils::logError(LOG_TAG, "Error during mipmap level info retrieval from KTX2 file.");
        return 4;
    }
    std::cout << "Mipmap level " << mipmapLevel << " size is " << levelInfo.m_width << "x" << levelInfo.m_height << "\n";

    basisuUtils::logInfo(LOG_TAG, "The test finished successfully!");
    
    basisuUtils::throwException(nullptr, "TEST EXCEPTION!");
    
    return 0;
}
