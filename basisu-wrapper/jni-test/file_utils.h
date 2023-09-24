#pragma once

#include <cstring>
#include <climits>

#include "basisu_containers.h"

using namespace std;

namespace fileUtils {

    int getFileSize(const char* fileName);

    basisu::vector<uint8_t> readFile(const char* fileName);
}