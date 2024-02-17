package org.xiaohuadev.base.exception;

import lombok.Getter;

/**
 * 本项目自定义异常类型
 */
@Getter
public class XueChengPlusException extends RuntimeException {
    private String errMessage;

    public XueChengPlusException() {
    }

    public XueChengPlusException(String message) {
        super(message);
        this.errMessage = message;
    }

    public static void cast(String errMessage) {
        throw new XueChengPlusException(errMessage);
    }

    public static void cast(CommonError error) {
        throw new XueChengPlusException(error.getErrMessage());
    }
}
