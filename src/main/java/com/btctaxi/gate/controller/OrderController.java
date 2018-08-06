package com.btctaxi.gate.controller;

import genesis.common.DataMap;
import genesis.gate.error.BadRequestError;
import genesis.gate.error.ServiceError;
import genesis.gate.service.OrderService;
import genesis.gate.service.PublishService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/gate/order")
public class OrderController extends BaseController {
    private Logger log = LoggerFactory.getLogger(getClass());

    private final String LIMIT = "LIMIT";
    private final String MARKET = "MARKET";

    private final String BUY = "BUY";
    private final String SELL = "SELL";

    private final String ORDER_PREFIX = "ORDER_";
    private final String CREATE = "CREATE";
    private final String REMOVE = "REMOVE";

    private OrderService orderService;
    private PublishService publishService;

    public OrderController(OrderService orderService, PublishService publishService) {
        this.orderService = orderService;
        this.publishService = publishService;
    }

    /**
     * 下单
     *
     * @param pairName 交易对
     * @param side     操作方向
     * @param price    出价
     * @param amount   量
     */
    @RequestMapping("/create")
    public Map<String, Object> create(@RequestParam("pair_name") String pairName, @RequestParam String side, @RequestParam String type, @RequestParam(required = false) BigDecimal price, @RequestParam BigDecimal amount, @RequestParam(name = "post_only", required = false, defaultValue = "0") int postOnly) {
        if (MARKET.equals(type))
            price = BigDecimal.ZERO;
        else if (price == null || price.compareTo(BigDecimal.ZERO) <= 0)
            throw new BadRequestError();

        if (!BUY.equals(side) && !SELL.equals(side))
            throw new ServiceError(ServiceError.ORDER_CREATE_OP_VALUE_ILLEGAL);

        pairName = pairName.toUpperCase();

        long orderId = orderService.create(pairName, sess.getId(), side, price, amount, postOnly == 1);

        String channel = ORDER_PREFIX + pairName;
        String message = CREATE + "," + sess.getId() + "," + orderId;
        try {
            publishService.publish(channel, message);
        } catch (Throwable e) {
            log.error("engine not reachable");
        }

        Map<String, Object> map = new HashMap<>();
        map.put("order_id", orderId);
        return map;
    }

    /**
     * 撤单
     *
     * @param orderId 订单号
     */
    @RequestMapping("/remove")
    public void remove(@RequestParam("order_id") Long orderId) {
        DataMap order = orderService.get(sess.getId(), orderId);

        String pairName = order.getString("pair_name");
        pairName = pairName.toUpperCase();
        String channel = ORDER_PREFIX + pairName;
        String message = REMOVE + "," + sess.getId() + "," + orderId;
        try {
            publishService.publish(channel, message);
        } catch (Throwable e) {
            log.error("engine not reachable");
        }
    }

    /**
     * 撤销所有订单
     */
    @RequestMapping("/removeall")
    public void removeAll(@RequestParam(name = "pair_name", required = false) String pair) {
        List<DataMap> orders = orderService.getUnclosed(sess.getId());
        orders.forEach(order ->
        {
            String pairName = order.getString("pair_name");
            pairName = pairName.toUpperCase();

            if (pair == null || pairName.equals(pair)) {
                long orderId = order.getLong("id");
                String channel = ORDER_PREFIX + pairName;
                String message = REMOVE + "," + sess.getId() + "," + orderId;
                try {
                    publishService.publish(channel, message);
                } catch (Throwable e) {
                    log.error("engine not reachable");
                }
            }
        });
    }

    /**
     * 查询成交详情
     */
    @RequestMapping("/query")
    public Map<String, Object> query(@RequestParam("order_id") Long orderId) {
        List<DataMap> transactions = orderService.query(sess.getId(), orderId);
        Map<String, Object> map = new HashMap<>();
        map.put("items", transactions);
        return map;
    }

    /**
     * 活动委托
     */
    @RequestMapping("/active")
    public Map<String, Object> active(@RequestParam(required = false) String pair_name) {
        List<DataMap> orders = orderService.active(pair_name, sess.getId());
        Map<String, Object> map = new HashMap<>();
        map.put("items", orders);
        return map;
    }

    /**
     * 历史委托
     *
     * @param startId 起始id，0为不指定
     * @param size    结果集长度
     */
    @RequestMapping("/history")
    public Map<String, Object> history(@RequestParam(required = false) String pair_name, @RequestParam(value = "start", required = false) Long startId, @RequestParam(required = false) Integer size) {
        Optional<Long> startOp = Optional.ofNullable(startId);
        Optional<Integer> sizeOp = Optional.ofNullable(size);
        startId = startOp.orElse(0L);
        size = sizeOp.orElse(50);
        List<DataMap> orders = orderService.history(pair_name, sess.getId(), startId, size);
        Map<String, Object> map = new HashMap<>();
        map.put("items", orders);
        return map;
    }

    /**
     * 成交单
     *
     * @param startId 起始id，0为不指定
     * @param size    结果集长度
     */
    @RequestMapping("/closed")
    public Map<String, Object> closed(@RequestParam(required = false) String pair_name, @RequestParam(value = "start", required = false) Long startId, @RequestParam(required = false) Integer size) {
        Optional<Long> startOp = Optional.ofNullable(startId);
        Optional<Integer> sizeOp = Optional.ofNullable(size);
        startId = startOp.orElse(0L);
        size = sizeOp.orElse(50);
        List<DataMap> orders = orderService.closed(pair_name, sess.getId(), startId, size);
        Map<String, Object> map = new HashMap<>();
        map.put("items", orders);
        return map;
    }
}
