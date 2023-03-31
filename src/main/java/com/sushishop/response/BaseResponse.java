package com.sushishop.response;


public class BaseResponse {
    
    public static final int NORMAL_CODE = 0;
    public static final int ERROR_CODE = 1;

    protected int code;
    protected String msg;

    public BaseResponse(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
    
    public int getCode() {
        return code;
    }
    public void setCode(int code) {
        this.code = code;
    }
    public String getMsg() {
        return msg;
    }
    public void setMsg(String msg) {
        this.msg = msg;
    }

    
}
