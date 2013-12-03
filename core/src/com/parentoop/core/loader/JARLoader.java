package com.parentoop.core.loader;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JarLoader {

    private URLClassLoader mClassLoader;

    public JarLoader(String jarPath){
        Path path = Paths.get(jarPath);
        URL url = null;
        try {
            url = path.toUri().toURL();
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
