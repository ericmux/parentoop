package com.parentoop.core.loader;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JarLoader {

    private URLClassLoader mClassLoader;

    public JarLoader(String pathToJar){
        Path path = Paths.get(pathToJar);
        URL u = null;
        try {
            u = path.toUri().toURL();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        mClassLoader = new URLClassLoader(new URL[]{u});
    }

    public Class<?> loadClass(String className){

        Class clzz = null;
        try {
            clzz = Class.forName(className, true, mClassLoader);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return clzz;

    }
}
