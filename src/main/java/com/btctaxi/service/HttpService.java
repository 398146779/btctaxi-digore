package com.btctaxi.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.btctaxi.config.ConstantUtils;
import com.btctaxi.config.WalletConfig;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class HttpService {
    private RestTemplate http;
    private WalletConfig walletConfig;

    private HttpService(RestTemplate http, WalletConfig walletConfig) {
        this.http = http;
        this.walletConfig = walletConfig;
    }

    public <T> T post(String uri, Object... pairs) {
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            if (pairs[i + 1] != null)
                map.put((String) pairs[i], pairs[i + 1]);
        }
        return (T) post(uri, map);
    }


    public <T> T post2(String uri, Object... pairs) {
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            if (pairs[i + 1] != null)
                map.put((String) pairs[i], pairs[i + 1]);
        }
        return (T) post2(uri, map);
    }


    public <T> T get(String uri, Object... pairs) {
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            if (pairs[i + 1] != null)
                map.put((String) pairs[i], pairs[i + 1]);
        }
        return (T) get(uri, map);
    }

    public Object post(String uri, Map<String, Object> map) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        Map<String, Object> params = sign(map);
        HttpEntity<Map<String, Object>> req = new HttpEntity<>(params, headers);
        ResponseEntity<String> res = http.postForEntity(walletConfig.getHost() + uri, req, String.class);
        String body = res.getBody();
        Object json = JSON.parse(body);
        if (json instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) json;
            Integer code = jsonObject.getInteger("code");
            if (code != null)
                throw new RuntimeException("钱包异常: " + jsonObject.getString("msg"));
        }
        return json;
    }


    public Object post2(String url, Map<String, Object> map) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        Map<String, Object> params = sign(map);
        HttpEntity<Map<String, Object>> req = new HttpEntity<>(params, headers);
        ResponseEntity<String> res = http.postForEntity(url, req, String.class);
        String body = res.getBody();
        Object json = JSON.parse(body);
        if (json instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) json;
            Integer code = jsonObject.getInteger("code");
            if (code != null)
                throw new RuntimeException("钱包异常: " + jsonObject.getString("msg"));
        }
        return json;
    }



    public Object get(String url, Map<String, Object> map) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
        Map<String, Object> params = sign(map);
        HttpEntity<Map<String, Object>> req = new HttpEntity<>(params, headers);
        ResponseEntity<String> res = http.getForEntity(url,String.class,req);
        String body = res.getBody();
        Object json = JSON.parse(body);
        if (json instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) json;
            Integer code = jsonObject.getInteger("code");
            if (code != null)
                throw new RuntimeException("钱包异常: " + jsonObject.getString("msg"));
        }
        return json;
    }

    private Map<String, Object> sign(Map<String, Object> map) {
        JSONObject json = new JSONObject(map);
        String data = json.toJSONString();
        String nonce = String.valueOf(System.currentTimeMillis());
        String signature = DigestUtils.sha256Hex(walletConfig.getKey() + nonce + data + walletConfig.getSecret());
        Map<String, Object> params = new HashMap<>();
        params.put("key", walletConfig.getKey());
        params.put("nonce", nonce);
        params.put("data", data);
        params.put("sign", signature);
        return params;
    }
}
