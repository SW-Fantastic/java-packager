#include <stdio.h>
#include <string.h>
#include "Platform.h"
#include "external/cJSON.h"
#include "external/whereami.h"
#include "external/lualfs/lfs.h"

cJSON* readConfigFile() {

    FILE *fp = fopen("package.json", "r");
    if(!fp) {
        return NULL;
    }
    fseek(fp, 0 , SEEK_END);
    long length = ftell(fp);
    fseek(fp, 0, SEEK_SET);

    char* buf = (char*)malloc(sizeof(char) * length + 1);
    memset(buf, 0, length + 1);
    fread(buf, 1, length, fp);
    fclose(fp);
    cJSON *json = cJSON_Parse(buf);
    if(json) {
        return json;
    }
    return NULL;

}


char* getLocation(char* target) {

    int dirname_length = 0;
    int length = wai_getExecutablePath(NULL, 0, &dirname_length);
    if(length <= 0) {
        return NULL;
    }
    
    char* path = (char*)malloc(sizeof(char) * (length + 1));
    if(!path) {
        return NULL;
    }
    wai_getExecutablePath(path, length, &dirname_length);
    path[dirname_length] = '\0';

    int len = strlen(path) + strlen(path) + 2;
    char* realPath = (char*)malloc(sizeof(char) * len);
    memset(realPath, 0, len);
    strcpy(realPath, path);
    strcat(realPath, target);

    return realPath;

}

char* getExecutableLocation() {

    int dirname_length = 0;
    int length = wai_getExecutablePath(NULL, 0, &dirname_length);
    if (length <= 0) {
        return NULL;
    }

    char* path = (char*)malloc(sizeof(char) * (length + 1));
    if (!path) {
        return NULL;
    }
    wai_getExecutablePath(path, length, &dirname_length);
    return path;

}


char* readScriptFile(char* script) {

    FILE* fp = fopen(script, "r");
    if (!fp) {
        return NULL;
    }
    fseek(fp, 0, SEEK_END);
    long length = ftell(fp);
    fseek(fp, 0, SEEK_SET);

    char* buf = (char*)malloc(sizeof(char) * length + 1);
    memset(buf, 0, length + 1);
    fread(buf, 1, length, fp);
    fclose(fp);
    return buf;

}

char* getLibOption(cJSON* modulePath, cJSON* classPath) {

    char* optClassPath = "";

    if(modulePath) {
        optClassPath = "--module-path=";
    } else if (classPath) {
        optClassPath = "-Djava.class.path=";
    } else {
        printf("There must be class-path or module-path in config file, cannot launch java vm");
        return NULL;
    }

    char* libPathStr = cJSON_GetStringValue(modulePath == NULL ? classPath : modulePath);
    libPathStr = getLocation(libPathStr);
    int libPathStrLen = strlen(optClassPath) + strlen(libPathStr) + 2;
        
    char* optBuf = (char*)malloc(sizeof(char) * libPathStrLen);
    if (optBuf == NULL) {
        return NULL;
    }
    memset(optBuf, 0, libPathStrLen);
    strcpy(optBuf, optClassPath);
    strcat(optBuf, libPathStr);
    return optBuf;

}

char* getModuleOption(cJSON* mainModule) {
    
    if (!mainModule) {
        return NULL;
    }

    char* strMainModule = cJSON_GetStringValue(mainModule);
    const char* prefix = "--add-modules=";
    int libPathStrLen = strlen(prefix) + strlen(strMainModule) + 2;
    char* optBuf = (char*)malloc(sizeof(char) * libPathStrLen);
    if (optBuf == NULL) {
        return NULL;
    }

    memset(optBuf, 0, libPathStrLen);
    strcpy(optBuf, prefix);
    strcat(optBuf, strMainModule);

    return optBuf;

}

char* getRootModuleOpt(cJSON* mainModule) {

    if (!mainModule) {
        return NULL;
    }

    char* strMainModule = cJSON_GetStringValue(mainModule);
    const char* prefix = "--modules=";
    int libPathStrLen = strlen(prefix) + strlen(strMainModule) + 2;
    char* optBuf = (char*)malloc(sizeof(char) * libPathStrLen);
    if (optBuf == NULL) {
        return NULL;
    }
    memset(optBuf, 0, libPathStrLen);
    strcpy(optBuf, prefix);
    strcat(optBuf, strMainModule);
    return optBuf;

}

char* getClassicMainOpt(cJSON* mainClass, cJSON* mainModule) {

    if(!mainClass || !mainModule) {
        return NULL;
    }

    char* mainClassStr = cJSON_GetStringValue(mainClass);
    char* moduleOpt = " ";
    char* mainModuleStr = cJSON_GetStringValue(mainModule);

    int mainLen = strlen(mainClassStr) + strlen(moduleOpt) + strlen(mainModuleStr) + 4;
    char* optMain = (char*)malloc(sizeof(char) * mainLen);
    memset(optMain, 0, mainLen);
    strcpy(optMain, moduleOpt);
    strcat(optMain, mainModuleStr);
    strcat(optMain, "/");
    strcat(optMain, mainClassStr);

    return optMain;
}


int luaGetSystemName(lua_State* state) {

#if defined(_WIN32) || defined(__MINGW32__)
    lua_pushstring(state, "windows");
    return 1;
#elif defined(__APPLE__)
    lua_pushstring(state, "macos");
    return 1;
#elif defined(__linux__)
    lua_pushstring(state, "linux");
    return 1;
#else
    lua_pushstring(state, "unknown");
    return 1;
#endif 

}


int luaGetAppName(lua_State* engine) {
    cJSON* json = readConfigFile();
    if (json == NULL) {
        lua_pushstring(engine, "");
        return 1;
    }
    cJSON* val = cJSON_GetObjectItem(json, "name");
    if (val == NULL) {
        lua_pushstring(engine, "");
        return 1;
    }
    lua_pushstring(engine, cJSON_GetStringValue(val));
    return 1;
}


int luaGetAppPath(lua_State* engine) {
    lua_pushstring(engine, getExecutableLocation());
    return 1;
}

int luaGetBasePath(lua_State* engine) {
    lua_pushstring(engine, getLocation(""));
    return 1;
}

int luaGetGui(lua_State* engine) {
    cJSON* json = readConfigFile();
    if (json == NULL) {
        lua_pushboolean(engine, 0);
        return 1;
    }
    cJSON* val = cJSON_GetObjectItem(json, "console");
    if (val == NULL) {
        lua_pushboolean(engine, 0);
        return 1;
    }
    lua_pushboolean(engine,cJSON_IsTrue(val));
    return 1;

}

int luaGetHomeDir(lua_State* state) {

    const char* homeDir = getHomeDir();
    if (homeDir == NULL) {
        lua_pushstring(state, "");
        return 1;
    }

    lua_pushstring(state, homeDir);
    return 1;
}

int main(int argC, char** argV) {

    char* loc = getLocation("");
    if(loc == NULL) {
        printf("Can not find executable location.");
        return 0;
    }

    if(chdir(loc) < 0) {
        printf("Cannot find execute location, failed to launch java vm, exit");
        return 0;
    }

    cJSON* json = readConfigFile();
    if(!json) {
        printf("Cannot load config file, can not launch java vm, exit");
        return 0;
    }

    cJSON* modulePath = NULL;
    if(cJSON_HasObjectItem(json, "module-path")) {
        modulePath = cJSON_GetObjectItem(json, "module-path");
    }

    cJSON* classPath = NULL;
    if(cJSON_GetObjectItem(json, "class-path")) {
        classPath = cJSON_GetObjectItem(json, "class-path");
    }

    cJSON* mainClass = NULL;
    if(cJSON_GetObjectItem(json, "main-class")) {
        mainClass = cJSON_GetObjectItem(json, "main-class");
    }

    cJSON* mainModule = NULL;
    if(cJSON_GetObjectItem(json, "main-module")) {
        mainModule = cJSON_GetObjectItem(json, "main-module");
    }

    cJSON* vmOptions = cJSON_GetObjectItem(json, "options");

    if(!mainClass) {
        printf("Cannot find main class in config file, can not launch java vm");
        return 0;
    } 

    char* optClassPath = getLibOption(modulePath, classPath);
    
    int nCustomOptions = cJSON_GetArraySize(vmOptions);
    int nVmOptions = nCustomOptions + (mainModule == NULL ? 1 : 2);
    char** options = (char**)malloc(sizeof(char*) * nVmOptions);
    if(nCustomOptions > 0) {
        for(int idx = 0; idx < nCustomOptions; idx ++) {
            cJSON* opt = cJSON_GetArrayItem(vmOptions, idx);
            options[idx] = cJSON_GetStringValue(opt); 
        }
    }

    options[nCustomOptions] = optClassPath;
    if (mainModule) {
        cJSON* modulesList = cJSON_GetObjectItem(json, "modules");
        options[nCustomOptions + 1] = getModuleOption(modulesList);
        printf("option : %s \n", cJSON_GetStringValue(modulesList));
    }

    printf("option : %s \n", optClassPath);
    printf("option : %s \n", cJSON_GetStringValue(mainClass));

    cJSON* script = cJSON_GetObjectItem(json, "script");
    if (script) {

        lua_State* scriptEngine = luaL_newstate();
        luaL_openlibs(scriptEngine);
        luaL_requiref(scriptEngine, "lfs", luaopen_lfs, 1);
        lua_pop(scriptEngine, 1);
        lua_register(scriptEngine, "getHomeDir", luaGetHomeDir);
        lua_register(scriptEngine, "getSystemName", luaGetSystemName);
        lua_register(scriptEngine, "getAppLocation", luaGetAppPath);
        lua_register(scriptEngine, "getAppName", luaGetAppName);
        lua_register(scriptEngine, "getAppDir", luaGetBasePath);
        lua_register(scriptEngine, "isGui", luaGetGui);

        char* scriptData = readScriptFile(cJSON_GetStringValue(script));
        if (scriptData != NULL) {
            printf("before startup: %s\n", cJSON_GetStringValue(script));
            luaL_dostring(scriptEngine, scriptData);
            free(scriptData);
        }

        lua_close(scriptEngine);
    }

    return launchVM(LIB_NAME,nVmOptions, options, argC, argV, cJSON_GetStringValue(mainClass));
}