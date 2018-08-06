package com.btctaxi.gate.controller.support;

import genesis.gate.controller.BaseController.Any;
import genesis.gate.error.ServiceError;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;

@Component
@Aspect
public class SessionAspect {
    private Session sess;

    public SessionAspect(Session sess) {
        this.sess = sess;
    }

    @Around("execution(public * genesis.gate.controller..*(..)) && @annotation(org.springframework.web.bind.annotation.RequestMapping)")
    public Object session(ProceedingJoinPoint point) throws Throwable {
        MethodSignature sig = (MethodSignature) point.getSignature();
        Method m = sig.getMethod();
        if (!m.isAnnotationPresent(Any.class) && sess.getId() == 0)
            throw new ServiceError(ServiceError.SIGNIN_UNAUTH);

        HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String l = req.getHeader("lang");
        Cookie[] cookies = req.getCookies();
        String lang = "en";
        if (cookies != null)
            for (Cookie c : cookies)
                if ("lang".equals(c.getName())) {
                    lang = c.getValue();
                    break;
                }

        sess.setLocale(l == null ? lang : l);

        return point.proceed();
    }
}
