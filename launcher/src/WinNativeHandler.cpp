#define STB_IMAGE_IMPLEMENTATION
#include <Windows.h>
#include "WinNativeHandler.h"
#include "external/stb_image.h"

#pragma pack(push, 1)
struct GRPICONDIR {
    WORD idReserved; // 0
    WORD idType;     // 1 = 图标
    WORD idCount;    // 图标数量
};

struct GRPICONDIRENTRY {
    BYTE  bWidth;       // 0 表示 256
    BYTE  bHeight;      // 0 表示 256
    BYTE  bColorCount;  // 0 如果 > 8 位
    BYTE  bReserved;    // 0
    WORD  wPlanes;      // 通常 1
    WORD  wBitCount;    // 32 位
    DWORD dwBytesInRes; // RT_ICON 的字节数
    WORD  nID;          // RT_ICON 的资源 ID
};
#pragma pack(pop)

LPCWSTR charToLPCWSTR(const char* charString) {
    int len = strlen(charString);
    int wlen = MultiByteToWideChar(CP_ACP, 0, charString, len, NULL, 0);

    wchar_t* wstr = new wchar_t[wlen + 1];
    MultiByteToWideChar(CP_ACP, 0, charString, len, wstr, wlen);
    wstr[wlen] = 0;

    return wstr;
}


char* asBitmap(const char* path, long* length, BITMAPINFOHEADER* header) {
    FILE* fp = fopen(path, "rb");
    if (!fp) {
        return NULL;
    }
    
    int w;
    int h;
    int n;
    unsigned char* rgba = stbi_load_from_file(fp, &w, &h, &n, 4);

    fclose(fp);

    BITMAPINFOHEADER bih = {};
    bih.biSize = sizeof(BITMAPINFOHEADER);
    bih.biWidth = w;
    bih.biHeight = h * 2; // ICON 存储时是高度*2（上半位 AND mask, 下半位 XOR mask）
    bih.biPlanes = 1;
    bih.biBitCount = 32;
    bih.biCompression = BI_RGB;

    int offset = sizeof(BITMAPINFOHEADER);
    int size = offset + w * h * 4;
    char* data = (char*)malloc(sizeof(char) * size);
    if (data == NULL) {
        stbi_image_free(rgba);
        return NULL;
    }
    memset(data, 0, size);
    memcpy(data, &bih, offset);
    memcpy(data + offset, rgba, w * h * 4);

    char* reversed = (char*)malloc(sizeof(char) * size);
    if (reversed == NULL) {
        stbi_image_free(rgba);
        return NULL;
    }
    memcpy(reversed, data, size);
    for (int y = 0; y < h; y++) {
        for (int x = 0; x < w; x++) {
            int srcIdx = (h - 1 - y) * w + x;
            int dstIdx = y * w + x;
            reversed[offset + dstIdx * 4 + 0] = rgba[srcIdx * 4 + 2];
            reversed[offset + dstIdx * 4 + 1] = rgba[srcIdx * 4 + 1];
            reversed[offset + dstIdx * 4 + 2] = rgba[srcIdx * 4 + 0];
            reversed[offset + dstIdx * 4 + 3] = rgba[srcIdx * 4 + 3];
        }
    }

    free(data);

    *length = size;
    *header = bih;
    return reversed;
}

JNIEXPORT jint JNICALL Java_org_swdc_packager_core_FileUtils_updateExecutableIcon
(JNIEnv* env, jclass caller, jstring exePath, jobjectArray iconPaths) {

    if (!exePath || !iconPaths) {
        return 0;
    }
    
    const char* theExePath = env->GetStringUTFChars(exePath, 0);
    int iconLen = env->GetArrayLength(iconPaths);
 

    LPCWSTR realExePath = charToLPCWSTR(theExePath);
    HANDLE targetExe = BeginUpdateResourceW(realExePath, FALSE);
    if(targetExe == NULL) {
        // 打不开EXE，结束
        env->ReleaseStringUTFChars(exePath, theExePath);
        return -2;
    }

    GRPICONDIR header = { 0, 1, iconLen };
    GRPICONDIRENTRY *entries = (GRPICONDIRENTRY*)malloc(sizeof(GRPICONDIRENTRY) * iconLen);
    memset(entries, 0, sizeof(GRPICONDIRENTRY) * iconLen);

    for (int i = 0; i < iconLen; i++) {
        jstring iconPath = (jstring)env->GetObjectArrayElement(iconPaths, i);
        const char* theIconPath = env->GetStringUTFChars(iconPath, 0);

        BITMAPINFOHEADER header;
        long length = 0;
        char* iconBuf = asBitmap(theIconPath, &length, &header);
        if (iconBuf <= 0 || length == 0) {
            env->ReleaseStringUTFChars(exePath, theExePath);
            env->ReleaseStringUTFChars(iconPath, theIconPath);
            return -1;
        }

        if (!UpdateResourceW(
            targetExe,
            (LPWSTR)RT_ICON,
            (LPWSTR)MAKEINTRESOURCE(i + 1),
            MAKELANGID(LANG_NEUTRAL, SUBLANG_NEUTRAL),
            iconBuf,
            static_cast<DWORD>(length)
        )) {
            // 更改失败
            env->ReleaseStringUTFChars(exePath, theExePath);
            env->ReleaseStringUTFChars(iconPath, theIconPath);
            free(iconBuf);
            EndUpdateResource(targetExe, TRUE);
            return -3;
        }

        entries[i].bWidth = header.biWidth;
        entries[i].bHeight = header.biHeight / 2;
        entries[i].bColorCount = 0;
        entries[i].bReserved = 0;
        entries[i].wPlanes = 1;
        entries[i].dwBytesInRes = length;
        entries[i].wBitCount = header.biBitCount;
        entries[i].nID = i + 1;

        env->ReleaseStringUTFChars(iconPath, theIconPath);
        free(iconBuf);
    }
    
    int gpHeaderOffset = sizeof(GRPICONDIR);
    int sizeGpBuf = sizeof(GRPICONDIR) + (iconLen * sizeof(GRPICONDIRENTRY));
    char* gpBuf = (char*)malloc(sizeof(char) * sizeGpBuf);
    if (gpBuf == NULL) {
        free(entries);
        env->ReleaseStringUTFChars(exePath, theExePath);
        return -4;
    }

    memset(gpBuf, 0, sizeGpBuf * sizeof(char));
    memcpy(gpBuf, &header, sizeof(GRPICONDIR));
    memcpy(gpBuf + gpHeaderOffset, entries, iconLen * sizeof(GRPICONDIRENTRY));

    if (!UpdateResourceW(
        targetExe,
        (LPWSTR)RT_GROUP_ICON,
        (LPWSTR)MAKEINTRESOURCE(1),
        MAKELANGID(LANG_NEUTRAL, SUBLANG_NEUTRAL),
        gpBuf,
        static_cast<DWORD>(sizeGpBuf)
    )) {
        // 更改失败
        env->ReleaseStringUTFChars(exePath, theExePath);
        free(entries);
        EndUpdateResource(targetExe, TRUE);
        return -3;
    }

    if (!EndUpdateResource(targetExe, FALSE)) {
        env->ReleaseStringUTFChars(exePath, theExePath);
        return -4;
    }

    env->ReleaseStringUTFChars(exePath, theExePath);
    
    return 0;
}




