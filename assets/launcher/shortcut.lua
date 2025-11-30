-- 分割字符串的函数，将source字符串按照指定分隔符进行分割。
-- @param source 需要分割的字符串
-- @param sp     分隔符
-- @return 字符串数组
function splitString(source, sp) 

    local results = {}
    local current = 0
    local next = 0

    if source == nil or sp == nil then
        return results
    end

    while true do
        -- 查找分隔符位置
        next = string.find(source,sp,current + 1)
        if next ~= nil then
            -- 找到分隔符，分割字符串并保存到数组中
            table.insert(results, string.sub(source, current + 1, next - 1))
        else
            -- 没有找到分隔符，分割最后一个字符串并保存到数组中
            table.insert(results, string.sub(source, current + 1))
            return results; 
        end
        -- 更新当前位置
        current = next
    end
end

-- 创建多级目录
-- @param path 需要创建的目录路径
-- @return 创建的目录属性
function createDirs(path) 

    local sp = nil
    if getSystemName() == "windows" then 
        sp = '\\'
    else 
        sp = '/'
    end

    local pathParts = splitString(path, sp)
    local currentPath = ""
    for key,val in ipairs(pathParts) do
        -- 拼接当前路径部分
        currentPath = currentPath .. val
        -- 如果当前路径不存在，则创建目录
        if lfs.attributes(currentPath) == nil then
            lfs.mkdir(currentPath)
        end
        -- 拼接分隔符，用于下一次路径部分拼接
        currentPath = currentPath .. sp
    end
    -- 返回创建的目录属性
    return lfs.attributes(path);
end

-- 为Linux创建应用图标
function createShortcut()
    
    if getSystemName() ~= "linux" then
        -- 非Linux系统，不创建快捷方式
    	print("not linux system, skipped")
        return
    end

    -- 创建快捷方式目录
    local path = getHomeDir() .. "/.local/share/applications/";
    local folder = createDirs(path);
    if folder == nil then
        -- 创建目录失败，退出
    	print("no such folder" .. path);
        return
    end

    -- 创建快捷方式文件内容
    local desktopFile = "[Desktop Entry]\n"
    desktopFile = desktopFile .. "Name=" .. getAppName() .. "\n"
    desktopFile = desktopFile .. "Type=Application\n"
    desktopFile = desktopFile .. "Exec=" .. getAppLocation() .. "\n"
    desktopFile = desktopFile .. "Icon=" .. getAppDir() .. "/icon.png\n"
    desktopFile = desktopFile .. "Terminal=" .. (isGui() and "true" or "false") .. "\n"

    -- 写入快捷方式文件
    local desktopPath = path .. getAppName() .. ".desktop"
    local targetFile = io.open(desktopPath, "wb");
    print("writing : " .. desktopPath)
    if targetFile then
        targetFile:write(desktopFile)
        targetFile:close()
        print("desktop shortcut has updated.")
    else
        print("Can not open desktop file, failed to write application shortcut.")
    end


end

-- 为Linux应用创建快捷方式，因为Linux系统的软件不像windows那样，可以方便的附带图标，
-- 所以为了方便用户和系统将它识别为软件，需要在特定位置创建一个快捷方式。
createShortcut()