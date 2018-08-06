package com.btctaxi.gate.controller;

import genesis.gate.controller.support.Session;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public class BaseController {
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Any {
    }

    @Autowired
    protected Session sess;
}
