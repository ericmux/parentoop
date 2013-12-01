package com.parentoop.storage.sqllite;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

// TODO: Logger
/* package private */ class SqlQueryHelper {

    private final Connection mConnection;
    // Any better ideas?
    private final Stack<Statement> mStatementStack = new Stack<>();

    public SqlQueryHelper(Connection connection) {
        mConnection = connection;
    }

    public void close() {
        try {
            mStatementStack.pop().close();
        } catch (SQLException e) {
            AssertionError error = new AssertionError();
            error.initCause(e);
            throw error;
        }
    }

    public void closeAll() {
        while (!mStatementStack.isEmpty()) close();
    }

    public SqlQueryHelper update(String query) throws SQLException {
        Statement statement = mConnection.createStatement();
        mStatementStack.push(statement);
        statement.executeUpdate(query);
        return this;
    }

    public SqlQueryHelper update(String query, Object first, Object... values) throws SQLException, IOException {
        // If trying to access ResultSet after closing statement throws error, use a statement stack
        List<Object> params = new ArrayList<>(Arrays.asList(values));
        params.add(0, first);
        PreparedStatement statement = prepareStatement(query, params.toArray());
        statement.executeUpdate();
        return this;
    }

    public ResultSet get(String query, Object... values) throws SQLException, IOException {
        // If trying to access ResultSet after closing statement throws error, use a statement stack
        PreparedStatement statement = prepareStatement(query, values);
        return statement.executeQuery();
    }

    private PreparedStatement prepareStatement(String query, Object... values) throws SQLException, IOException {
        PreparedStatement statement = mConnection.prepareStatement(query);
        mStatementStack.push(statement);
        int i = 1;
        for (Object value : values) {
            if (value instanceof String) {
                statement.setString(i, (String) value);
            } else if (value instanceof Integer) {
                statement.setInt(i, (int) value);
            } else if (value instanceof byte[]) {
                statement.setBytes(i, (byte[]) value);
            } else {
                String type = value.getClass().getSimpleName();
                throw new IllegalArgumentException("Value type " + type + " not supported in query");
            }
            i++;
        }
        return statement;
    }

}
