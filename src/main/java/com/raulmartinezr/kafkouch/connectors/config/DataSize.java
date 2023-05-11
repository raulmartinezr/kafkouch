package com.raulmartinezr.kafkouch.connectors.config;

public class DataSize {
    private final long byteCount;

    private DataSize(long byteCount) {
        this.byteCount = byteCount;
    }

    public static DataSize ofBytes(long bytes) {
        return new DataSize(bytes);
    }

    public long getByteCount() {
        return byteCount;
    }

    public int getByteCountAsSaturatedInt() {
        return (int) Math.min(Integer.MAX_VALUE, byteCount);
    }

    @Override
    public String toString() {
        return byteCount + " bytes";
    }
}