package ru.DmN.atlcrack2;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

public class CustomClassLoader extends URLClassLoader {
    public static final Map<String, String> WRAPPERS = new HashMap<>();

    public CustomClassLoader() throws MalformedURLException {
        super(new URL[]{new File("ATLauncher.jar").toURI().toURL()});
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (name.startsWith("java.") || name.startsWith("javax.") || name.startsWith("sun."))
            return super.loadClass(name, resolve);

        Class<?> clazz = this.findLoadedClass(name);
        if (clazz != null)
            return clazz;

        byte[] bytes;
        bytes = this.findClassWrapper(name);
        if (bytes == null)
            bytes = this.findClassFile(name);
        return this.defineClass(name, bytes, 0, bytes.length);
    }

    private byte[] findClassWrapper(String clazz) {
        String wrapper = WRAPPERS.get(clazz);
        if (wrapper == null)
            return null;
        try {
            return (byte[]) this.loadClass(wrapper).getMethod("dump").invoke(null);
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private byte[] findClassFile(String clazz) throws ClassNotFoundException {
        InputStream inputStream = this.getResourceAsStream(clazz.replace('.', '/') + ".class");
        if (inputStream == null)
            throw new ClassNotFoundException(clazz);
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try {
            byte[] data = new byte[8192];
            int nRead;
            while ((nRead = inputStream.read(data, 0, data.length)) != -1)
                buffer.write(data, 0, nRead);
            buffer.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return buffer.toByteArray();
    }

    static {
        WRAPPERS.put("com.atlauncher.Launcher", "asm.com.atlauncher.LauncherDump");
        WRAPPERS.put("com.atlauncher.data.MicrosoftAccount", "asm.com.atlauncher.data.MicrosoftAccountDump");
        WRAPPERS.put("com.atlauncher.utils.OS", "asm.com.atlauncher.utils.OSDump");
        WRAPPERS.put("com.atlauncher.managers.AccountManager", "asm.com.atlauncher.managers.AccountManagerDump");
    }
}
