package fr.supermax_8.boostedaudio.core.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class ResourceUtils {


    /**
     * Copies a resource from the JAR file to a specified location on the file system.
     *
     * @param resourcePath The path of the resource to copy from the JAR.
     * @param outputPath   The destination path where the resource should be saved.
     * @throws IOException If an error occurs while copying the resource.
     */
    public static void saveResource(String resourcePath, String outputPath) throws IOException {
        try (InputStream inputStream = ResourceUtils.class.getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IOException("The specified resource was not found: " + resourcePath);
            }

            Path destination = Path.of(outputPath);
            Files.createDirectories(destination.getParent());

            // Copy the resource to the destination location
            Files.copy(inputStream, destination, StandardCopyOption.REPLACE_EXISTING);
        }
    }


    /**
     * Returns an InputStream for a specified resource from the JAR.
     *
     * @param resourcePath The path of the resource to retrieve from the JAR.
     * @return An InputStream for the specified resource.
     * @throws IOException If the resource is not found or an error occurs while opening it.
     */
    public static InputStream getResourceAsStream(String resourcePath) throws IOException {
        InputStream inputStream = ResourceUtils.class.getResourceAsStream(resourcePath);
        if (inputStream == null) {
            throw new IOException("The specified resource was not found: " + resourcePath);
        }
        return inputStream;
    }



}