package org.swdc.packager.core.builder;

import org.slf4j.Logger;
import org.swdc.packager.core.FileUtils;
import org.swdc.packager.core.PackageProject;
import org.swdc.packager.core.entity.JavaEnvironment;
import org.swdc.packager.core.entity.JavaFXEnvironment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class JavaRuntimeLinker {

    private JavaEnvironment environment;

    private JavaFXEnvironment fxEnvironment;

    private List<String> modules;

    private static Logger logger = org.slf4j.LoggerFactory.getLogger(JavaRuntimeLinker.class);

    public JavaRuntimeLinker(PackageProject packageProject) {
        this.environment = packageProject.getJavaEnvironment();
        this.modules = packageProject.getSystemModules();
        this.fxEnvironment = packageProject.getJavaFxEnvironment();
    }

    public boolean link(File targetDir) {

        String linkerPath = environment.getPath() +
                File.separator  + "bin" +
                File.separator + "jlink";

        if (targetDir.exists()) {
            try {
                FileUtils.deleteFolder(targetDir);
            } catch (Exception e) {
                logger.error("Error while deleting target dir", e);
            }
        }

        String subfix = "";
        String osName = System.getProperty("os.name").trim().toLowerCase();
        if (osName.startsWith("win")) {
            subfix = ".exe";
        }
        File linker = new File(linkerPath + subfix);
        if (!linker.exists()) {
            try {
                File runtimeDir = new File(environment.getPath() + File.separator + "jre");
                if (runtimeDir.exists()) {
                    FileUtils.copyFolder(runtimeDir, targetDir);
                    return true;
                }
                return false;
            } catch (Exception e) {
                logger.error("Error while coping runtime", e);
                return false;
            }
        }

        List<String> params = new ArrayList<>();
        params.add(linkerPath);
        params.add("--output");
        params.add(targetDir.getAbsolutePath());
        if (fxEnvironment != null) {
            params.add("--module-path");
            params.add(fxEnvironment.getPath());
        }
        params.add("--add-modules");
        StringJoiner joiner = new StringJoiner(",");
        for (String module : modules) {
            joiner.add(module);
        }
        if (fxEnvironment != null) {
            File fxMods = new File(fxEnvironment.getPath());
            File[] mods = fxMods.listFiles();
            if (mods != null) {
                for (File mod : mods) {
                    if (!mod.getName().endsWith("jmod")) {
                        continue;
                    }
                    joiner.add(mod.getName().replace(".jmod", ""));
                }
            }
        }
        params.add(joiner.toString());

        ProcessBuilder jlink = new ProcessBuilder();
        jlink.command(params);
        jlink.redirectErrorStream(true);
        try {
            Process process = jlink.start();
            InputStream inputStream = process.getInputStream();
            StringBuilder exception = new StringBuilder();
            Thread.ofVirtual().start(() -> {
                try {
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int read;
                    while ((read = inputStream.read(buffer)) != -1) {
                       outputStream.write(buffer, 0, read);
                    }
                    exception.append(outputStream.toString(StandardCharsets.UTF_8));
                } catch (Exception e) {
                    logger.error("Error while linking runtime", e);
                }
            });
            process.waitFor();
            return exception.isEmpty();
        } catch (Exception e) {
            logger.error("Error while linking runtime", e);
            return false;
        }
    }

}
