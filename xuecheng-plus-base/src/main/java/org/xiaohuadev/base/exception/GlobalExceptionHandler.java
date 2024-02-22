package org.xiaohuadev.base.exception;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 全局异常处理器
 */
@Slf4j
@ControllerAdvice //控制器增强
public class GlobalExceptionHandler {

    /**
     * 自定义异常抛出(计划内异常)
     *
     * @param exception 异常类型
     * @return 异常信息模型类
     */
    @ResponseBody //返回值转Json
    @ExceptionHandler(XueChengPlusException.class) //捕获指定异常
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse customException(XueChengPlusException exception) {
        //记录日志
        log.error("系统异常{}", exception.getErrMessage());

        //解析出异常信息 封装成RestErrorResponse对象并返回给前端
        String errMessage = exception.getErrMessage();
        return new RestErrorResponse(errMessage);
    }

    /**
     * 抛出异常(计划外异常)
     *
     * @param unknownException 计划外的未知异常 统一用Exception捕获
     * @return 异常信息模型类
     */
    @ResponseBody
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse exception(Exception unknownException) {
        log.error("系统异常{}", unknownException.getMessage());
        return new RestErrorResponse(CommonError.UNKNOWN_ERROR.getErrMessage());
    }

    /**
     * 捕获参数异常
     *
     * @param exception 参数校验错误异常
     * @return 异常信息模型类
     */
    @ResponseBody
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RestErrorResponse methodArgumentNotValidException(MethodArgumentNotValidException exception) {
        BindingResult bindingResult = exception.getBindingResult();
        List<String> errors = new ArrayList<>();
        bindingResult.getFieldErrors().stream().forEach(item -> {
            errors.add(item.getDefaultMessage());
        });

        String errorMessage = StringUtils.join(errors, ",");
        log.error("系统异常{}", exception.getMessage());
        return new RestErrorResponse(errorMessage);
    }
}
