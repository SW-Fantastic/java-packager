package org.swdc.packager.core.builder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

public class JavaVersionExtractor extends ProcessExecutor {

    private String javacPath;

    public JavaVersionExtractor(File javaHome) {
        this.javacPath = javaHome.getPath() + File.separator + "bin" + File.separator + "javac";
    }

    public String extractVersion() {

        try {
            ProcessBuilder builder = new ProcessBuilder(javacPath, "-version");
            builder.redirectErrorStream(true);

            String output = execute(Arrays.asList(
                    javacPath,
                    "-version"
            ));

            return output.trim().strip()
                    .replace("javac", "Java");
        } catch (Exception e) {
            return null;
        }

    }

}
