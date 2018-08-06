package com.btctaxi.gate.controller.support;

import genesis.gate.error.BadRequestError;
import genesis.gate.error.ConcurrentError;
import genesis.gate.error.ServiceError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.core.MethodParameter;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@RestControllerAdvice
public class ResponseAdvice implements ResponseBodyAdvice<Object> {
    private Logger log = LoggerFactory.getLogger(getClass());

    private final String CODE_KEY = "code";
    private final String PARAMS_KEY = "params";
    private final String MESSAGE_KEY = "message";
    private final String DATA_KEY = "data";
    private final String FA2_KEY = "fa2";

    private MessageSource errors;
    private Session sess;

    public ResponseAdvice(@Qualifier("error") MessageSource errors, Session sess) {
        this.errors = errors;
        this.sess = sess;
    }

    @ExceptionHandler(ServiceError.class)
    public Map<String, Object> handle(ServiceError err) {
        Map<String, Object> map = new HashMap<>();
        map.put(CODE_KEY, err.code);
        map.put(PARAMS_KEY, err.params);
        return map;
    }

    @ExceptionHandler(BadRequestError.class)
    public Map<String, Object> handle(BadRequestError err) {
        log.error(err.getMessage(), err);
        Map<String, Object> map = new HashMap<>();
        map.put(CODE_KEY, HttpServletResponse.SC_BAD_REQUEST);
        return map;
    }

    @ExceptionHandler(ConcurrentError.class)
    public Map<String, Object> handle(ConcurrentError err) {
        log.error(err.getMessage(), err);
        Map<String, Object> map = new HashMap<>();
        map.put(CODE_KEY, HttpServletResponse.SC_CONFLICT);
        return map;
    }

    @ExceptionHandler(Throwable.class)
    public Map<String, Object> handle(Throwable err) {
        log.error(err.getMessage(), err);
        Map<String, Object> map = new HashMap<>();
        map.put(CODE_KEY, HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        return map;
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        if (body instanceof Resource)
            return body;

        Map<String, Object> fa2Map = null;
        if (body instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) body;
            Integer code = (Integer) map.get(CODE_KEY);
            Object[] params = (Object[]) map.get(PARAMS_KEY);
            if (code != null && code != 0) {
                if (sess.getLocale() == null || sess.getLocale().isEmpty())
                    map.put(MESSAGE_KEY, errors.getMessage(String.valueOf(code), params, Locale.US));
                else {
                    String[] lc = sess.getLocale().split("-");
                    if (lc.length == 2)
                        map.put(MESSAGE_KEY, errors.getMessage(String.valueOf(code), params, new Locale(lc[0], lc[1])));
                    else
                        map.put(MESSAGE_KEY, errors.getMessage(String.valueOf(code), params, Locale.US));
                }
                map.remove(PARAMS_KEY);
                return map;
            }

            if (map.containsKey("key"))
                fa2Map = map;
        }

        Map<String, Object> result = new HashMap<>();
        result.put(CODE_KEY, 0);
        result.put(DATA_KEY, body);
        if (fa2Map != null)
            result.put(FA2_KEY, fa2Map);

        return result;
    }
}
