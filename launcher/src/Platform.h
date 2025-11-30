#ifndef NFFJNI
#define NFFJNI

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

extern "C" {
    #include "external/lua/lua.h"
    #include "external/lua/lauxlib.h"
    #include "external/lua/lualib.h"
}

extern const char* getHomeDir();

extern int changeDir(char* path);

extern int launchVM(
        char* vmLocation, 
        int vmOptionCount, 
        char** vmOptions, 
        int argC,
        char** argv, 
        char* mainClass
);

#if defined(_WIN32) || defined(__MINGW32__)

    #include "external/windows/jni.h"
    #include <tchar.h>
    #include <Windows.h>
    #include <shlobj.h>
    #include <direct.h>  // _chdir
    #define API_EXPORT extern "C" __declspec(dllexport)
    #define Handler HINSTANCE
    #define LIB_NAME "runtime/bin/server/jvm.dll"

#elif defined(__APPLE__)

    #include <dlfcn.h>
    #include <mach-o/dyld.h>
    #include <pwd.h>
    #include <unistd.h>
    #include "external/osx/jni.h"
    #define LIB_NAME "runtime/bin/server/libjvm.dylib"

    #define API_EXPORT extern "C"
    #define _T(param) param
    #define Handler void*

    extern Handler LoadLibrary(const char* path);
    extern void* GetProcAddress(Handler lib,const char* funcName);
    extern void FreeLibrary(Handler lib);

#elif defined(__linux__)

    #include <dlfcn.h>
    #include <unistd.h>
    #include <pwd.h>
    #include <unistd.h>
    #include "external/linux/jni.h"
    #define LIB_NAME "runtime/lib/server/libjvm.so"

    #define API_EXPORT extern "C"
    #define _T(param) param
    #define Handler void*

    extern Handler LoadLibrary(const char* path);
    extern void* GetProcAddress(Handler lib,const char* funcName);
    extern void FreeLibrary(Handler lib);

#endif

#endif