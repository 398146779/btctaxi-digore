package com.btctaxi.controller.support;


import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class ResponseAdvice implements ResponseBodyAdvice<Object> {
    @ExceptionHandler(Throwable.class)
    public Map<String, Object> handle(Throwable err) {

        log.error(err.getMessage(), err);
        Map<String, Object> map = new HashMap<>();

        if ((err instanceof com.btctaxi.controller.support.ServiceError)) {
            com.btctaxi.controller.support.ServiceError error = (com.btctaxi.controller.support.ServiceError) err;
            map.put("code", error.getCode());
            map.put("data", null);
            map.put("error", error.getMsg());
            return map;
        }

        map.put("error", err.getMessage().split(":")[0]);
        return map;
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        return body == null ? new JSONObject() : body;
    }
}
