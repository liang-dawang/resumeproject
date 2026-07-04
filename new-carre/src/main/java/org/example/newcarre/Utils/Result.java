package org.example.newcarre.Utils;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data

@AllArgsConstructor
public class Result<T> implements Serializable {

    private int code;
    private String msg;
    private T data;

    private Result() {}

    // ========== 成功 ==========
    public static <T> Result<T> ok() {
        return ok(null);
    }

    public static <T> Result<T> ok(T data) {
        Result<T> r = new Result<>();
        r.setCode(200);
        r.setMsg("success");
        r.setData(data);
        return r;
    }

    public static <T> Result<T> ok(String msg, T data) {
        Result<T> r = new Result<>();
        r.setCode(200);
        r.setMsg(msg);
        r.setData(data);
        return r;
    }

    // ========== 失败 ==========
    public static <T> Result<T> fail(String msg) {
        return fail(500, msg);
    }

    public static <T> Result<T> fail(int code, String msg) {
        Result<T> r = new Result<>();
        r.setCode(code);
        r.setMsg(msg);
        r.setData(null);
        return r;
    }

    // ========== 认证/授权专用（配合JWT） ==========
    public static <T> Result<T> unauthorized(String msg) {
        return fail(401, msg);
    }

    public static <T> Result<T> forbidden(String msg) {
        return fail(403, msg);
    }
}