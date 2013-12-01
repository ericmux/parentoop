package com.parentoop.storage.sqllite;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;

public class SqlLiteStorageTest {

    private SqlLiteStorage mStorage;

    @Before
    public void setUp() throws Exception {
        mStorage = new SqlLiteStorage();
        mStorage.initialize();
    }

    @After
    public void tearDown() throws Exception {
        mStorage.terminate();
    }

    @Test
    public void testInsertAndRetrieveCommonTypeValues() throws Exception {
        assertInsertAndRetrieve("a", "String", String.class);
        assertInsertAndRetrieve("b", 10, Integer.class);
        assertInsertAndRetrieve("c", new ArrayList<>(Arrays.asList(1, 2, 3, 4)), ArrayList.class);
        assertInsertAndRetrieve("d", new ArrayList<>(Arrays.asList("one", "two")), ArrayList.class);
        assertInsertAndRetrieve("e", 3.14, Double.class);
        assertInsertAndRetrieve("f", true, Boolean.class);
    }

    private <T extends Serializable> void assertInsertAndRetrieve(String key, T value, Class<? extends T> clazz) {
        mStorage.insert(key, value);
        Object retrieved = mStorage.read(key).iterator().next();
        assertThat(retrieved, instanceOf(clazz));
        assertEquals(value, retrieved);
    }

    @Test
    public void testInsertAndRetrieveCustomType() throws Exception {
        assertInsertAndRetrieve("a", new CustomSerializableObject(10, "ten"), CustomSerializableObject.class);
    }

    @Test
    public void testWipeDataAfterTerminating() throws Exception {
        assertFalse(mStorage.read("key").iterator().hasNext());
        mStorage.insert("key", 10);
        assertTrue(mStorage.read("key").iterator().hasNext());
        mStorage.terminate();
        mStorage.initialize();
        assertFalse(mStorage.read("key").iterator().hasNext());
    }

    @Test
    public void testReadMultipleValues() throws Exception {
        Collection<? extends Serializable> values = Arrays.asList("a", 2, 0.5, true);
        for (Serializable value : values) {
            mStorage.insert("key", value);
        }
        int i = 0;
        for (Object value : mStorage.read("key")) {
            assertTrue(values.contains(value));
            i++;
        }
        assertEquals(values.size(), i);
    }

}
