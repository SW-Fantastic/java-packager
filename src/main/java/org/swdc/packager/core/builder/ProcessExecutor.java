package org.swdc.packager.core.builder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ProcessExecutor {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    protected String execute(List<String> cmds) {
        try {
            ProcessBuilder pb = new ProcessBuilder();
            pb.command(cmds);
            pb.redirectErrorStream(true);
            Process process = pb.start();
            InputStream inputStream = process.getInputStream();
            StringBuilder out = new StringBuilder();
            Thread.ofVirtual().start(() -> {
                try {
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    byte[] buffer = new byte[1024];
                    int read;
                    while ((read = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, read);
                    }
                    out.append(outputStream.toString(StandardCharsets.UTF_8));
                } catch (Exception e) {
                    logger.error("Error while linking runtime", e);
                }
            });
            process.waitFor();
            return out.toString();
        } catch (Exception e) {
            logger.error("Error while linking runtime", e);
            return "";
        }
    }

}
