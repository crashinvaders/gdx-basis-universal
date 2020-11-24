#include <iostream>

#include "file_utils.h"
#include "basisu_transcoder.h"
#include "basisu_wrapper.h"

using namespace std;

int main(int, char**) {
    vector<uint8_t> basisData = fileUtils::readFile("../../resources/kodim3.basis");
    if (basisData.size() == 0) {
        cout << "An error occured during reading the file." << endl;
        return 1;
    }

    cout << "File was successfully read. Size: " << basisData.size() << endl;
    
    if (!basisuWrapper::validateHeader(basisData.data(), basisData.size())) {
        cout << "File is not a valid basis universal image!" << endl;
        return 2;
    }

    vector<uint8_t> rgba;
    if (!basisuWrapper::transcode(rgba, basisData.data(), basisData.size(), 0, basist::transcoder_texture_format::cTFRGBA4444)) {
        cout << "Error during image transcoding!" << endl;
        return 3;
    }

    cout << "rgba[0] = [" << (int)rgba[0] << " " << (int)rgba[1] << " " << (int)rgba[2] << " " << (int)rgba[3] << "] size: " << rgba.size() << endl;

    return 0;
}