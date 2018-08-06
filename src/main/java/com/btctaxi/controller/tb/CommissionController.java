package genesis.accounting.controller.tb;

import com.google.common.collect.Lists;
import genesis.accounting.config.CommonConfig;
import genesis.accounting.config.RedisKeyConfig;
import genesis.accounting.dao.tb.TbRefundCommissionRepository;
import genesis.accounting.service.tb.TbCommissionService;
import genesis.common.Data;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.FastDateFormat;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static genesis.accounting.config.RedisKeyConfig.CHANGE_RATE_MINUTE;

/**
 * 邀请返佣
 * <p>
 * User: guangtou
 * Date: 2018/7/5 17:08
 */
@RestController
@RequestMapping("/gate/commission")
@Slf4j
public class CommissionController {


    @Autowired
    private TbCommissionService tbCommissionService;


    @Autowired
    private Data data;

    @Autowired
    private TbRefundCommissionRepository tbRefundCommissionRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private CommonConfig commonConfig;


    @RequestMapping("/changeRateSnapshot")
    @ApiOperation("每分钟的汇率快照")
    public void changeRate() {
        // 调用接口 ,  获取汇率, 转换 feeTb feeCurrency feeProduct --> TB / BTC
        //String result = "{\"EOS\": \"0.00576\", \"USDT\": \"0.6657\", \"BTC\": \"0.000006657\" , \"TB\": \"0.000006657\", \"ETH\": \"0.000006657\"}";
        String changeRate = restTemplate.getForObject(commonConfig.getChangeRateUrl(), String.class);

        DateTime now = DateTime.now();
        String k = CHANGE_RATE_MINUTE + FastDateFormat.getInstance("yyyy-MM-dd").format(now.toDate());
        int minuteOfDay = now.getMinuteOfDay();
//        redisTemplate.opsForList().set(k, minuteOfDay, changeRate);
        Long push = redisTemplate.opsForList().leftPush(k, changeRate);

    }

    //汇总 邀请 ID | 已经获取佣金 | 已获取返利
    @RequestMapping("/avgChangeRate")
    @ApiOperation("返佣列表")
    public HashMap<String, BigDecimal> avgChangeRate(@RequestParam String day) {
        return tbCommissionService.getAvgChangeRate(day);
    }


    @RequestMapping("/balanceSnapshot")
    @ApiOperation("每小时的持仓快照")
    public Boolean balanceSnapshot() {
        tbCommissionService.balanceSnapshot();
        return true;
    }


    @RequestMapping("/create")
    @ApiOperation("创建返利返佣")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "day", value = "日期 yyyy-MM-dd", required = true, dataType = "String")
    })
    public Boolean create(@RequestParam(required = false) String day) {
        Date date;
        try {
            date = FastDateFormat.getInstance("yyyy-MM-dd").parse(day);
        } catch (Exception e) {
            date = DateTime.now().withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0).toDate();
        }
        return tbCommissionService.create(date);
    }


    @RequestMapping("/createAll")
    @ApiOperation("创建")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "day", value = "日期 yyyy-MM-dd", required = true, dataType = "String")
    })
    public Boolean createAll() {
        for (int i = 0; i < 300; i++) {
            Date date = DateTime.now().plusDays(-1 * i).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0).toDate();
            try {
                tbCommissionService.create(date);
            } catch (Exception e) {
                log.error("tbCommissionService.create", e);
            }
        }
        return true;
    }

    @RequestMapping("/createDividend")
    @ApiOperation("创建分红")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "day", value = "日期 yyyy-MM-dd", required = true, dataType = "String")
    })
    public Boolean createDividend(@RequestParam(required = false) String day) {
        DateTime endDate;
        try {
            endDate = DateTimeFormat.forPattern("yyyy-MM-dd").parseDateTime(day);
        } catch (Exception e) {
            endDate = DateTime.now().withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
        }
//        tbCommissionService.createDividend(endDate.plusDays(-1).toDate(), endDate.toDate());
        tbCommissionService.createDividend2(endDate.plusDays(-1).toDate(), endDate.toDate());


        return true;
    }


    @RequestMapping("/test3")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "day", value = "日期 yyyy-MM-dd", required = true, dataType = "String")
    })
    public Boolean createDividend3(@RequestParam(required = false) String day) {
        DateTime endDate;
        try {
            endDate = DateTimeFormat.forPattern("yyyy-MM-dd").parseDateTime(day);
        } catch (Exception e) {
            endDate = DateTime.now().withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
        }
        tbCommissionService.createDividend3(endDate.plusDays(-1).toDate(), endDate.toDate());


        return true;
    }

    @RequestMapping("/listConfig")
    public List<String> listConfig() {
        ArrayList<String> keys = Lists.newArrayList(
                RedisKeyConfig.CommonConfig.REFUND_COMMISSION_RATE.name(),
                RedisKeyConfig.CommonConfig.REFUND_COMMISSION_UNIT.name(),
                RedisKeyConfig.CommonConfig.REFUND_LOCKE_RATE.name(),
                RedisKeyConfig.CommonConfig.REFUND_LOCKE_UNIT.name(),
                RedisKeyConfig.CommonConfig.REFUND_PROFIT_RATE.name(),
                RedisKeyConfig.CommonConfig.REFUND_PROFIT_UNIT.name(),
                RedisKeyConfig.CommonConfig.DIVIDEND_RATE.name(),
                RedisKeyConfig.CommonConfig.DIVIDEND_UNIT.name(),
                RedisKeyConfig.CommonConfig.MINE_RATE.name(),
                RedisKeyConfig.CommonConfig.MINE_UNIT.name(),
                RedisKeyConfig.CommonConfig.THINK_BIT.name()
//                        RedisKeyConfig.CommonConfig.USER_UP_LIMIT.name()
        );
        List<String> config = redisTemplate.<String, String>opsForHash().multiGet(RedisKeyConfig.COMMON_CONFIG, keys);

        for (int i = 0; i < config.size(); i++) {
            log.info("load config by redis , list.k={}, v={}", keys.get(i), config.get(i));

        }

        return config;
    }


    @RequestMapping("/config")
    public String config(@RequestParam String k) {
        return redisTemplate.<String, String>opsForHash().get(RedisKeyConfig.COMMON_CONFIG, k);
    }

    /**
     * 返利结算
     *
     * @param day
     * @return
     */
    @RequestMapping("/refund")
    public Boolean refund(@RequestParam(required = false) String day) {
        DateTime endDate;
        try {
            endDate = DateTimeFormat.forPattern("yyyy-MM-dd").parseDateTime(day);
        } catch (Exception e) {
            endDate = DateTime.now().withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
        }
        tbCommissionService.refund(endDate.plusDays(-1).toDate(), endDate.toDate());
        return true;
    }

    /**
     * 分红 结算
     *
     * @param day
     * @return
     */
    @RequestMapping("/refundDividend")
    public Boolean refundDividend(@RequestParam(required = false) String day) {
        DateTime endDate;
        try {
            endDate = DateTimeFormat.forPattern("yyyy-MM-dd").parseDateTime(day);
        } catch (Exception e) {
            endDate = DateTime.now().withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
        }
        tbCommissionService.refundDividend(endDate.plusDays(-1).toDate(), endDate.toDate());
        return true;
    }

}
