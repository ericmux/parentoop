package com.parentoop.storage.sqllite;

import java.io.Serializable;

/* package private */ class CustomSerializableObject implements Serializable {
    public final int integer;
    public final String string;

    public CustomSerializableObject(int integer, String string) {
        this.integer = integer;
        this.string = string;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof CustomSerializableObject)) return false;
        CustomSerializableObject that = (CustomSerializableObject) object;
        if (integer != that.integer) return false;
        if (string != null ? !string.equals(that.string) : that.string != null) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = integer;
        result = 31 * result + (string != null ? string.hashCode() : 0);
        return result;
    }
}
