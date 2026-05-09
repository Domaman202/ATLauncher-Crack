package ru.DmN.atlcrack2;

import java.net.URL;
import java.net.URLClassLoader;

public class BootstrapClassLoader extends URLClassLoader {
    public BootstrapClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> c = findLoadedClass(name);
        if (c != null)
            return c;
        try {
            c = findClass(name);
            if (resolve)
                resolveClass(c);
            return c;
        } catch (ClassNotFoundException e) {
            return super.loadClass(name, resolve);
        }
    }
}
