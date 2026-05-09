package ru.DmN.atlcrack2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;

public class Bootstrap {
    public static void main(String[] args) throws IOException, ClassNotFoundException, InvocationTargetException, IllegalAccessException, NoSuchMethodException, URISyntaxException {
        Path fileATL = new File("ATLauncher.jar").toPath();

        if (!Files.exists(fileATL)) {
            System.out.println("Downloading ATLauncher... Please wait!");
            try (ReadableByteChannel rbc = Channels.newChannel(URI.create("https://atlauncher.com/download/jar").toURL().openStream());
                 FileOutputStream fos = new FileOutputStream(fileATL.toFile())) {
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            }
        }

        System.out.println("Starting ATLauncher...");
        try (URLClassLoader loader = new BootstrapClassLoader(new URL[]{Bootstrap.class.getProtectionDomain().getCodeSource().getLocation().toURI().toURL(), fileATL.normalize().toUri().toURL()}, ClassLoader.getSystemClassLoader())) {
            Thread.currentThread().setContextClassLoader(loader);
            Class<?> clazz = loader.loadClass("ru.DmN.atlcrack2.Main");
            Method method = clazz.getMethod("main", String[].class);
            method.invoke(null, (Object) args);
        }
    }
}
