package com.parentoop.core.data;

import java.io.Serializable;

public final class Datum implements Serializable {

    private final String key;
    private final Serializable value;

    public Datum(String key, Serializable value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public Serializable getValue() {
        return value;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof Datum)) return false;
        Datum datum = (Datum) object;
        if (!key.equals(datum.key)) return false;
        if (!value.equals(datum.value)) return false;
        return true;
    }

    @Override
    public int hashCode() {
        int result = key.hashCode();
        result = 31 * result + value.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return key + " : " + value;
    }
}
