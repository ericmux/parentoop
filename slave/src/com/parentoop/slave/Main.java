package com.parentoop.slave;

import com.parentoop.slave.application.SlaveApplication;
import com.parentoop.storage.api.SlaveStorage;

import java.util.ArrayList;
import java.util.Arrays;

public class Main {

    // Prevents instantiation
    private Main() {
        throw new AssertionError("Cannot instantiate object from " + this.getClass());
    }

    public static void main(String[] args) throws Exception {
        SlaveApplication.getInstance().initialize();
        SlaveApplication app = SlaveApplication.getInstance();
        SlaveStorage storage = app.getSlaveStorage();
        storage.insert("key-a", new ArrayList<>(Arrays.asList(1, 2, 3)));
        storage.insert("key-a", "Stringao!");
        storage.insert("key-a", true);
        storage.insert("b", "value serializable B!");
        for (Object obj : storage.read("key-a")) {
            System.out.println(obj.getClass().getSimpleName() + ": " + obj);
        }
        SlaveApplication.getInstance().terminate();
    }

}
