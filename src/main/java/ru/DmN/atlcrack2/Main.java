package ru.DmN.atlcrack2;

import com.atlauncher.App;
import com.atlauncher.Network;
import com.atlauncher.network.GraphqlClient;
import okhttp3.OkHttpClient;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;

public class Main {
    public static void main(String[] args) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        initAppWorkdir();
        loadAppSettings();
        initProxy();
        runAppMain(args);
        showUsernameDialog();
    }

    private static void initAppWorkdir() {
        App.workingDir = new File("ATL").toPath();
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
