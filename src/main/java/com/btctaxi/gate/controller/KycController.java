package com.btctaxi.gate.controller;

import com.alibaba.fastjson.JSONObject;
import com.amazonaws.services.s3.model.S3Object;
import com.google.common.collect.Maps;
import genesis.common.DataMap;
import genesis.gate.config.GeetestConfig;
import genesis.gate.error.ServiceError;
import genesis.gate.service.KycService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

import static genesis.gate.error.ServiceError.SIGNIN_AUTH_GEETEST_FAIL;

@Slf4j
@Controller
@RequestMapping("/gate/kyc")
public class KycController extends BaseController {
    private KycService kycService;

    @Autowired
    private GeetestConfig geetestConfig;

    public KycController(KycService kycService) {
        this.kycService = kycService;
    }

    @RequestMapping("/query")
    @ResponseBody
    public DataMap query(HttpServletRequest req) {
        DataMap kyc = kycService.query(sess.getId(), "https://" + req.getServerName());
        if (kyc != null && !kyc.isEmpty())
            sess.setKyc(kyc.getInt("state"));
        return kyc;
    }

    @RequestMapping("/update")
    @ResponseBody
    public Map update(@RequestParam int type, @RequestParam String id_no, @RequestParam String first_name, @RequestParam String last_name,
                      @RequestParam String birthday, @RequestParam(required = false) String unit, String street,
                      @RequestParam(required = false) String city,
                      @RequestParam int location, @RequestParam(required = false) String zipcode) {

        Map map = kycService.update(sess.getId(), type, id_no, first_name, last_name, birthday, unit, street, city, location, zipcode);
        sess.setKyc((Integer) map.get("state"));
        return map;
    }

    @PostMapping("/upload")
    @ResponseBody
    public void upload(@RequestParam(required = false) MultipartFile passport, @RequestParam(required = false) MultipartFile id_front,
                       @RequestParam(required = false) MultipartFile id_back, @RequestParam(required = false) MultipartFile id_hold,
                       @RequestParam(required = false) MultipartFile guardian, @RequestParam(required = false) MultipartFile other) {
        int state = kycService.upload(sess.getId(), sess.getEmail(), passport, id_front, id_back, id_hold, guardian, other);
        sess.setKyc(state);
    }


    /**
     * fh
     *
     * @return
     */
    @RequestMapping("/update2")
    @ResponseBody
    public Map update2(@RequestParam int type, @RequestParam String id_no, @RequestParam String first_name, @RequestParam String birthday, String street,
                       @RequestParam(required = false) String geeToken, @RequestParam String id_front_key, @RequestParam String id_back_key,
                       @RequestParam(required = false) String last_name,
                       @RequestParam(required = false) String unit,
                       @RequestParam(required = false) String city, @RequestParam(required = false) String zipcode,
                       @RequestParam(required = false) String time_limit, @RequestParam(required = false) String authority, HttpServletRequest httpServletRequest) {

        String header = httpServletRequest.getHeader("User-Agent");


        Map map = kycService.update2(sess.getId(), type, id_no, first_name, last_name, birthday, unit, street,
                city, 86, zipcode, geeToken, id_front_key, id_back_key, time_limit, authority, header.contains("os=ios"));
        sess.setKyc((Integer) map.get("state"));
        return map;
    }

    /**
     * 返回图片 key
     *
     * @param file
     * @return
     */
    @PostMapping("/upload2")
    @ResponseBody
    public Map<String, Object> upload2(@RequestParam MultipartFile file) {
        String key = kycService.upload2(sess.getId(), file);
        HashMap<String, Object> map = Maps.newHashMap();
        map.put("key", key);
        return map;
    }


    @RequestMapping("/read")
    public ResponseEntity<Resource> read(@RequestParam String type) {
        S3Object o = kycService.getResource(sess.getId(), type);
        InputStreamResource res = new InputStreamResource(o.getObjectContent());
        return ResponseEntity.ok().contentLength(o.getObjectMetadata().getContentLength()).contentType(MediaType.APPLICATION_OCTET_STREAM).body(res);
    }


    @RequestMapping("/ocr")
    @ResponseBody
    @Any
    public JSONObject ocr(String token) {

        try {
            JSONObject resultMap = kycService.getOCRValidateResult(token);
            log.info("result = " + resultMap);
            if ("200".equals(resultMap.getString("status"))) {
                return resultMap.getJSONObject("data");
            }

            return resultMap;

        } catch (Exception e) {
            log.error("call OCR error ", e);
            throw new ServiceError(SIGNIN_AUTH_GEETEST_FAIL);
        }
    }


}
