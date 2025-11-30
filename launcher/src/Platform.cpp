#include "Platform.h"
JNIEnv* env;
JavaVM* jvm;
Handler jvmDLL;

typedef jint(JNICALL* JNICREATEPROC)(JavaVM**, void**, void*);


#if defined(_WIN32) || defined(__MINGW32__)
const char* getHomeDir() {

    static char path[4096];
    // 使用SHGetKnownFolderPath获取windows系统的用户主文件夹
    HRESULT hr = CoInitialize(NULL);
    if (FAILED(hr)) {
        return NULL;
    }
    PWSTR wpath;
    if (SUCCEEDED(SHGetKnownFolderPath(FOLDERID_Profile, 0, NULL, &wpath))) {
        // 宽字符转换到多字节
        WideCharToMultiByte(CP_UTF8, 0, wpath, -1, path, MAX_PATH, NULL, NULL);
        CoTaskMemFree(wpath);
        CoUninitialize();
        return path;
    }

    // 备选方案 - 环境变量
    const char* env_home = getenv("USERPROFILE");
    if (env_home != NULL && strlen(env_home) > 0) {
        CoUninitialize();
        return env_home;
    }

    CoUninitialize();
    return NULL;
}
#else

const char* getHomeDir() {

    struct passwd* pw = getpwuid(getuid());
    if (pw != NULL) {
        return pw->pw_dir;
    }
    const char* home = getenv("HOME");
    if (home != NULL && strlen(home) > 0) {
        return home;
    }
    return NULL;

}

#endif

int launchVM(
    char* vmLocation, 
    int vmOptionCount, 
    char** vmOptions, 
    int argC,
    char** argv, 
    char* mainClass
) {
    
    if (jvm != NULL) {
		return 0;
	}

    // 加载JVM的动态库
	jvmDLL = LoadLibrary(vmLocation);
    if (jvmDLL == NULL) {
		return -1;
	}

	//初始化jvm物理地址
	JNICREATEPROC jvmProcAddress = (JNICREATEPROC)GetProcAddress(jvmDLL, "JNI_CreateJavaVM");
	if (jvmProcAddress == NULL) {
		FreeLibrary(jvmDLL);
		return -2;
	}

    JavaVMOption* vmOption = new JavaVMOption[vmOptionCount];
    for(int curIndex = 0; curIndex < vmOptionCount; curIndex ++) {
        JavaVMOption opt;
		opt.optionString = vmOptions[curIndex];
		vmOption[curIndex] = opt;
    }

    JavaVMInitArgs vmInitArgs;
	vmInitArgs.version = JNI_VERSION_1_8;
	vmInitArgs.options = vmOption;
	vmInitArgs.nOptions = vmOptionCount;
	//忽略无法识别jvm的情况
	vmInitArgs.ignoreUnrecognized = JNI_TRUE;
	
	/*
	* 这里的异常是JVM主动生成，不需要理会它。
	* 
	* SEGV（或异常0xC0000005）是在JVM启动时有意生成的，以验证某些CPU / OS功能。
	* 某些操作系统或虚拟机管理程序存在一个错误，即 AVX 寄存器在信号处理后无法恢复。
 	* 因此，JVM需要检查是否是这种情况,因此，它通过写入零地址生成异常，然后处理它。
	*/
	jint jvmProc = (jvmProcAddress)(&jvm, (void**)&env, &vmInitArgs);
	if (jvmProc < 0 || jvm == NULL || env == NULL) {
		FreeLibrary(jvmDLL);
		jvmDLL = NULL;
		return -3;
	}

    // 查找和加载MainClass
    // 支持JPMS体系的Java可以直接读取到指定的Class（只要该Class是可读的）
    jclass clsClass = (env)->FindClass("java/lang/Class");
    jmethodID forName = (env)->GetStaticMethodID(clsClass, "forName", "(Ljava/lang/String;)Ljava/lang/Class;");
    jstring clsName = (env)->NewStringUTF(mainClass);
    jclass mainClazz = (jclass)env->CallStaticObjectMethod(clsClass, forName, clsName);
    
    if (mainClazz == NULL) {
        // 如果正确指定了ClassPath或者ModulePath，依然无法加载Class，那么可能是一个低版本的Java。
        // 尝试通过SystemClassLoader加载Class
        jclass clClass = env->FindClass("java/lang/ClassLoader");
        jmethodID getSystemCL = env->GetStaticMethodID(
            clClass,
            "getSystemClassLoader",
            "()Ljava/lang/ClassLoader;"
        );
        jobject sysCL = env->CallStaticObjectMethod(clClass, getSystemCL);
        jmethodID loadClass = env->GetMethodID(
            clClass,
            "loadClass",
            "(Ljava/lang/String;)Ljava/lang/Class;"
        );

        jstring clsName = (env)->NewStringUTF(mainClass);
        mainClazz = (jclass)env->CallObjectMethod(sysCL, loadClass, clsName);
    }

	if (mainClazz == NULL) {
		return -4;
	}
    
	// 查找Main方法
	jmethodID init = env->GetStaticMethodID(mainClazz, "main", "([Ljava/lang/String;)V");
	if (init == NULL) {
		return -5;
	}

    jobjectArray params = env->NewObjectArray(argC, env->FindClass("java/lang/String"),NULL);
    for(int curIndex = 0; curIndex < argC; curIndex ++) {
        jstring mainParam = env->NewStringUTF(argv[curIndex]);
        env->SetObjectArrayElement(params, curIndex, mainParam);
    }
    env->CallStaticVoidMethod(mainClazz, init, params);
    jvm->DestroyJavaVM();
	return 0;

}

#if !defined(_WIN32) && !defined(__MINGW32__)
Handler LoadLibrary(const char* path) {
    return dlopen(path,RTLD_LAZY);
}

void* GetProcAddress(Handler lib,const char* funcName) {
    return dlsym(lib, funcName);
}

void FreeLibrary(Handler lib) {
    dlclose(lib);
}
#endif