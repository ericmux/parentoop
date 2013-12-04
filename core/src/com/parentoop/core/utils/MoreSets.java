package com.parentoop.core.utils;

import java.util.*;

public class MoreSets {

    public static  <T> List<Set<T>> disjointSubsets(Collection<T> collection, int numSets) {
        int setSize = collection.size() / numSets;
        int extraElms = collection.size() % numSets;

        List<Set<T>> sets = new ArrayList<>(numSets);
        Set<T> set = null;
        for (T item : collection) {
            if (set == null) {
                set = new HashSet<>();
                sets.add(set);
            }
            set.add(item);
            int currSize = set.size();
            if (currSize >= setSize) {
                if (currSize == setSize && extraElms > 0) extraElms--;
                else set = null;
            }
        }
        while (sets.size() < numSets) sets.add(new HashSet<T>());
        return sets;
    }
}
