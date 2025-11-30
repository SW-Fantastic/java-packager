# 指定Cmake的版本
cmake_minimum_required(VERSION 3.15)
project(JavaLauncher VERSION 1.0 DESCRIPTION "JavaLauncher")

# 枚举“external/lua/*.c”并且装入列表LUA_SOURCE（变量）中。
file(GLOB LUA_SOURCES src/external/lua/*.c)
# 编译为lua_static的静态库，使用LUA_SOURCES作为源码的列表
add_library(lua_static STATIC ${LUA_SOURCES})
# 编译一个lualfs提供基本的文件操作能力
add_library(lua_lfs_static STATIC src/external/lualfs/lfs.c)
# 编译Launcher，通过指定的源码列表生成目标文件。
add_executable(launcher src/external/cJSON.c src/external/whereami.c src/external/cJSON_Utils.c src/Platform.cpp src/Launcher.cpp)
if(WIN32)

    # windows下需要一个无命令行窗口的形式，用于GUI应用。
    add_executable(launcherW src/external/cJSON.c src/external/whereami.c src/external/cJSON_Utils.c src/Platform.cpp src/Launcher.cpp)
    set_target_properties(launcherW PROPERTIES
        RUNTIME_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/bin
        LINK_FLAGS "/SUBSYSTEM:WINDOWS /ENTRY:mainCRTStartup"
    )
    # 为Launcher链接Lua引擎
    target_link_libraries(launcherW lua_static)
    target_link_libraries(launcherW lua_lfs_static)

    # windows下需要编译NativeHandler用于添加exe的图标、
    add_library(NativeHandler SHARED src/WinNativeHandler.cpp)
    set_target_properties(NativeHandler PROPERTIES
        RUNTIME_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/bin
        OUTPUT_NAME "NativeHandler"
    )

endif()

# 为Launcher链接Lua引擎
target_link_libraries(launcher lua_static)
target_link_libraries(launcher lua_lfs_static)
set_target_properties(launcher PROPERTIES
    RUNTIME_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/bin
    OUTPUT_NAME "launcher"
)


