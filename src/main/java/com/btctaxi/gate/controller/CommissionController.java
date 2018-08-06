package com.btctaxi.gate.controller;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import genesis.common.Data;
import genesis.common.DataMap;
import genesis.gate.config.RedisKeyConfig;
import genesis.gate.util.Convert;
import genesis.gate.util.SensitiveInfoUtils;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 邀请返佣
 * <p>
 * User: guangtou
 * Date: 2018/7/5 17:08
 */

/**
 * 邀请返佣
 * <p>
 * User: guangtou
 * Date: 2018/7/5 17:08
 */
@RestController
@RequestMapping("/gate/commission")
@Slf4j
public class CommissionController extends BaseController {


    @Autowired
    private Data data;

    @Autowired
    private RedisTemplate redisTemplate;


    //成功推荐好友 : 邮箱 | 时间
    @RequestMapping("/investUserId")
    @ApiOperation("邀请人 ID")
    public Pair<String, String> investUserId() {
        Long user_id = sess.getId();
        return Pair.of("invitor_id", Convert._10_to_62(user_id));
    }

    @RequestMapping("/id")
    @ApiOperation("TAXI专用: 邀请人 ID")
    @Any
    public Pair<String, String> id(@RequestParam String email) {
        DataMap dataMap = data.queryOne("select id from tb_user where email = ? ", email);
        return Pair.of("url", "https://btctaxi.com/account/register?invitor=" + Convert._10_to_62(((Long) dataMap.get("id"))));
    }

    public static void main(String[] args) {
        int user_id = 11;
        System.out.println("Convert._10_to_62(user_id) = " + Convert._10_to_62(user_id));
    }


    //成功推荐好友 : 邮箱 | 时间
    @RequestMapping("/listInvest")
    @ApiOperation("汇总")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page", value = "页码从零开始", dataType = "Integer"),
            @ApiImplicitParam(name = "size", value = " 每页显示条数", dataType = "Integer"),
            @ApiImplicitParam(name = "user_id", value = " user_id", required = true, dataType = "Long")
    })
    public List<Map<String, Object>> tbInvestUserList() {
        String sql = "select a.email,a.create_time  from tb_user a  where    invitor_id = ? order  by create_time desc ";
        Long user_id = sess.getId();
//        user_id = 10726394L;
        List<Map<String, Object>> list = data.queryForList(sql, new Object[]{user_id})
                .stream().peek(o -> {
                    o.put("email", SensitiveInfoUtils.email((String) o.get("email")));
                    Timestamp create_time = ((Timestamp) o.get("create_time"));
                    o.put("create_time", FastDateFormat.getInstance("yyyy-MM-dd").format(create_time));
                }).collect(Collectors.toList());
        log.info("user_id={}, list.size={}", user_id, list.size());
        return list;

    }


    //汇总 邀请 ID | 已经获取佣金 | 已获取返利
    @RequestMapping("/listSummary")
    @ApiOperation("返佣列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "user_id", value = " user_id", required = true, dataType = "Long")
    })
    public Map<String, Object> listSummary(@RequestParam(defaultValue = "0") Integer page,
                                           @RequestParam(defaultValue = "100") Integer size) {
        Long user_id = sess.getId();

        String sql1 = "select sum(re.refund_commission_amount) as refund_commission_amount , re.refund_commission_unit " +
                " from tb_refund_commission re where re.invitor_id = ? " +
                " group by re.invitor_id , re.refund_commission_unit";

        Map<String, Object> commissionMaps = data.queryForMap(sql1, user_id);

        String sql2 = " select sum(re.refund_profit_amount) as refund_profit_amount , re.refund_profit_unit " +
                "  from tb_refund_commission re where re.sub_user_id = ? and invitor_id is not null " +
                "  group by re.sub_user_id , re.refund_profit_unit ";

        Map<String, Object> profitMaps = data.queryForMap(sql2, user_id);

        HashMap<String, Object> map = Maps.newHashMap();
        if (commissionMaps != null) {
            map.put("refund_commission_amount", commissionMaps.get("refund_commission_amount"));
            map.put("refund_commission_unit", commissionMaps.get("refund_commission_unit"));
        }
        if (profitMaps != null) {
            map.put("refund_profit_amount", profitMaps.get("refund_profit_amount"));
            map.put("refund_profit_unit", profitMaps.get("refund_profit_unit"));
        }

        if (CollectionUtils.isEmpty(map)) {
            map.put("refund_commission_amount", "0");
            map.put("refund_commission_unit", "BTC");
            map.put("refund_profit_amount", "0");
            map.put("refund_profit_unit", "BTC");
        }

        return map;
    }

    //最近返佣 list : 佣金|邮箱|时间
    @RequestMapping("/listCommission")
    @ApiOperation("返佣列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "user_id", value = " user_id", required = true, dataType = "Long")
    })
    public List<Map<String, Object>> listCommission(@RequestParam(defaultValue = "0") Integer page,
                                                    @RequestParam(defaultValue = "100") Integer size) {
        String sql = " select  sum(re.refund_commission_amount) as refund_commission_amount , re.sub_user_id, re.sub_email, re.create_time, re.refund_commission_unit" +
                " from tb_refund_commission re where re.invitor_id = ?" +
                " group by re.sub_user_id, re.sub_email, re.create_time, re.refund_commission_unit" +
                " order  by re.create_time desc";
        Long user_id = sess.getId();
        return data.queryForList(sql, user_id).stream().peek(o -> o.put("sub_email", SensitiveInfoUtils.email((String) o.get("sub_email"))))
                .collect(Collectors.toList());
    }

    //最近的反利 list : 佣金|邮箱|时间


    @RequestMapping("/listProfit")
    @ApiOperation("返利列表")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "user_id", value = " user_id", required = true, dataType = "Long")
    })
    public List<Map<String, Object>> listProfit(@RequestParam(defaultValue = "0") Integer page,
                                                @RequestParam(defaultValue = "100") Integer size) {
        String sql = " SELECT sum(re.refund_profit_amount) AS refund_profit_amount,  re.create_time, re.refund_profit_unit " +
                " FROM tb_refund_commission re " +
                " WHERE re.sub_user_id = ? and re.invitor_id is not null " +
                " GROUP BY re.sub_user_id,  re.create_time, re.refund_profit_unit" +
                " order  by re.create_time desc  ";
        Long user_id = sess.getId();
        return data.queryForList(sql, user_id).stream().peek(o -> o.put("sub_email", SensitiveInfoUtils.email((String) o.get("sub_email"))))
                .collect(Collectors.toList());
    }


    //排名 : 邮箱|佣金
    @RequestMapping("/leaderBoard")
    @ApiOperation("返利列表")
    @Any
    public List<Map<String, Object>> leaderBoard() {
        Object leaderBoardOpen = redisTemplate.opsForHash().get(RedisKeyConfig.COMMON_CONFIG, RedisKeyConfig.CommonConfig.LEADER_BOARD_OPEN.name());

        if (leaderBoardOpen == null) {
            return Lists.newArrayList();
        }
        String sql = "SELECT sum(re.refund_commission_amount) AS refund_commission_amount , re.email sub_email, re.refund_commission_unit " +
                " FROM tb_refund_dividend re WHERE re.refund_commission_amount > 0" +
                " GROUP BY re.email, re.refund_commission_unit " +
                " ORDER BY refund_commission_amount DESC LIMIT 4";
        return data.queryForList(sql).stream()
                .peek(o -> o.put("sub_email", SensitiveInfoUtils.email((String) o.get("sub_email"))))
                .collect(Collectors.toList());
    }


}





