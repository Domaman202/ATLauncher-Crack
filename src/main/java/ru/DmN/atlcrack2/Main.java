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
        try {
            if (new java.util.Scanner(new URL("http://ip-api.com/json/").openStream(), "UTF-8").useDelimiter("\\A").next().contains("\"countryCode\":\"RU\"")) {
                System.out.println("Обнаружен Российский IP - подключаем вам прокси!");
                Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("193.35.17.153", 8888));
                Network.CLIENT = Network.CLIENT.newBuilder().proxy(proxy).build();
                Network.GRAPHQL_CLIENT = Network.GRAPHQL_CLIENT.newBuilder().proxy(proxy).build();
                Network.CACHED_CLIENT = Network.CACHED_CLIENT.newBuilder().proxy(proxy).build();
                return;
            }
        } catch (IOException ignored) {
        }
        System.out.println("Russian ip not detected - no proxy.");
    }

    private static void runAppMain(String[] args) {
        App.main(args);
    }

    private static void showUsernameDialog() {
        UsernameDialog.showDialog();
    }
}
