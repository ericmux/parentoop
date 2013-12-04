package com.parentoop.slave;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class Main {

    // Prevents instantiation
    private Main() {
        throw new AssertionError("Cannot instantiate object from " + this.getClass());
    }

    public static void main(String[] args) throws Exception {
        List<Integer> list = Arrays.asList(1, 2, 3, 4);
        Iterator<Integer> i;
        for (i = list.iterator(); i.hasNext(); ) {
            int v = i.next();
            if (v == 3) list.add(5);
        }
        i.next();

    }

}
