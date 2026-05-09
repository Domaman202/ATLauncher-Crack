package ru.DmN.atlcrack2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Bootstrap {
    public static void main(String[] args) throws IOException, URISyntaxException {
        Path atlJar = new File("ATLauncher.jar").toPath();

        if (!Files.exists(atlJar)) {
            System.out.println("Downloading ATLauncher... Please wait!");
            try (ReadableByteChannel rbc = Channels.newChannel(
                    URI.create("https://atlauncher.com/download/jar").toURL().openStream());
                 FileOutputStream fos = new FileOutputStream(atlJar.toFile())) {
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            }
        }

        Path currentJar = new File(Bootstrap.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toPath();

        String classpath = currentJar + File.pathSeparator + atlJar.normalize();

        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            javaBin += ".exe";
        }

        List<String> command = new ArrayList<>();
        command.add(javaBin);
        command.add("-cp");
        command.add(classpath);
        command.add("ru.DmN.atlcrack2.Main");
        command.addAll(Arrays.asList(args));

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.inheritIO();
        System.out.println("Restarting application...");
        pb.start();

        System.exit(0);
    }
}