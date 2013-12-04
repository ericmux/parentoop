package com.parentoop.core.utils;

import org.junit.BeforeClass;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DisjointSubsetsTest {

    private static List<Integer> LIST0;
    private static List<Integer> LIST10;
    private static List<Integer> LIST30;

    @BeforeClass
    public static void initLists() {
        LIST0 = Collections.emptyList();
        LIST10 = newAscendingList(10);
        LIST30 = newAscendingList(30);
    }

    private static List<Integer> newAscendingList(int size) {
        List<Integer> list = new ArrayList<>(size);
        for (int i = 0; i < size; i++) list.add(i);
        return Collections.unmodifiableList(list);
    }

    @Test
    public void testNumberOfSets() {
        assertEquals(10, MoreSets.disjointSubsets(LIST0, 10).size());
        assertEquals(20, MoreSets.disjointSubsets(LIST10, 20).size());
        assertEquals(42, MoreSets.disjointSubsets(LIST30, 42).size());
        assertEquals(13, MoreSets.disjointSubsets(LIST30, 13).size());
        assertEquals(3, MoreSets.disjointSubsets(LIST10, 3).size());
        assertEquals(51, MoreSets.disjointSubsets(LIST10, 51).size());
        assertEquals(1, MoreSets.disjointSubsets(LIST0, 1).size());
    }

    @Test
    public void testDisjointProperty() {
        testSetsDisjoint(MoreSets.disjointSubsets(LIST30, 7));
        testSetsDisjoint(MoreSets.disjointSubsets(LIST30, 8));
        testSetsDisjoint(MoreSets.disjointSubsets(LIST30, 83));
        testSetsDisjoint(MoreSets.disjointSubsets(LIST10, 3));
        testSetsDisjoint(MoreSets.disjointSubsets(LIST10, 4));
        testSetsDisjoint(MoreSets.disjointSubsets(LIST10, 11));
    }

    @Test
    public void testSetsUnionMaintained() {
        assertTrue(setsUnion(MoreSets.disjointSubsets(LIST30, 8)).containsAll(LIST30));
        assertTrue(setsUnion(MoreSets.disjointSubsets(LIST30, 5)).containsAll(LIST30));
        assertTrue(setsUnion(MoreSets.disjointSubsets(LIST30, 31)).containsAll(LIST30));
        assertTrue(setsUnion(MoreSets.disjointSubsets(LIST10, 9)).containsAll(LIST10));
        assertTrue(setsUnion(MoreSets.disjointSubsets(LIST10, 2)).containsAll(LIST10));
        assertTrue(setsUnion(MoreSets.disjointSubsets(LIST10, 17)).containsAll(LIST10));
    }

    private <T> void testSetsDisjoint(Collection<Set<T>> sets) {
        for (Set<T> set1 : sets) {
            for (Set<T> set2 : sets) {
                if (set1 == set2) continue;
                assertFalse(set1.removeAll(set2));
            }
        }
    }

    private <T> Set<T> setsUnion(Collection<Set<T>> sets) {
        Set<T> union = new HashSet<>();
        for (Set<T> set : sets) union.addAll(set);
        return union;
    }
}
