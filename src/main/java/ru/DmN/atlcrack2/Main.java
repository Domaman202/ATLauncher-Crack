package ru.DmN.atlcrack2;

import com.atlauncher.App;
import com.atlauncher.Network;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Proxy;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        initAppWorkdir();
        fixUpdate();
        loadAppSettings();
        initProxy();
        runAppMain(args);
        showUsernameDialog();
    }

    private static void initAppWorkdir() {
        String os = System.getProperty("os.name").toLowerCase();
        String basePath;
        if (os.contains("win"))
            basePath = System.getenv("APPDATA") != null ? System.getenv("APPDATA") : (System.getProperty("user.home") + File.separator + "AppData" + File.separator + "Roaming");
        else if (os.contains("mac"))
            basePath = System.getProperty("user.home") + File.separator + "Library" + File.separator + "Application Support";
        else
            basePath = System.getProperty("user.home") + File.separator + ".local/share";
        File atlDir = new File(basePath + File.separator + "atlauncher");
        App.workingDir = (atlDir.exists() ? atlDir : new File("ATL")).toPath();;
    }

    private static void fixUpdate() {
        App.noLauncherUpdate = true;
    }

    private static void loadAppSettings() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method method = App.class.getDeclaredMethod("loadSettings");
        method.setAccessible(true);
        method.invoke(null);
    }

    private static void initProxy() {
        if (Bootstrap.checkIsRussianIP()) {
            System.out.println("Обнаружен Российский IP - подключаем вам прокси!");
            Proxy proxy = Bootstrap.createProxy();
            Network.CLIENT = Network.CLIENT.newBuilder().proxy(proxy).build();
            Network.GRAPHQL_CLIENT = Network.GRAPHQL_CLIENT.newBuilder().proxy(proxy).build();
            Network.CACHED_CLIENT = Network.CACHED_CLIENT.newBuilder().proxy(proxy).build();
        } else {
            System.out.println("Russian ip not detected - no proxy.");
        }
    }

    private static void runAppMain(String[] args) {
        App.main(args);
    }

    private static void showUsernameDialog() {
        UsernameDialog.showDialog();
    }
}
