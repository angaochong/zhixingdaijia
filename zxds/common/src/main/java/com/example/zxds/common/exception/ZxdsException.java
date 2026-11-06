package com.example.zxds.common.exception;

import lombok.Data;

@Data
public class ZxdsException extends RuntimeException {
    private String msg;
    private int code = 500;

    public ZxdsException(String msg) {
        super(msg);
        this.msg = msg;
    }

    public ZxdsException(String msg, Throwable e) {
        super(msg, e);
        this.msg = msg;
    }

    public ZxdsException(String msg, int code) {
        super(msg);
        this.msg = msg;
        this.code = code;
    }

    public ZxdsException(String msg, int code, Throwable e) {
        super(msg, e);
        this.msg = msg;
        this.code = code;
    }

}
