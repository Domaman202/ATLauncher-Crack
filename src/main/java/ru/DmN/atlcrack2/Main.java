package ru.DmN.atlcrack2;

import com.atlauncher.App;
import com.atlauncher.Network;
import com.atlauncher.data.MicrosoftAccount;
import com.atlauncher.managers.AccountManager;
import sun.misc.Unsafe;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;

public class Main {
    public static void main(String[] args) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        initAppWorkdir();
        fixUpdate();
        loadAppSettings();
        initProxy();
        runAppMain(args);
        loadOrInputUsername();
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

    private static void loadOrInputUsername() {
        File username = new File("username.txt");
        if (username.exists()) {
            try (FileInputStream fis = new FileInputStream("username.txt")) {
                byte[] bytes = new byte[fis.available()];
                fis.read(bytes);
                addAccount(new String(bytes, StandardCharsets.UTF_8));
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        } else {
            UsernameDialog.showDialog();
        }
    }

    public static void addAccount(String username) {
        App.settings.lastAccount = username;
        MicrosoftAccount account;
        try {
            Field unsafeF = Unsafe.class.getDeclaredField("theUnsafe");
            unsafeF.setAccessible(true);
            Unsafe unsafe = (Unsafe) unsafeF.get(null);
            account = (MicrosoftAccount) unsafe.allocateInstance(MicrosoftAccount.class);
            FakeAccount fake = new FakeAccount(username);
            for (Field f : FakeAccount.class.getFields()) {
                if (!Modifier.isStatic(f.getModifiers()) && !f.getType().isPrimitive()) {
                    unsafe.putObjectVolatile(account, unsafe.objectFieldOffset(f), unsafe.getObjectVolatile(fake, unsafe.objectFieldOffset(f)));
                }
            }
            Field mustLogin = FakeAccount.class.getField("mustLogin");
            unsafe.putBooleanVolatile(account, unsafe.objectFieldOffset(mustLogin), unsafe.getBooleanVolatile(fake, unsafe.objectFieldOffset(mustLogin)));
        } catch (NoSuchFieldException | IllegalAccessException | InstantiationException ex) {
            throw new RuntimeException(ex);
        }
        AccountManager.addAccount(account);
    }
}
