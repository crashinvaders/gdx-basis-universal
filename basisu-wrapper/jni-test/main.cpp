#include <string>
#include <iostream>

#include "file_utils.h"
#include "basisu_transcoder.h"
#include "basisu_wrapper.h"
#include "basisu_native_utils.h"

#define LOG_TAG "jni-test"

int main(int, char**) {
    std::vector<uint8_t> basisData = fileUtils::readFile("../test-resources/kodim3.basis");
    if (basisData.size() == 0) {
        basisuUtils::logError(LOG_TAG, "An error occured during reading the file.");
        return 1;
    }

    basisuUtils::logInfo(LOG_TAG, (std::string("File was successfully read. Size: ") += basisData.size()).c_str());

    
    if (!basisuWrapper::validateHeader(basisData.data(), basisData.size())) {
        basisuUtils::logError(LOG_TAG, "File is not a valid basis universal image!");
        return 2;
    }

    std::vector<uint8_t> rgba;
    if (!basisuWrapper::transcode(rgba, basisData.data(), basisData.size(), 0, 0, basist::transcoder_texture_format::cTFRGBA4444)) {
        basisuUtils::logError(LOG_TAG, "Error during image transcoding!");
        return 3;
    }
    basisuUtils::logInfo(LOG_TAG, "The test finished successfully!");

    basisuUtils::throwException(nullptr, "TEST EXCEPTION!");
    
    return 0;
}