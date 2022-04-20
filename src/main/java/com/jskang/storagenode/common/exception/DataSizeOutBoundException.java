package com.jskang.storagenode.common.exception;

public class DataSizeOutBoundException extends Exception {

    public DataSizeOutBoundException(int limitSize) {
        super("The size range is incorrect. ( " +limitSize+ " < DataSize )");
    }
}
