package fr.supermax_8.boostedaudio.sharedutils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class JavaUtils {


    public static File getCurrentJar() {
        try {
            String jarPath = JavaUtils.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            return new File(jarPath);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static File getCurrentDir() {
        return getCurrentJar().getParentFile();
    }

    public static void loadAllClasses(ClassLoader classLoader, File f) {
        for (String s : getClassesNames(f)) {
            try {
                classLoader.loadClass(s);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static List<String> getClassesNames(File f) {
        try {
            // Ouvrir le fichier JAR
            JarFile jar = new JarFile(f);
            Enumeration<JarEntry> entries = jar.entries();

            // Liste pour stocker les noms des classes
            List<String> classNames = new ArrayList<>();

            // Parcourir les entrées du fichier JAR
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();
                if (entryName.endsWith(".class")) {
                    // Ajouter le chemin de la classe à la liste
                    classNames.add(entryName);
                }
            }

            // Fermer le fichier JAR
            jar.close();
            return classNames;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}