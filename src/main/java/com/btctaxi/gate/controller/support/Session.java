package com.btctaxi.gate.controller.support;

import lombok.Data;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.io.Serializable;

@Data
@Component
@SessionScope
public class Session implements Serializable {
    private long id;
    private String email;
    private String nick;
    private String locale;

    private String googleKey;
    private String newGoogleKey;

    private Integer regionId;
    private String phone;
    private Integer newRegionId;
    private String newPhone;

    private String activateCode;
    private long activateSendTime;

    private String phoneCode;
    private long phoneCodeTime;

    private int kyc;

    private int gtServerStatus;
}
