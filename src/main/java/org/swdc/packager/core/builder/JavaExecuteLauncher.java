package org.swdc.packager.core.builder;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.swdc.packager.core.PackageProject;
import org.swdc.packager.core.entity.JavaEnvironment;
import org.swdc.packager.views.ModEntity;

import java.io.File;
import java.io.InputStream;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class JavaExecuteLauncher {

    private Consumer<String> wirter;

    private File workingDir;

    private PackageProject project;

    private Process launched;

    private static final Logger logger = LoggerFactory.getLogger(JavaExecuteLauncher.class);

    public JavaExecuteLauncher(PackageProject project, File workingDir, Consumer<String> wirter) {

        this.wirter = wirter;
        this.workingDir = workingDir;
        this.project = project;

    }

    public void execute() {

        JavaEnvironment environment = project.getJavaEnvironment();

        String execPath = environment.getPath() +
                File.separator  + "bin" +
                File.separator + "java";

        String subfix = "";
        String osName = System.getProperty("os.name").trim().toLowerCase();
        if (osName.startsWith("win")) {
            subfix = ".exe";
        }

        File executable = new File(execPath + subfix);
        if (!executable.exists()) {
            wirter.accept("Java runtime not found");
            return;
        }

        List<String> params = new ArrayList<>();
        params.add(executable.getAbsolutePath());
        ProcessBuilder builder = new ProcessBuilder();
        if (project.getMainModule() != null && !project.getMainModule().isBlank()) {

            // 按照JPMS方式启动
            List<String> modules = project.getSystemModules();
            List<ModEntity> deps = project.getDependencies().stream()
                    .filter(modEntity -> modEntity.isEnabled() && modEntity.getFileName().endsWith("jar"))
                    .collect(Collectors.toList());

            StringJoiner modulesPath = new StringJoiner(File.pathSeparator);

            for (ModEntity mod : deps) {
                File file = new File(mod.getOriginalFilePath());
                ModuleFinder finder = ModuleFinder.of(file.toPath());
                ModuleReference reference = finder.findAll().stream().findFirst().orElse(null);
                if (reference != null) {
                    String name = reference.descriptor().name();
                    if (name != null) {
                        modules.add(name);
                        modulesPath.add(file.getAbsolutePath());
                    }
                }
            }

            params.add("--module-path");
            params.add(modulesPath.toString());
            params.add("--add-modules");
            StringJoiner joiner = new StringJoiner(",");
            for (String module : modules) {
                joiner.add(module);
            }
            params.add(joiner.toString());
            params.add("-m");
            params.add(project.getMainModule() + "/" + project.getMainClass());

        } else {

            // 按照传统方式启动
            StringJoiner modulesPath = new StringJoiner(File.pathSeparator);
            List<ModEntity> deps = project.getDependencies().stream()
                    .filter(modEntity -> modEntity.isEnabled() && modEntity.getFileName().endsWith("jar"))
                    .collect(Collectors.toList());

            for (ModEntity mod : deps) {
                File file = new File(mod.getOriginalFilePath());
                ModuleFinder finder = ModuleFinder.of(file.toPath());
                ModuleReference reference = finder.findAll().stream().findFirst().orElse(null);
                if (reference != null) {
                    String name = reference.descriptor().name();
                    if (name != null) {
                        modulesPath.add(file.getAbsolutePath());
                    }
                }
            }
            params.add("-Djava.class.path=" + modulesPath);
            params.add(project.getMainClass());

        }

        project.getVmOptions().forEach(vmOption -> {
            if (vmOption.getOptValue() != null && !vmOption.getOptValue().isBlank()) {
                params.add(vmOption.getOptName() + "=" + vmOption.getOptValue());
            } else {
                params.add(vmOption.getOptName());
            }
        });

        builder.directory(workingDir);
        builder.redirectErrorStream(true);
        builder.command(params);
        try {
            launched = builder.start();
            Thread.ofVirtual().start(() -> {
                InputStream is = launched.getInputStream();
                try {
                    byte[] buffer = new byte[1024];
                    int read;
                    while ((read = is.read(buffer)) != -1) {
                        String line = getString(buffer,read);
                        wirter.accept(line);
                    }
                } catch (Exception e) {
                    logger.error("Error while reading process output", e);
                }
            });
            launched.waitFor();
            wirter.accept("Process finished");
        } catch (Exception e) {
            wirter.accept("Failed to start Java process");
        }

    }

    private String getString(byte[] data, int len) {
        CharsetDetector detector = new CharsetDetector();
        detector.setText(data);
        detector.setDeclaredEncoding(null);
        CharsetMatch match = detector.detect();
        String detectedEncoding = match.getName();
        if (detectedEncoding.contains("UTF") || detectedEncoding.contains("utf")) {
            detectedEncoding = "UTF-8";
        }
        return new String(data,0, len, Charset.forName(detectedEncoding));
    }

    public void stop() {
        if (launched != null) {
            launched.destroyForcibly();
        }
    }

}
