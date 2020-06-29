package no.nav.veilarbperson.utils;

import lombok.SneakyThrows;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class FileUtils {

    @SneakyThrows
    public static String getResourceFileAsString(String fileName) {
        ClassLoader classLoader = FileUtils.class.getClassLoader();
        try (InputStream resourceStream = classLoader.getResourceAsStream(fileName)) {
            return new String(resourceStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

}
