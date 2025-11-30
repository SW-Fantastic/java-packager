package org.swdc.packager.core;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

public class FileUtils {

    static ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES);
    }

    private static native int updateExecutableIcon(String exePath, String[] iconPath);

    public static boolean updateExecutableIcon(File exe, List<File> icon, File original) {
        String osName = System.getProperty("os.name").trim().toLowerCase();
        if (osName.contains("win")) {
            System.load(new File("NativeHandler.dll").getAbsolutePath());
            String[] paths = icon.stream().map(File::getAbsolutePath).toArray(String[]::new);
            int rst = updateExecutableIcon(exe.getAbsolutePath(), paths);
            return rst == 0;
        } else if (osName.contains("linux")) {
            try {
                File parent = exe.getParentFile();
                resizeIconImage(original,new File(parent, "icon.png"), 128,128);
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    public static void resizeIconImage(File icon,File target, int width, int height) throws IOException {
        BufferedImage image = ImageIO.read(icon);
        BufferedImage generated = new BufferedImage(width, height, image.getType());
        Graphics2D g = generated.createGraphics();
        g.drawImage(image, 0, 0, width, height, null);
        g.dispose();
        try (OutputStream out = Files.newOutputStream(target.toPath())) {
            ImageIO.write(generated, "png", out);
        }
    }

    public static void copyFolder(File sourceFolder, File targetFolder) throws IOException {

        Path targetBase = targetFolder.toPath();
        Path sourceBase = sourceFolder.toPath();
        Files.walkFileTree(sourceBase, new FileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path folder = targetBase.resolve(sourceBase.relativize(dir)).normalize();
                if (!Files.exists(folder)) {
                    Files.createDirectories(folder);
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path target = targetBase.resolve(sourceBase.relativize(file)).normalize();
                Files.copy(file,target);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }
        });

    }

    public static void deleteFolder(File folder) throws IOException {
        Files.walkFileTree(folder.toPath(), new FileVisitor<>() {

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }


        });
    }

    public static void save(Object data, File target) {
        try {
            mapper.writerWithDefaultPrettyPrinter()
                    .writeValue(target, data);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T load(File source, Class<T> type) {
        try {
            return mapper.readValue(source, type);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T load(InputStream is, Class<T> type) {
        try {
            return mapper.readValue(is, type);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> List<T> loadAsList(InputStream is, Class<T> type) {
        try {
            JavaType theType = mapper.getTypeFactory().constructParametricType(List.class, type);
            return mapper.readValue(is, theType);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
