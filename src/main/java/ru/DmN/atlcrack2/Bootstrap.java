package ru.DmN.atlcrack2;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.*;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Bootstrap {
    public static void main(String[] args) throws IOException, URISyntaxException, InvocationTargetException, IllegalAccessException, ClassNotFoundException, NoSuchMethodException {
        if (args.length == 1 && Objects.equals(args[0], "run")) {
            try (CustomClassLoader loader = new CustomClassLoader()) {
                Thread.currentThread().setContextClassLoader(loader);
                Class<Main> main = (Class<Main>) loader.loadClass("ru.DmN.atlcrack2.Main");
                Method method = main.getMethod("main", String[].class);
                method.invoke(null, (Object) new String[0]);
                return;
            }
        }

        Path atlJar = new File("ATLauncher.jar").toPath();

        if (!Files.exists(atlJar)) {
            System.out.println("Downloading ATLauncher... Please wait!");

            URL url = URI.create("https://download.nodecdn.net/containers/atl/ATLauncher.jar").toURL();
            URLConnection urlConnection;

            if (checkIsRussianIP()) {
                System.out.println("Обнаружен Российский IP - подключаем вам прокси!");
                urlConnection = url.openConnection(createProxy());
            } else {
                System.out.println("Russian ip not detected - no proxy.");
                urlConnection = url.openConnection();
            }

            if (urlConnection instanceof HttpURLConnection) {
                urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
                urlConnection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8");
                urlConnection.setRequestProperty("Accept-Language", "ru-RU,ru;q=0.9,en-US;q=0.8,en;q=0.7");
                urlConnection.setRequestProperty("Accept-Encoding", "gzip, deflate, br");
                urlConnection.setRequestProperty("Connection", "keep-alive");
                urlConnection.setRequestProperty("Upgrade-Insecure-Requests", "1");
                urlConnection.setRequestProperty("Sec-Fetch-Dest", "document");
                urlConnection.setRequestProperty("Sec-Fetch-Mode", "navigate");
                urlConnection.setRequestProperty("Sec-Fetch-Site", "none");
                urlConnection.setRequestProperty("Sec-Fetch-User", "?1");
            }

            try (ReadableByteChannel rbc = Channels.newChannel(urlConnection.getInputStream());
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
        command.add("ru.DmN.atlcrack2.Bootstrap");
        command.add("run");
        command.addAll(Arrays.asList(args));

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.inheritIO();
        System.out.println("Restarting application...");
        pb.start();

        System.exit(0);
    }

    public static boolean checkIsRussianIP() {
        try {
            return new java.util.Scanner(new URL("http://ip-api.com/json/").openStream(), "UTF-8").useDelimiter("\\A").next().contains("\"countryCode\":\"RU\"");
        } catch (IOException ignored) {
            return false;
        }
    }

    public static Proxy createProxy() {
        return new Proxy(Proxy.Type.HTTP, new InetSocketAddress("193.35.17.153", 8888));
    }
}