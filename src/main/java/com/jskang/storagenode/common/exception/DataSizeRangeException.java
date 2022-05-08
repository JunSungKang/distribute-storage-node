package com.jskang.storagenode.common.exception;

public class DataSizeRangeException extends Exception {

    public DataSizeRangeException() {
        super("The size range is incorrect. ( 0 < DataSize < 32)");
    }

    public DataSizeRangeException(String data) {
        super("The size range is incorrect. ( 0 < DataSize < 32), Original Data: " +data);
    }
}
