package com.btctaxi.gate.controller;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.btctaxi.common.DataMap;
import com.btctaxi.gate.config.GeetestConfig;
import com.btctaxi.gate.error.ServiceError;
import com.btctaxi.gate.service.FA2Service;
import com.btctaxi.gate.service.KycService;
import com.btctaxi.gate.service.SignService;
import com.btctaxi.gate.util.IP;
import com.btctaxi.gate.vendor.GeetestLib;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/gate/sign")
public class SignController extends BaseController {
    private final int MINUTE1 = 60000;
    private final String SIGN_SIGNIN_ACTION = "1";

    private final String REGEX_EMAIL = "^([a-z0-9A-Z]+[_|\\-|\\.]?)+[a-z0-9A-Z]?@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
    private final String REGEX_PASS = "^(?![A-Z]+$)(?![a-z]+$)(?!\\d+$)\\S{8,}$";

    private SignService signService;
    private FA2Service fa2Service;
    private KycService kycService;
    private GeetestConfig geetestConfig;

    private final long SECOND60 = 60000L;

    private Logger log = LoggerFactory.getLogger(getClass());

    @Autowired
    protected StringRedisTemplate redisTemplate;

    public SignController(SignService signService, FA2Service fa2Service, KycService kycService, GeetestConfig geetestConfig) {
        this.signService = signService;
        this.fa2Service = fa2Service;
        this.kycService = kycService;
        this.geetestConfig = geetestConfig;
    }

    /**
     * 注册账号并发起激活
     *
     * @param email     邮箱
     * @param password  密码
     * @param invitorId 邀请人ID
     */
    @RequestMapping("/signup")
    @Any
    public void signup(@RequestParam String email, @RequestParam String password, @RequestParam(name = "invitor_id", required = false) String invitorId, HttpServletRequest req) {
        if (sess.getId() != 0)
            throw new ServiceError(ServiceError.SIGNIN_CANT_SIGNUP);

        if (email.isEmpty() || !Pattern.matches(REGEX_EMAIL, email))
            throw new ServiceError(ServiceError.SIGNUP_EMAIL_NULL);

        if (password.isEmpty() || !Pattern.matches(REGEX_PASS, password))
            throw new ServiceError(ServiceError.SIGNUP_PASS_NULL);

        JSONObject payload = signService.preSignup(email, password, invitorId, "https://" + req.getServerName(), null, null);
        String auth = signService.signupAction(payload);
        long now = System.currentTimeMillis();
        sess.setActivateCode(auth);
        sess.setActivateSendTime(now);
    }

    @RequestMapping("/signup/phone")
    @Any
    public DataMap signup_phone(@RequestParam Integer region_id, @RequestParam String phone, @RequestParam String email, @RequestParam String password, @RequestParam(name = "invitor_id", required = false) String invitorId, @RequestParam String geetest_challenge, @RequestParam String geetest_validate, @RequestParam String geetest_seccode, HttpServletRequest req) {
        if (sess.getId() != 0)
            throw new ServiceError(ServiceError.SIGNIN_CANT_SIGNUP);

        GeetestLib gtSdk = new GeetestLib(geetestConfig.getId(), geetestConfig.getKey(), geetestConfig.isNewFailBack());

        HashMap<String, String> geetestMap = new HashMap<>();
        geetestMap.put("ip_address", req.getRemoteAddr());

        int gt_server_status_code = gtSdk.preProcess(geetestMap);
        if(gt_server_status_code != 1)
            throw new ServiceError(ServiceError.GEETEST_AUTH_FAIL);

        int gtResult = gtSdk.geetestSecondCheck(gt_server_status_code, geetest_challenge, geetest_validate, geetest_seccode, req.getRemoteAddr());
        if (gtResult != 1)
            throw new ServiceError(ServiceError.GEETEST_AUTH_FAIL);

        if (email.isEmpty() || !Pattern.matches(REGEX_EMAIL, email))
            throw new ServiceError(ServiceError.SIGNUP_EMAIL_NULL);

        if (password.isEmpty() || !Pattern.matches(REGEX_PASS, password))
            throw new ServiceError(ServiceError.SIGNUP_PASS_NULL);

        if (region_id == null || phone == null)
            throw new ServiceError(ServiceError.SIGNUP_PHONE_NULL);

        long now = System.currentTimeMillis();
        if (now - sess.getPhoneCodeTime() < SECOND60)
            throw new ServiceError(ServiceError.AUTH_PHONE_CODE_LIMIT);

        JSONObject payload = signService.preSignup(email, password, invitorId, "https://" + req.getServerName(), region_id, phone);
        DataMap map = signService.signupSMS(payload);

        sess.setRegionId(region_id);
        sess.setPhone(phone);
        sess.setPhoneCode(map.getString("code"));
        sess.setPhoneCodeTime(System.currentTimeMillis());

        map.remove("code");
        return map;
    }

    @RequestMapping("/signup/code")
    @Any
    public void signupCode(@RequestParam String key, String code) {
        if (code == null || !code.equals(sess.getPhoneCode()))
            throw new ServiceError(ServiceError.SIGNUP_PHONE_AUTH_FAIL);

        String auth = signService.signupCode(key);
        long now = System.currentTimeMillis();
        sess.setActivateCode(auth);
        sess.setActivateSendTime(now);
    }

    @RequestMapping("/signup/code/resend")
    @Any
    public void signupCodeReSend(@RequestParam String key) {
        long now = System.currentTimeMillis();
        if (now - sess.getPhoneCodeTime() < SECOND60)
            throw new ServiceError(ServiceError.AUTH_PHONE_CODE_LIMIT);

        signService.signupCodeReSend(key, sess.getRegionId(), sess.getPhone(), sess.getPhoneCode());
        sess.setPhoneCodeTime(System.currentTimeMillis());
    }

    /**
     * 重新发送激活码
     */
    @RequestMapping("/reactivate")
    @Any
    public void reactivate(HttpServletRequest req) {
        if (sess.getActivateCode() == null)
            throw new ServiceError(ServiceError.ACTIVATE_CODE_NOT_EXISTS);
        long now = System.currentTimeMillis();
        if (now - sess.getActivateSendTime() < MINUTE1)
            throw new ServiceError(ServiceError.ACTIVATE_CODE_RESEND_TIME_TOO_SHORT);

        signService.reactivate(sess.getActivateCode(), "https://" + req.getServerName());
    }

    /**
     * 邮箱激活
     *
     * @param auth 激活码
     */
    @RequestMapping("/activate")
    @Any
    public void activate(@RequestParam String auth) {
        signService.activate(auth);
    }

    /**
     * 登录验证
     *
     * @param email    邮箱
     * @param password 密码
     */
    @RequestMapping("/signin/email")
    @Any
    public Map<String, Object> email_signin(@RequestParam String email, @RequestParam String password, @RequestParam String geetest_challenge, @RequestParam String geetest_validate, @RequestParam String geetest_seccode, HttpServletRequest req) {
        if (email.isEmpty() || password.isEmpty())
            throw new ServiceError(ServiceError.SIGNIN_NAME_OR_PASS_NULL);

        log.info("session id={}, email={} ", req.getSession().getId(), email);
        return signin(email, null, null, password, geetest_challenge, geetest_validate, geetest_seccode, req);
    }

    @RequestMapping("/signin/phone")
    @Any
    public Map<String, Object> phone_signin(@RequestParam Integer region_id, @RequestParam String phone, @RequestParam String password, @RequestParam String geetest_challenge, @RequestParam String geetest_validate, @RequestParam String geetest_seccode, HttpServletRequest req) {
        if (region_id == null || phone == null || password == null)
            throw new ServiceError(ServiceError.SIGNIN_NAME_OR_PASS_NULL);

        log.info("session id={}, phone={} ", req.getSession().getId(), phone);
        return signin(null, region_id, phone, password, geetest_challenge, geetest_validate, geetest_seccode, req);
    }

    private Map<String, Object> signin(String email, Integer region_id, String phone, String password, String geetest_challenge, String geetest_validate, String geetest_seccode, HttpServletRequest req) {
        GeetestLib gtSdk = new GeetestLib(geetestConfig.getId(), geetestConfig.getKey(), geetestConfig.isNewFailBack());

        HashMap<String, String> map = new HashMap<>();
        map.put("ip_address", req.getRemoteAddr());

        int gt_server_status_code = gtSdk.preProcess(map);
        if(gt_server_status_code != 1)
            throw new ServiceError(ServiceError.SIGNIN_AUTH_GEETEST_FAIL);

        int gtResult = gtSdk.geetestSecondCheck(gt_server_status_code, geetest_challenge, geetest_validate, geetest_seccode, req.getRemoteAddr());

        log.info("geetest check result: {}", gtResult);

        if (gtResult != 1)
            throw new ServiceError(ServiceError.SIGNIN_AUTH_GEETEST_FAIL);

        DataMap user = email == null ? signService.signin_phone(region_id, phone, password) : signService.signin_email(email, password);
        long userId = user.getLong("id");
        String googleKey = user.getString("google_key");
        String _phone = user.getString("phone");

        JSONObject payload = new JSONObject();
        payload.put("userId", userId);
        payload.put("ip", IP.getIpAddr(req));

        UserAgent ua = UserAgent.parseUserAgentString(req.getHeader("User-Agent"));
        payload.put("device", ua.getOperatingSystem() + "，" + ua.getBrowser());

        Map<String, Object> result;

        if (googleKey != null) {
            result = fa2Service.getFA2Map(userId, SIGN_SIGNIN_ACTION, payload, googleKey, null);
        } else if (_phone != null) {
            result = fa2Service.getFA2Map(userId, SIGN_SIGNIN_ACTION, payload, null, _phone);
        } else {
            result = signService.signinAction(payload, user);
        }

        //TODO 世界杯活动
        if (_phone != null) {
            try {
                String region_idInDb = user.getInt("region_id") + "";
                //记录登录次数, 每天记录一次, 过期时间设置为 10天
                String key = getUserLoginCountKey();
                redisTemplate.opsForSet().add(key, region_idInDb + _phone);
                redisTemplate.expire(key, DAY_10, TimeUnit.DAYS);
                //opsForValue().set(otpKey, code, SECOND50, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                log.error(String.format("redis send error , key USER_LOGIN_COUNT_* , userId=%s", userId), e);
            }
        }

        return result;
    }

    private String getUserLoginCountKey() {
        return "USER_LOGIN_COUNT_" + DateTime.now().toString("yyyyMMdd");
    }

    /**
     * 获取会话信息
     */
    @RequestMapping("/session")
    public Map<String, Object> session() {
        DataMap kyc = kycService.state(sess.getId());
        if (kyc != null)
            sess.setKyc(kyc.getInt("state"));

        DataMap user = signService.getUser(sess.getId());
        if (user != null) {
            sess.setGoogleKey(user.getString("google_key"));
            sess.setRegionId(user.getInt("region_id"));
            sess.setPhone(user.getString("phone"));
        }

        Map<String, Object> map = new HashMap<>(8);
        map.put("email", sess.getEmail());
        map.put("nick", sess.getNick());
        map.put("google", sess.getGoogleKey() != null);
        map.put("phone", sess.getPhone() != null);
        map.put("locale", sess.getLocale());
        map.put("kyc", sess.getKyc());

        return map;
    }

    /**
     * 退出登录
     */
    @RequestMapping("/signout")
    @Any
    public void signout(@Autowired HttpSession s) {
        s.invalidate();
    }

    @RequestMapping("/loop")
    @Any
    public Map<String, Object> loop() {
        String code = sess.getActivateCode();
        boolean result = false;
        if (code != null)
            result = signService.loop(code);
        Map<String, Object> map = new HashMap<>();
        map.put("activated", result);
        return map;
    }


    private int DAY_10 = 35;
    private String countToken = "99427ccb-d450-42b8-bd17-ecd1ab317dc1";

    /**
     * 获取登录次数
     *
     * @return
     */
    @RequestMapping("/signCounter")
    @Any
    public List<String> signCounter(@RequestParam String token, @RequestParam String regionId, @RequestParam String phone) {
        //    HashMap<String, Integer> retMap = Maps.newHashMap();
        ArrayList<String> ret = Lists.newArrayList();
        //get 循环日期 diff redis 结果
        if (!countToken.equals(token)) {
            return null;
        }

        for (int i = 0; i < DAY_10; i++) {
            String days = DateTime.now().plusDays(-i).toString("yyyyMMdd");
            String key = "USER_LOGIN_COUNT_" + days;

            if (redisTemplate.opsForSet().isMember(key, regionId + phone)) {
                ret.add(days);
            }
        }

        return ret;

    }

    //TODO 世界杯活动
    @RequestMapping("/userInfo")
    @BaseController.Any
    public Map<String, Object> userInfo(@RequestParam String token, @RequestParam String regionId, @RequestParam String phone) {
        if (!countToken.equals(token)) {
            return null;
        }

        Map<String, Object> ret = Maps.newHashMap();

        DataMap user = signService.findUserByPhone(regionId, phone);
        if (user == null) {
            return null;
        }
        ret.put("kycState", 0);
        ret.put("kycDate", null);
        String mail = "email";
        String freezeTime = "freeze_time";

        ret.put(mail, user.getString(mail));
        ret.put(freezeTime, user.getString(freezeTime));

        DataMap kyc = kycService.kycInfo(user.getLong("id"));
        if (kyc != null) {
            ret.put("kycState", kyc.getInt("state"));
            ret.put("kycTime", kyc.getTime("review_time"));
        }


        return ret;
    }

}
