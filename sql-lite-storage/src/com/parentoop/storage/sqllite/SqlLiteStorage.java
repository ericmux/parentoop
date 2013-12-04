package com.parentoop.storage.sqllite;

import com.parentoop.slave.api.SlaveStorage;
import com.parentoop.storage.sqllite.result.ResultIterable;
import com.parentoop.storage.sqllite.result.ResultIterator;
import com.parentoop.storage.utils.SerializableConverter;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class SqlLiteStorage<T extends Serializable> implements SlaveStorage<T> {

    private final static String DRIVER_CLASS = org.sqlite.JDBC.class.getName();
    private final static String SERVER_ADDRESS = "jdbc:sqlite:slave.db";

    private /* final */ Connection mConnection;
    private /* final */ SqlQueryHelper mQueryHelper;

    @Override
    public void initialize() throws Exception {
        if (mConnection != null) return;
        Class.forName(DRIVER_CLASS);
        mConnection = DriverManager.getConnection(SERVER_ADDRESS);
        mQueryHelper = new SqlQueryHelper(mConnection);
//        dropSchema(); // TODO: Comment after debugging
        createSchema();
    }

    // TODO: Choose proper indexes
    // TODO: Externalize table and column names
    private static final String CREATE_KEYS_TABLE_QUERY =
            "CREATE TABLE IF NOT EXISTS keys (\n" +
            "    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,\n" +
            "    `key` TEXT NOT NULL\n" +
            ");";

    private static final String CREATE_DATA_TABLE_QUERY =
            "CREATE TABLE IF NOT EXISTS data (\n" +
            "    `key_id` INT NOT NULL,\n" +
            "    `value` BLOB NOT NULL,\n" +
            "    CONSTRAINT `data_key`\n" +
            "        FOREIGN KEY (`key_id`)\n" +
            "        REFERENCES keys(`id`)\n" +
            "        ON DELETE CASCADE\n" +
            "        ON UPDATE CASCADE\n" +
            ");";

    private void createSchema() throws SQLException {
        mQueryHelper.update(CREATE_KEYS_TABLE_QUERY).close();
        mQueryHelper.update(CREATE_DATA_TABLE_QUERY).close();
    }

    private static final String DROP_KEYS_TABLE_QUERY = "DROP TABLE IF EXISTS keys";

    private static final String DROP_DATA_TABLE_QUERY = "DROP TABLE IF EXISTS data";

    private void dropSchema() throws SQLException {
        mQueryHelper.update(DROP_KEYS_TABLE_QUERY).close();
        mQueryHelper.update(DROP_DATA_TABLE_QUERY).close();
    }

    @Override
    public void terminate()  {
        try {
            if (mConnection == null) return;
            mQueryHelper.closeAll();
            dropSchema();
            mConnection.close();
            mConnection = null;
        } catch (SQLException e) {
            AssertionError error = new AssertionError();
            error.initCause(e);
            throw error;
        }

        // TODO: Clean up .db file
    }

    private static final String SELECT_KEY_ID_QUERY = "SELECT `id` FROM keys WHERE keys.`key` = ?";


    private int selectOrCreateKeyId(String key) throws SQLException, IOException {
        ResultSet result = mQueryHelper.get(SELECT_KEY_ID_QUERY, key);
        if (!result.next()) {
            mQueryHelper.close();
            createKey(key);
            return selectOrCreateKeyId(key);
        }
        int id = result.getInt("id");
        mQueryHelper.close();
        return id;
    }

    private static final String INSERT_KEY_QUERY = "INSERT INTO keys (`key`) VALUES (?)";

    private void createKey(String key) throws SQLException, IOException {
        mQueryHelper.update(INSERT_KEY_QUERY, key).close();
    }

    private static final String INSERT_VALUE_QUERY = "INSERT INTO data (`key_id`, `value`) VALUES (?, ?)";

    private Map<String, Integer> mKeysMemoize = new HashMap<>();

    @Override
    public void insert(String key, T value) {
        try {
            int id;
            if (!mKeysMemoize.containsKey(key)) {
                id = selectOrCreateKeyId(key);
                mKeysMemoize.put(key, id);
            } else {
                id = mKeysMemoize.get(key);
            }
            byte[] bytes = SerializableConverter.toByteArray(value);
            mQueryHelper.update(INSERT_VALUE_QUERY, id, (Object) bytes).close();
        } catch (SQLException | IOException e) {
            try { terminate(); } catch (Exception exc) { /* No-op */ }
            AssertionError error = new AssertionError();
            error.initCause(e);
            throw error;
        }
    }

    private static final String SELECT_DATA_BY_KEY_QUERY =
            "SELECT keys.`key`, data.`value` FROM keys" +
            "    INNER JOIN data" +
            "        ON data.`key_id` = keys.`id`" +
            "    WHERE keys.`key` = ?";

    @Override
    public Iterable<T> read(String key) {
        try {
            ResultSet result = mQueryHelper.get(SELECT_DATA_BY_KEY_QUERY, key);
            return new ResultIterable<>(new ResultIterator<T>(result));
        } catch (SQLException | IOException e) {
            try { terminate(); } catch (Exception exc) { /* No-op */ }
            AssertionError error = new AssertionError();
            error.initCause(e);
            throw error;
        }
    }

}