package com.btctaxi.gate.service;

import com.alibaba.fastjson.JSONObject;
import genesis.common.DataMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EosRamService extends BaseService {
    @Autowired
    private RedisTemplate redisTemplate;

    private AccountingService accountingService;
    /**查询EOSRAM价格*/
    public static String GETEOSRAM = "/eosram/v1/getEosRamPrice";
    /**购买EOS RAM*/
    public static String BUYEOSRAM = "/eosram/v1/buyEosRam";
    /**卖出EOS RAM*/
    public static String SELLEOSRAM = "/eosram/v1/sellEosRam";
    /**卖出EOS RAM LIST */
    public static String SELLEOSRAMLIST = "/eosram/v1/getEosRamList";
    /**卖出EOS RAM LIST */
    public static String SELLEOSRAMLISTBYUSER = "/eosram/v1/getEosRamListByUser";

    public static String EOSRAMPRICE_KEY = "EOSRAMPRICE_KEY";

    public static String EOSRAMPRICE_LIST_KEY = "EOSRAMPRICE_LIST";

    private final long SECOND3 = 3L;

    public EosRamService(AccountingService accountingService){
        this.accountingService = accountingService;
    }

    public Map<String, BigDecimal> getEosRamPrice() {
        //从redis取当前链上eosram价格
        return kv.<String, BigDecimal>opsForHash().entries(EOSRAMPRICE_KEY);
    }

    /**
     * 每隔2秒取一次数据
     */
//    @Scheduled(fixedRate = 2000)
    public void timmerToEosRamPrice(){
        Map<String, String> map = new HashMap<>();
        JSONObject jsonObject = accountingService.post(GETEOSRAM);
        if(jsonObject !=null){
            boolean flag = Boolean.valueOf(jsonObject.get("key")+"");
            if(flag){
                BigDecimal eosRamPrice = jsonObject.getBigDecimal("price");
                map.put("eosRamPrice",eosRamPrice.toString());
                kv.<String, String>opsForHash().putAll(EOSRAMPRICE_KEY, map);
                //设置eosram过期时间为3秒
//                kv.expire(EOSRAMPRICE_KEY, SECOND3, TimeUnit.SECONDS);
            }
        }
    }

    public JSONObject buyEosRam(Long userId, BigDecimal eos_number) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.set("user_id", userId+"");
        params.set("eos_number",eos_number+"");
        JSONObject jsonObject = (JSONObject)accountingService.postMap(BUYEOSRAM,params);
        return jsonObject;
    }

    public List<DataMap> getEosRamListByUser(Long user_id, Integer size) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.set("size", size+"");
        params.set("user_id", user_id+"");
        List<DataMap> list =  (List<DataMap>)accountingService.postMap(SELLEOSRAMLISTBYUSER,params);
        kv.opsForList().leftPush(EOSRAMPRICE_LIST_KEY, String.valueOf(list));
        return list;
    }

    public List<DataMap> getEosRamList(Integer size) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.set("size", size+"");
        List<DataMap> list = (List<DataMap>)accountingService.postMap(SELLEOSRAMLIST,params);
        kv.opsForList().leftPush(EOSRAMPRICE_LIST_KEY, String.valueOf(list));
        return list;
    }

    public JSONObject sellEosRam(Long userId, BigDecimal eosram_number) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.set("user_id", userId+"");
        params.set("eosram_number",eosram_number+"");
        JSONObject dataMap = (JSONObject)accountingService.postMap(SELLEOSRAM,params);
        return dataMap;
    }
}
