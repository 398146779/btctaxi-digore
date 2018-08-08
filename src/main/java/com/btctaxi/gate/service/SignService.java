package com.btctaxi.gate.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.btctaxi.common.DataMap;
import com.btctaxi.gate.controller.support.Session;
import com.btctaxi.gate.error.ServiceError;
import com.btctaxi.gate.util.Convert;
import org.apache.commons.codec.digest.DigestUtils;
//import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpSession;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class SignService extends BaseService {
    protected final int USER_STATE_ACTIVE = 1;
    protected final int USER_STATE_FORBID = 0;
    protected final String ACTIVATE_KEY_PREFIX = "ACTIVATE_";
    protected final String ACTIVATE_EMAIL_KEY_PREFIX = "ACTIVATE_EMAIL_";
    protected final String SIGNUP_KEY_PREFIX = "SIGNUP_";

    protected final String INVITE_RANKING_KEY_PREFIX = "MARKETING_INVITE_RANKING";
    protected final String MARKETING_INVITE_KEY_PREFIX = "MARKETING_INVITE_USER_";

    private final long HOUR2 = 7200000L;
    private final long MINUTE5 = 300000L;

    private KycService kycService;
    private HttpSession session;
    private Session sess;

    public SignService(KycService kycService, HttpSession session, Session sess) {
        this.kycService = kycService;
        this.session = session;
        this.sess = sess;
    }

    @Transactional(rollbackFor = Throwable.class)
    public JSONObject preSignup(String email, String password, String invitorId, String host, Integer region_id, String phone) {
        String emailKey = ACTIVATE_EMAIL_KEY_PREFIX + email;
        String val = kv.opsForValue().get(emailKey);
        if (val != null)
            throw new ServiceError(ServiceError.ACTIVATE_CODE_RESEND_TIME_TOO_SHORT);

        String sql = "SELECT id FROM tb_user WHERE email = ?";
        DataMap user = data.queryOne(sql, email);
        if (user != null)
            throw new ServiceError(ServiceError.SIGNUP_EMAIL_EXISTS);

        if (phone != null) {
            sql = "SELECT id FROM tb_user WHERE phone = ?";
            user = data.queryOne(sql, phone);
            if (user != null)
                throw new ServiceError(ServiceError.SIGNUP_PHONE_EXISTS);
        }

        JSONObject payload = new JSONObject();
        payload.put("email", email);
        payload.put("region_id", region_id);
        payload.put("phone", phone);
        payload.put("password", password);
        payload.put("invitorId", invitorId);
        payload.put("host", host);
        return payload;
    }

    @Transactional(rollbackFor = Throwable.class)
    public DataMap signupSMS(JSONObject payload) {
        Integer region_id = payload.getInteger("region_id");
        String phone = payload.getString("phone");

        String key = DigestUtils.md5Hex(UUID.randomUUID().toString() + phone);
        String signupKey = SIGNUP_KEY_PREFIX + key;

        String code = sms.generateCode();
        Map<String, String> attrs = new HashMap<>();
        attrs.put("payload", payload.toJSONString());
        kv.<String, String>opsForHash().putAll(signupKey, attrs);
        kv.expire(signupKey, MINUTE5, TimeUnit.MILLISECONDS);

        String sql = "SELECT operator, operator_backup FROM tb_region WHERE id = ?";
        DataMap region = data.queryOne(sql, region_id);

        int operator = region.getInt("operator");
        int operator_backup = region.getInt("operator_backup");

        if (region_id != 86 && operator == 3)
            operator = operator_backup;
        sms.sendCode(operator, region_id, phone, code, distConfig.getSmsSign());

        DataMap map = new DataMap();
        map.put("code", code);
        map.put("auth", key);

        return map;
    }

    @Transactional(rollbackFor = Throwable.class)
    public String signupCode(String key) {
        String signupKey = SIGNUP_KEY_PREFIX + key;
        Map<String, String> map = kv.<String, String>opsForHash().entries(signupKey);
        if (map == null || map.isEmpty())
            throw new ServiceError(ServiceError.SIGNUP_PHONE_AUTH_FAIL);

        JSONObject payload = JSON.parseObject(map.get("payload"));
        return signupAction(payload);
    }

    @Transactional(rollbackFor = Throwable.class)
    public void signupCodeReSend(String key, Integer region_id, String phone, String code) {
        String signupKey = SIGNUP_KEY_PREFIX + key;
        Map<String, String> map = kv.<String, String>opsForHash().entries(signupKey);
        if (map == null || map.isEmpty())
            throw new ServiceError(ServiceError.SIGNUP_PHONE_AUTH_FAIL);

        String sql = "SELECT operator, operator_backup FROM tb_region WHERE id = ?";
        DataMap region = data.queryOne(sql, region_id);

        int operator = region.getInt("operator");
        int operator_backup = region.getInt("operator_backup");

        if (region_id != 86 && operator == 3)
            operator = operator_backup;

        sms.sendCode(operator, region_id, phone, code, distConfig.getSmsSign());
    }

    public String signupAction(JSONObject payload) {
        String email = payload.getString("email");
        Integer region_id = payload.getInteger("region_id");
        String phone = payload.getString("phone");
        String password = payload.getString("password");
        String invitorId = payload.getString("invitorId");
        String host = payload.getString("host");

        String nick = getNick(email);
        String auth = UUID.randomUUID().toString().replaceAll("-", "");
        String salt = UUID.randomUUID().toString();
        String sha = DigestUtils.sha256Hex(password + salt);

        String emailKey = ACTIVATE_EMAIL_KEY_PREFIX + email;

        // 加入待激活
        Map<String, String> map = new HashMap<>();
        map.put("email", email);
        map.put("pass", sha);
        map.put("salt", salt);
        if (invitorId != null)
            map.put("invitor_id", invitorId);
        if (phone != null)
            map.put("phone", phone);
        if (region_id != null) {
            map.put("region_id", String.valueOf(region_id));
        }
        map.put("activated", "false");

        kv.opsForValue().set(emailKey, "1", HOUR2, TimeUnit.MILLISECONDS);

        String key = ACTIVATE_KEY_PREFIX + auth;
        kv.opsForHash().putAll(key, map);
        kv.expire(key, HOUR2, TimeUnit.MILLISECONDS);

        sendActivateEmail(email, nick, auth, host);

        return auth;
    }

    @Transactional(rollbackFor = Throwable.class)
    public void reactivate(String auth, String host) {
        Map<String, String> map = kv.<String, String>opsForHash().entries(ACTIVATE_KEY_PREFIX + auth);
        if (map == null || map.isEmpty())
            throw new ServiceError(ServiceError.ACTIVATE_CODE_NOT_EXISTS);

        String email = map.get("email");
        String nick = getNick(email);

        sendActivateEmail(email, nick, auth, host);
    }

    @Transactional(rollbackFor = Throwable.class)
    public void activate(String auth) {
        String key = ACTIVATE_KEY_PREFIX + auth;
        Map<String, String> map = kv.<String, String>opsForHash().entries(key);
        if (map == null || map.isEmpty())
            throw new ServiceError(ServiceError.ACTIVATE_CODE_NOT_EXISTS);

        boolean activated = Boolean.parseBoolean(map.get("activated"));
        if (activated)
            throw new ServiceError(ServiceError.ACTIVATE_DONE_ERROR);

        String email = map.get("email");
        String pass = map.get("pass");
        String salt = map.get("salt");
        String invitorIdStr = map.get("invitor_id");
        String phone = map.get("phone");
        String regionIdStr = map.get("region_id");
        String nick = getNick(email);

        String sql = "SELECT id FROM tb_user WHERE email = ?";
        DataMap user = data.queryOne(sql, email);
        if (user != null)
            throw new ServiceError(ServiceError.SIGNUP_EMAIL_EXISTS);

        if (phone != null) {
            sql = "SELECT id FROM tb_user WHERE phone = ?";
            user = data.queryOne(sql, phone);
            if (user != null)
                throw new ServiceError(ServiceError.SIGNUP_PHONE_EXISTS);
        }

        kv.opsForHash().put(key, "activated", "true");

        Long invitorId = null;
        String invitorEmail = null;
        if (invitorIdStr != null) {
            invitorId = Convert._62_to_10(invitorIdStr);
            sql = "SELECT id, email FROM tb_user WHERE id = ?";
            user = data.queryOne(sql, invitorId);

            if (user == null) {
                invitorId = null;
            } else {
                invitorEmail = user.getString("email");
            }
        }

        Integer region_id = null;
        if (regionIdStr != null) {
            try {
                region_id = Integer.parseInt(regionIdStr);
            } catch (Exception e) {
            }
        }

        sql = "INSERT INTO tb_user(email, pass, salt, nick, region_id, phone, invitor_id) VALUES(?, ?, ?, ?, ?, ?, ?)";
        long userId = data.insert(sql, email, pass, salt, nick, region_id, phone, invitorId);

        sess.setId(userId);
        sess.setEmail(email);
        sess.setRegionId(region_id);
        sess.setPhone(phone);
        sess.setNick(nick);
        sess.setKyc(0);

        if (invitorId != null) {
            updateInvite(sess.getId(), invitorId, email, invitorEmail);
        }

        emailService.sendTemplate(email, "welcome", "zh_CN");
    }

    @Transactional(rollbackFor = Throwable.class)
    public void updateInvite(long userId, long invitorId, String email, String invitorEmail) {
        String key = MARKETING_INVITE_KEY_PREFIX + userId;
        Map<String, String> map = new HashMap<>();
        map.put("email", email);
        map.put("parent", String.valueOf(invitorId));
        map.put("parentEmail", invitorEmail);
        kv.opsForHash().putAll(key, map);

        Long parent = invitorId;
        String parentEmail = invitorEmail;
        for (int i = 1; i <= 6 && parent != null; i++) {
            Map<String, String> parentMap = kv.<String, String>opsForHash().entries(MARKETING_INVITE_KEY_PREFIX + parent);
            if (parentMap == null) {
                parentMap = new HashMap<>();
                parentMap.put("email", parentEmail);
                parentMap.put("n" + i, String.valueOf(1));
                break;
            } else {
                int count = 0;
                String countStr = parentMap.get("n" + i);
                if (countStr != null) {
                    count = Integer.parseInt(countStr);
                }
                parentMap.put("n" + i, String.valueOf(count + 1));
            }

            kv.opsForHash().putAll(MARKETING_INVITE_KEY_PREFIX + parent, parentMap);

            double score = 0;
            try {
                score = kv.opsForZSet().score(INVITE_RANKING_KEY_PREFIX, parentEmail);
            } catch (Throwable t) {
            }

            switch (i) {
                case 1:
                    score += 100;
                    break;
                case 2:
                    score += 50;
                    break;
                case 3:
                    score += 30;
                    break;
                case 4:
                    score += 30;
                    break;
                case 5:
                    score += 30;
                    break;
                case 6:
                    score += 30;
                    break;
            }

            kv.opsForZSet().add(INVITE_RANKING_KEY_PREFIX, parentEmail, score);

            String parentStr = parentMap.get("parent");
            parent = parentStr == null ? null : Long.parseLong(parentStr);
            parentEmail = parentMap.get("parentEmail");
        }
    }

    @Transactional(readOnly = true)
    public DataMap signin_email(String email, String password) {
        String sql = "SELECT salt FROM tb_user WHERE email = ?";
        DataMap user = data.queryOne(sql, email);
        if (user == null)
            throw new ServiceError(ServiceError.SIGNIN_EMAIL_OR_PASS_NOT_EXISTS);
        String salt = user.getString("salt");

        sql = "SELECT id, email, google_key, region_id, phone, nick, locale, freeze_time FROM tb_user WHERE email = ? AND pass = ?";
        user = data.queryOne(sql, email, DigestUtils.sha256Hex(password + salt));
        if (user == null)
            throw new ServiceError(ServiceError.SIGNIN_EMAIL_OR_PASS_NOT_EXISTS);

        Timestamp freezeTime = user.getTime("freeze_time");
        if (freezeTime != null && freezeTime.getTime() > System.currentTimeMillis())
            throw new ServiceError(ServiceError.SIGNIN_USER_FROZEN);

        return user;
    }

    @Transactional(readOnly = true)
    public DataMap signin_phone(Integer region_id, String phone, String password) {
        String sql = "SELECT salt FROM tb_user WHERE region_id = ? AND phone = ?";
        DataMap user = data.queryOne(sql, region_id, phone);
        if (user == null)
            throw new ServiceError(ServiceError.SIGNIN_EMAIL_OR_PASS_NOT_EXISTS);
        String salt = user.getString("salt");

        sql = "SELECT id, email, google_key, region_id, phone, nick, locale, freeze_time FROM tb_user WHERE region_id = ? AND phone = ? AND pass = ?";
        user = data.queryOne(sql, region_id, phone, DigestUtils.sha256Hex(password + salt));
        if (user == null)
            throw new ServiceError(ServiceError.SIGNIN_EMAIL_OR_PASS_NOT_EXISTS);

        Timestamp freezeTime = user.getTime("freeze_time");
        if (freezeTime != null && freezeTime.getTime() > System.currentTimeMillis())
            throw new ServiceError(ServiceError.SIGNIN_USER_FROZEN);

        return user;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> signinAction(JSONObject payload, DataMap user) {
        long userId = payload.getLong("userId");
        String ip = payload.getString("ip");
        if (user == null) {
            String sql = "SELECT id, email, google_key, region_id, phone, nick, locale FROM tb_user WHERE id = ?";
            user = data.queryOne(sql, userId);
        }
        String email = user.getString("email");
        Integer region_id = user.getInt("region_id");
        String phone = user.getString("phone");
        String nick = user.getString("nick");

        sess.setId(userId);
        sess.setGoogleKey(user.getString("google_key"));
        sess.setRegionId(region_id);
        sess.setPhone(phone);
        sess.setEmail(email);
        sess.setNick(nick);
        DataMap kyc = kycService.state(userId);
        if (kyc != null)
            sess.setKyc(kyc.getInt("state"));

        session.setAttribute(FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME, String.valueOf(userId));

        Map<String, Object> map = new HashMap<>(8);
        map.put("email", sess.getEmail());
        map.put("nick", sess.getNick());
        map.put("google", sess.getGoogleKey() != null);
        map.put("phone", sess.getPhone() != null);
        map.put("locale", sess.getLocale());
        map.put("kyc", sess.getKyc());

        sendSigninNotify(email, region_id, phone, payload);

        return map;
    }

    @Transactional(readOnly = true)
    public boolean loop(String code) {
        String key = ACTIVATE_KEY_PREFIX + code;
        String activated = kv.<String, String>opsForHash().get(key, "activated");
        boolean result = activated != null && !activated.isEmpty() ? Boolean.parseBoolean(activated) : false;
        return result;
    }

    public void sendSigninNotify(String email, Integer region_id, String phone, JSONObject payload) {
        String time = getTime();
        String device = payload.getString("device");
        String ip = payload.getString("ip");
        if (email != null) {
            emailService.sendTemplate(email, "login", "zh_CN", device, time, ip);
        }

    /*
    if(phone != null)
    {
      String sql = "SELECT operator, operator_backup FROM tb_region WHERE id = ?";
      DataMap region = data.queryOne(sql, region_id);

      String content = "【ThinkBit】你的帐号刚刚于ThinkBit Pro登录，如果你没有进行此操作，请联系客服，必要时采取冻结帐号操作。";
      sms.sendNotify(region.getInt("operator"), region_id, phone, content);
    }
    */
    }

    public void sendActivateEmail(String email, String nick, String auth, String host) {
        String url = host + "/account/activate?auth=" + auth;
        emailService.sendTemplate(email, "register", "zh_CN", nick, url);
    }

    public DataMap getUser(long userId) {
        String sql = "SELECT id, email, google_key, region_id, phone, locale, freeze_time FROM tb_user WHERE id = ?";
        return data.queryOne(sql, userId);
    }

    private String getTime() {
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        f.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        return f.format(new Date()) + "(UTC+8)";
    }

    private String getNick(String email) {
        return email.split("@")[0];
    }

    //TODO 世界杯活动
    public DataMap findUserByPhone(String regionId, String phone) {
        String sql = "SELECT * FROM tb_user WHERE phone = ? and  region_id = ?";
        return data.queryOne(sql, phone, regionId);
    }
}
