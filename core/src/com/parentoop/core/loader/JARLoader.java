package com.parentoop.core.loader;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;

public class JarLoader {

    private URLClassLoader mClassLoader;

    public JarLoader(Path jarFile){
        URL url = null;
        try {
            url = jarFile.toUri().toURL();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        mClassLoader = new URLClassLoader(new URL[] { url });
    }

    public Class<?> loadClass(String className){
        try {
            return Class.forName(className, true, mClassLoader);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
