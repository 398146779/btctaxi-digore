package com.btctaxi.gate.util;

import com.btctaxi.gate.vendor.AmazonSMS;
import com.btctaxi.gate.vendor.Twilio;
import com.btctaxi.gate.vendor.YunPian;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class SMS {
    private YunPian yunPian;
    private AmazonSMS amazonSMS;
    private Twilio twilio;

    private final Random RAND = new Random(System.currentTimeMillis());
    private final int CARRIER_AWS = 1;
    private final int CARRIER_TWILIO = 2;
    private final int CARRIER_YUNPIAN = 3;
    private final int CODE_N = 6;

    public SMS(YunPian yunPian, AmazonSMS amazonSMS, Twilio twilio) {
        this.yunPian = yunPian;
        this.amazonSMS = amazonSMS;
        this.twilio = twilio;
    }

    public void send(int operator, int region_id, String phone, String content) {
        String mobile = region_id + phone;
        switch (operator) {
            case CARRIER_YUNPIAN:
                yunPian.send(region_id, phone, content);
                break;
            case CARRIER_AWS:
                amazonSMS.send(mobile, content);
                break;
            case CARRIER_TWILIO:
                twilio.send(mobile, content);
                break;
            default:
                break;
        }
    }

    @Async
    public void sendNotify(int operator, int region_id, String mobile, String content) {
        send(operator, region_id, mobile, content);
    }

    // 智能匹配模板，举例：【THINKBit】您的验证码是920201
    @Async
    public void sendCode(int operator, int region_id, String mobile, String code, String sign) {
        if (operator == CARRIER_TWILIO)
            operator = CARRIER_AWS;
        sign = sign == null ? "ThinkBit" : sign;
        String content = "【" + sign + "】您的验证码是" + code + "，如非本人操作，请勿告诉他人。";
        send(operator, region_id, mobile, content);
    }

    public String generateCode() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < CODE_N; i++) {
            sb.append(RAND.nextInt(10));
        }
        return sb.toString();
    }
}
