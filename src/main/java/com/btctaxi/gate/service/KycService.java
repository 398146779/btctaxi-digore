package com.btctaxi.gate.service;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.btctaxi.common.DataMap;
import com.btctaxi.gate.config.GeetestConfig;
import com.btctaxi.gate.config.KycConfig;
import com.btctaxi.gate.error.BadRequestError;
import com.btctaxi.gate.error.ServiceError;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class KycService extends BaseService {
    private final int KYC_NULL = 0;
    private final int KYC_AUDIT_PASS = 1;
    private final int KYC_AUDIT_UNPASS = 2;
    private final int KYC_UNAUDITED = 3;

    private final String OCR_RATE_VALUE = "OCR_RATE_VALUE"; //人脸识别通过率

    private AmazonS3 amazonS3;
    private String bucket;
    @Autowired
    private GeetestConfig geetestConfig;

    public KycService(AmazonS3 kycS3, KycConfig kycConfig) {
        this.amazonS3 = kycS3;
        this.bucket = kycConfig.getBucket();
    }

    @Transactional(readOnly = true)
    public DataMap query(long userId, String host) {
        String sql = "SELECT type, id_no, first_name, last_name, birthday, unit, street, city, location, zipcode, passport, id_front, id_back, id_hold, guardian, other, state, reason FROM tb_kyc WHERE uid = ?";
        DataMap kyc = data.queryOne(sql, userId);
        DataMap result = new DataMap();
        if (kyc != null) {
            kyc.forEach((k, v) ->
            {
                if (k.equals("passport") || k.equals("id_front") || k.equals("id_back") || k.equals("id_hold") || k.equals("guardian") || k.equals("other")) {
                    if (v != null) {
                        result.put(k, host + "/gate/kyc/read?type=" + k);
                    }
                } else {
                    result.put(k, v);
                }
            });
        }
        return result;
    }

    @Transactional(readOnly = true)
    public DataMap state(long userId) {
        String sql = "SELECT state FROM tb_kyc WHERE uid = ?";
        DataMap kyc = data.queryOne(sql, userId);
        return kyc;
    }

    @Transactional(rollbackFor = Throwable.class)
    public Map<String, Object> update(long userId, int type, String id_no, String first_name, String last_name, String birthday,
                                      String unit, String street, String city, int location, String zipCode) {
        int state = KYC_NULL;
        String sql = "SELECT uid, id_no, state FROM tb_kyc WHERE uid = ?";
        DataMap kyc = data.queryOne(sql, userId);
        if (kyc != null && (kyc.getInt("state") == KYC_AUDIT_PASS || kyc.getInt("state") == KYC_UNAUDITED))
            throw new BadRequestError();


        Map<String, Object> result = new HashMap<>();

//        try {
//            if (StringUtils.isNotEmpty(geeToken)) {
//                JSONObject resultMap = getOCRValidateResult(geeToken);
//                if (resultMap != null && "200".equals(resultMap.getString("status"))) {
//                    Boolean passed = resultMap.getJSONObject("data").getBoolean("passed");
//                    if (passed) {
//                        state = KYC_AUDIT_PASS;
//                    }
//                    result.put("passed", passed);
//                }
//            }
//        } catch (Exception e) {
//            log.error("getOCRValidateResult error: ", e);
//        }


        sql = "SELECT uid, id_no, state FROM tb_kyc WHERE id_no = ?";
        List<DataMap> kycs = data.query(sql, id_no);

        if (kyc == null) {
            if (kycs.size() >= 5)
                throw new ServiceError(ServiceError.KYC_SAME_ID_NO_LIMIT5);

            sql = "INSERT INTO tb_kyc(uid, type, id_no, first_name, last_name, birthday, unit, street, city, location, zipcode, state) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            data.update(sql, userId, type, id_no, first_name, last_name, birthday, unit, street, city, location, zipCode, state);
        } else {
            if ((kycs.size() == 5 && !kyc.getString("id_no").equals(id_no)) || kyc.size() > 5)
                throw new ServiceError(ServiceError.KYC_SAME_ID_NO_LIMIT5);

            state = kyc.getInt("state");
            sql = "UPDATE tb_kyc SET type = ?, id_no = ?, first_name = ?, last_name = ?, birthday = ?, unit = ?, street = ?, city = ?, location = ?, zipcode = ?, state = ?, update_time = NOW() WHERE uid = ?";
            int rowN = data.update(sql, type, id_no, first_name, last_name, birthday, unit, street, city, location, zipCode, state, userId);
            if (rowN == 1)
                state = KYC_NULL;
        }

        result.put("state", state);

        return result;
    }


    String validateUrl = "http://tectapi.geetest.com/validate";

    public JSONObject getOCRValidateResult(String token) {
        return getOCRValidateResult(token, null, null);
    }

    public JSONObject getOCRValidateResult(String token, String ocrId, String ocrKey) {
        String timestamp = (System.currentTimeMillis() / 1000) + "";
        String nonce = UUID.randomUUID().toString().replaceAll("-", "");


        if (StringUtils.isEmpty(ocrId))
            ocrId = geetestConfig.getOcrId();
        if (StringUtils.isEmpty(ocrKey))
            ocrKey = geetestConfig.getOcrKey();


        List<String> stringList = Lists.newArrayList(ocrId, timestamp, nonce);

        String str = stringList.stream()
//                .sorted(Comparator.reverseOrder())
                .sorted()
                .collect(Collectors.joining(""));

//        String str = String.format("%s%s%s", ocrId, timestamp, nonce);
        final Mac sha256_HMAC;
        try {
            sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(ocrKey.getBytes("UTF-8"), "HmacSHA256");
            sha256_HMAC.init(secret_key);

            String hex = Hex.encodeHexString(sha256_HMAC.doFinal(str.getBytes("UTF-8")));
            String signatureStr = String.format("gt_id=%s,timestamp=%s,nonce=%s,signature=%s", ocrId, timestamp, nonce, hex);

            log.info("headers.Authorization={}    \tjoin_str={}  \ttoken=\t{}, id=\t{}, key=\t{}", signatureStr, str, token, ocrId, ocrKey);


            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", signatureStr);
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            Map<String, Object> params = Maps.newHashMap();
            params.put("token", token);
            HttpEntity req = new HttpEntity("token=" + token, headers);
            ResponseEntity<JSONObject> res = http.postForEntity(validateUrl, req, JSONObject.class);

            log.info("headers.Authorization={}, body={}", signatureStr, res.getBody());
            return res.getBody();
        } catch (NoSuchAlgorithmException | InvalidKeyException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return null;


    }

    @Transactional(readOnly = true)
    public S3Object getResource(long userId, String type) {
        String sql = "SELECT passport, id_front, id_back, id_hold, guardian, other, state FROM tb_kyc WHERE uid = ?";
        DataMap kyc = data.queryOne(sql, userId);
        String key = kyc.getString(type);
        S3Object o = amazonS3.getObject(bucket, key);
        return o;
    }

    @Transactional(rollbackFor = Throwable.class)
    public int upload(long userId, String email, MultipartFile passport, MultipartFile id_front, MultipartFile id_back, MultipartFile id_hold, MultipartFile guardian, MultipartFile other) {
        int state = KYC_NULL;
        try {
            String sql = "SELECT passport, id_front, id_back, id_hold, guardian, other, state FROM tb_kyc WHERE uid = ?";
            DataMap kyc = data.queryOne(sql, userId);

            if (kyc != null && kyc.getInt("state") != 1) {
                int uid = (int) (System.currentTimeMillis() / 1000);
                state = kyc.getInt("state");

                if (state == KYC_AUDIT_PASS || state == KYC_UNAUDITED)
                    throw new BadRequestError();

                String passport_key = getKycValue(kyc, "passport");
                if (passport != null && passport.getSize() > 0) {
                    passport_key = "passport" + userId + "-" + uid + getExtension(passport.getContentType());
                    long size = passport.getSize();
                    ObjectMetadata meta = new ObjectMetadata();
                    meta.setContentLength(size);
                    amazonS3.putObject(new PutObjectRequest(bucket, passport_key, passport.getInputStream(), meta).withCannedAcl(CannedAccessControlList.BucketOwnerRead));
                }

                String idfront_key = getKycValue(kyc, "id_front");
                if (id_front != null && id_front.getSize() > 0) {
                    idfront_key = "idfront" + userId + "-" + uid + getExtension(id_front.getContentType());
                    long size = id_front.getSize();
                    ObjectMetadata meta = new ObjectMetadata();
                    meta.setContentLength(size);
                    amazonS3.putObject(new PutObjectRequest(bucket, idfront_key, id_front.getInputStream(), meta).withCannedAcl(CannedAccessControlList.BucketOwnerRead));
                }

                String idback_key = getKycValue(kyc, "id_back");
                if (id_back != null && id_back.getSize() > 0) {
                    idback_key = "idback" + userId + "-" + uid + getExtension(id_back.getContentType());
                    long size = id_back.getSize();
                    ObjectMetadata meta = new ObjectMetadata();
                    meta.setContentLength(size);
                    amazonS3.putObject(new PutObjectRequest(bucket, idback_key, id_back.getInputStream(), meta).withCannedAcl(CannedAccessControlList.BucketOwnerRead));
                }

                String idhold_key = getKycValue(kyc, "id_hold");
                if (id_hold != null && id_hold.getSize() > 0) {
                    idhold_key = "idhold" + userId + "-" + uid + getExtension(id_hold.getContentType());
                    long size = id_hold.getSize();
                    ObjectMetadata meta = new ObjectMetadata();
                    meta.setContentLength(size);
                    amazonS3.putObject(new PutObjectRequest(bucket, idhold_key, id_hold.getInputStream(), meta).withCannedAcl(CannedAccessControlList.BucketOwnerRead));
                }

                String guardian_key = getKycValue(kyc, "guardian");
                if (guardian != null && guardian.getSize() > 0) {
                    guardian_key = "guardian" + userId + "-" + uid + getExtension(guardian.getContentType());
                    long size = guardian.getSize();
                    ObjectMetadata meta = new ObjectMetadata();
                    meta.setContentLength(size);
                    amazonS3.putObject(new PutObjectRequest(bucket, guardian_key, guardian.getInputStream(), meta).withCannedAcl(CannedAccessControlList.BucketOwnerRead));
                }

                String other_key = getKycValue(kyc, "other");
                if (other != null && other.getSize() > 0) {
                    other_key = "other" + userId + "-" + uid + getExtension(other.getContentType());
                    long size = other.getSize();
                    ObjectMetadata meta = new ObjectMetadata();
                    meta.setContentLength(size);
                    amazonS3.putObject(new PutObjectRequest(bucket, other_key, other.getInputStream(), meta).withCannedAcl(CannedAccessControlList.BucketOwnerRead));
                }

                sql = "UPDATE tb_kyc SET passport = ?, id_front = ?, id_back = ?, id_hold = ?, guardian = ?, other = ?, state = ?, update_time = NOW() WHERE uid = ?";
                int rowN = data.update(sql, passport_key, idfront_key, idback_key, idhold_key, guardian_key, other_key, KYC_UNAUDITED, userId);
                if (rowN == 1)
                    state = KYC_UNAUDITED;
            }
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            throw new ServiceError(ServiceError.UPLOAD_FAILED);
        }

        emailService.sendTemplate(email, "kyc-processing", "zh_CN");

        return state;
    }


    @Transactional(rollbackFor = Throwable.class)
    public String upload2(long userId, MultipartFile file) {
        String uuid = userId + "-" + UUID.randomUUID().toString();
        long size = 0;
        try {
            if (file != null && file.getSize() > 0) {
                size = file.getSize();
                ObjectMetadata meta = new ObjectMetadata();
                meta.setContentLength(size);
                log.info(" userId={}, imageId={}, size={}", userId, uuid, size);
                amazonS3.putObject(new PutObjectRequest(bucket, uuid, file.getInputStream(), meta).withCannedAcl(CannedAccessControlList.BucketOwnerRead));
            }
        } catch (Throwable e) {
            log.error(String.format("upload2 error: size=%s uuid=%s", size, uuid), e);
            throw new ServiceError(ServiceError.UPLOAD_FAILED);
        }

        return uuid;
    }


    @Transactional(rollbackFor = Throwable.class)
    public Map<String, Object> update2(long userId, int type, String id_no,
                                       String first_name, String last_name, String birthday,
                                       String unit, String street, String city,
                                       int location, String zipCode, String geeToken,
                                       String id_front, String id_back,
                                       String time_limit, String authority, boolean isIos) {
        int state = KYC_NULL;
        DataMap kyc = data.queryOne("SELECT uid, id_no, state FROM tb_kyc WHERE uid = ?", userId);
        if (kyc != null && (kyc.getInt("state") == KYC_AUDIT_PASS || kyc.getInt("state") == KYC_UNAUDITED))
            throw new BadRequestError();


        Map<String, Object> result = new HashMap<>();
        //{"data":{"verification_score":0.9306188225746155},"status":200}
        //{"data":{"liveness_score":0.5, passed=false }     ,"status":200}
        try {
            if (StringUtils.isNotEmpty(geeToken)) {
                JSONObject resultMap = getOCRValidateResult(geeToken);
                if (resultMap != null && "200".equals(resultMap.getString("status"))) {
                    Float score = 0f;
                    JSONObject data = resultMap.getJSONObject("data");
                    if (isIos) {
                        score = data.getFloat("verification_score");
                        Boolean passed = data.getBoolean("passed");
                        if (passed && score > 0.75) {
                            state = KYC_AUDIT_PASS;
                            result.put("state", state);
                            result.put("passed", passed);
                        }
                    } else {
                        score = data.getFloat("verification_score");
                        if (score > 0.75) {
                            state = KYC_AUDIT_PASS;
                            result.put("state", state);
                        }
                    }

                    result.put("score", score);

                    log.info("userId={}, orc.score={}, getOCRValidateResult={}", userId, result.get("score"), resultMap);

                }
            }
        } catch (Exception e) {
            log.error("getOCRValidateResult error: ", e);
        }


        String sql = "SELECT uid, id_no, state FROM tb_kyc WHERE id_no = ?";
        List<DataMap> kycs = data.query(sql, id_no);

        if (kycs.size() >= 5) throw new ServiceError(ServiceError.KYC_SAME_ID_NO_LIMIT5);
        Integer reviewer_id = state == KYC_AUDIT_PASS ? 0 : null;
        Date review_time = state == KYC_AUDIT_PASS ? new Date() : null;
        if (kyc == null) {
            sql = "INSERT INTO tb_kyc(uid, type, id_no, first_name, last_name, birthday, unit, street, city, location, zipcode, state, id_front, id_back, time_limit, authority, update_time , reviewer_id, review_time) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, now(), ?,? )";
            data.update(sql, userId, type, id_no, first_name, last_name, birthday, unit, street, city, location, zipCode, state, id_front, id_back, time_limit, authority, reviewer_id, review_time);
        } else {
            sql = "UPDATE tb_kyc SET type = ?, id_no = ?, first_name = ?, last_name = ?, birthday = ?, unit = ?, street = ?, city = ?, location = ?, zipcode = ?, state = ?, " +
                    " id_front=?, id_back=?, time_limit=?, authority=?, update_time = NOW(), reviewer_id=?,review_time=? WHERE uid = ?";
            int rowN = data.update(sql, type, id_no, first_name, last_name, birthday, unit, street, city, location, zipCode, state,
                    id_front, id_back, time_limit, authority, reviewer_id, review_time,
                    userId);
//            if (rowN == 1)
//                state = KYC_NULL;
        }

        result.put("state", state);

        return result;
    }


    private String getKycValue(DataMap kyc, String key) {
        return kyc == null ? "" : kyc.getString(key);
    }

    private String getExtension(String type) {
        String ext = "jpg";
        if (type.endsWith("PNG") || type.endsWith("png"))
            ext = "png";
        return "." + ext;
    }

    //TODO 世界杯活动
    @Transactional(readOnly = true)
    public DataMap kycInfo(long userId) {
        String sql = "SELECT * FROM tb_kyc WHERE uid = ?";
        DataMap kyc = data.queryOne(sql, userId);
        return kyc;
    }


}
