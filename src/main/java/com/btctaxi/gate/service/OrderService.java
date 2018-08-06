package com.btctaxi.gate.service;

import genesis.common.DataMap;
import genesis.gate.error.BadRequestError;
import genesis.gate.error.ConcurrentError;
import genesis.gate.error.ServiceError;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * 订单
 */
@Service
public class OrderService extends BaseService {
    private final String BUY = "BUY";
    private final String SELL = "SELL";

    private final int STATUS_DONE = 1;
    private final int STATUS_PART = 2;
    private final int STATUS_REMOVED = 3;
    private final int STATUS_POST_CANCEL = 4;
    private final int STATUS_EMPTY = 5;

    @Transactional(rollbackFor = Throwable.class)
    @Retryable(value = ConcurrentError.class, backoff = @Backoff(delay = 100L, multiplier = 1))
    public long create(String pairName, long userId, String directionStr, BigDecimal price, BigDecimal amount, boolean postOnly) {
        int direction = BUY.equals(directionStr) ? 1 : 2;

        String sql = "SELECT product_name, currency_name, price_scale, amount_scale, min_amount, max_amount, min_total, max_total FROM tb_pair WHERE name = ?";
        DataMap pair = data.queryOne(sql, pairName);
        if (pair == null)
            throw new ServiceError(ServiceError.ORDER_CREATE_PAIR_NOT_EXISTS);

        String pairProductName = pair.getString("product_name");
        String pairCurrencyName = pair.getString("currency_name");

        int priceScale = pair.getInt("price_scale");
        int amountScale = pair.getInt("amount_scale");
        BigDecimal minAmount = pair.getBig("min_amount");
        BigDecimal maxAmount = pair.getBig("max_amount");
        BigDecimal minTotal = pair.getBig("min_total");
        BigDecimal maxTotal = pair.getBig("max_total");

        price = price.setScale(priceScale, BigDecimal.ROUND_DOWN);
        amount = amount.setScale(amountScale, BigDecimal.ROUND_DOWN);

        if (price.compareTo(BigDecimal.ZERO) < 0)
            throw new BadRequestError();

        if (amount.compareTo(minAmount) < 0)
            throw new ServiceError(ServiceError.ORDER_CREATE_AMOUNT_ILLEGAL_LT_MIN);
        if (amount.compareTo(maxAmount) > 0)
            throw new ServiceError(ServiceError.ORDER_CREATE_AMOUNT_ILLEGAL_GT_MAX);

        if (price.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal total = price.multiply(amount);
            if (total.compareTo(minTotal) < 0 || total.compareTo(maxTotal) > 0)
                throw new ServiceError(ServiceError.ORDER_CREATE_TOTAL_ILLEGAL);
        }

        String currencyName = direction == 1 ? pairCurrencyName : pairProductName;
        sql = "SELECT available FROM accounting_balance WHERE user_id = ? AND currency_name = ?";
        DataMap balance = data.queryOne(sql, userId, currencyName);
        if (balance == null)
            throw new ServiceError(ServiceError.ORDER_BALANCE_NOT_ENOUGH);
        BigDecimal available = balance.getBig("available");
        if (available.compareTo(BigDecimal.ZERO) == 0)
            throw new ServiceError(ServiceError.ORDER_BALANCE_NOT_ENOUGH);

        BigDecimal expect;
        if (direction == 1) {
            if (price.compareTo(BigDecimal.ZERO) == 0)
                expect = available;
            else
                expect = price.multiply(amount);
        } else
            expect = amount;
        if (available.compareTo(expect) < 0)
            throw new ServiceError(ServiceError.ORDER_BALANCE_NOT_ENOUGH);

        sql = "INSERT INTO tb_order(pair_name, user_id, direction, price, amount, post_only, unclosed, locked, product_name, currency_name) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        long orderId = data.insert(sql, pairName, userId, direction, price, amount, postOnly, amount, expect, pairProductName, pairCurrencyName);

        sql = "INSERT INTO tb_order_active(id, pair_name, user_id, direction, price, amount, post_only, unclosed, product_name, currency_name) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        data.update(sql, orderId, pairName, userId, direction, price, amount, postOnly, amount, pairProductName, pairCurrencyName);

        sql = "UPDATE accounting_balance SET available = available - ? WHERE user_id = ? AND currency_name = ? AND available = ?";
        int rowN = data.update(sql, expect, userId, currencyName, available);
        if (rowN != 1)
            throw new ConcurrentError();

        return orderId;
    }

    @Transactional(readOnly = true)
    public DataMap get(long userId, long orderId) {
        String sql = "SELECT pair_name FROM tb_order_active WHERE user_id = ? AND id = ? AND price > 0";
        DataMap order = data.queryOne(sql, userId, orderId);
        if (order == null)
            throw new ServiceError(ServiceError.ORDER_REMOVE_ORDER_NOT_EXISTS);
        return order;
    }

    @Transactional(readOnly = true)
    public List<DataMap> getUnclosed(long userId) {
        String sql = "SELECT id, pair_name FROM tb_order_active WHERE user_id = ? AND price > 0";
        List<DataMap> orders = data.query(sql, userId);
        return orders;
    }

    @Transactional(readOnly = true)
    public List<DataMap> query(long userId, long orderId) {
        String sql = "SELECT id, price, amount, create_time, fee_tb, fee_currency, fee_product FROM tb_transaction WHERE user_id = ? AND order_id = ?";
        List<DataMap> transactions = data.query(sql, userId, orderId);
        return transactions;
    }

    @Transactional(readOnly = true)
    public List<DataMap> active(String pair_name, long userId) {
        ArrayList<Object> params = new ArrayList<>();
        params.add(userId);
        StringBuilder sql = new StringBuilder("SELECT id, pair_name, direction AS side, price, amount, unclosed, create_time, deal_amount, deal_total AS total FROM tb_order_active WHERE user_id = ? AND price > 0 AND create_time <= NOW()");
        if (pair_name != null) {
            sql.append(" AND pair_name = ?");
            params.add(pair_name);
        }
        sql.append(" ORDER BY id DESC");

        List<DataMap> orders = data.query(sql.toString(), params.toArray());

        for (DataMap order : orders) {
            order.put("side", order.getInt("side") == 1 ? BUY : SELL);
            order.put("type", order.getBig("price").compareTo(BigDecimal.ZERO) == 0 ? "MARKET" : "LIMIT");
        }
        return orders;
    }

    @Transactional(readOnly = true)
    public List<DataMap> history(String pair_name, long userId, long startId, int size) {
        ArrayList<Object> params = new ArrayList<>();
        params.add(userId);
        StringBuilder sql = new StringBuilder("SELECT id, pair_name, direction AS side, price, amount, post_only, create_time, remove_time, deal_amount, deal_total AS total FROM tb_order WHERE user_id = ? AND unclosed = 0 AND create_time <= NOW()");
        if (startId != 0) {
            sql.append(" AND id < ?");
            params.add(startId);
        }
        if (pair_name != null) {
            sql.append(" AND pair_name = ?");
            params.add(pair_name);
        }
        sql.append(" ORDER BY id DESC LIMIT ?");
        params.add(size);
        List<DataMap> orders = data.query(sql.toString(), params.toArray());

        for (DataMap order : orders) {
            BigDecimal orderAmount = order.getBig("amount");
            BigDecimal dealAmount = order.getBig("deal_amount");
            boolean postOnly = order.getBoolean("post_only");
            Timestamp removeTime = order.getTime("remove_time");

            int status;
            if (dealAmount.compareTo(BigDecimal.ZERO) == 0)
                status = removeTime != null ? STATUS_REMOVED : postOnly ? STATUS_POST_CANCEL : STATUS_EMPTY;
            else
                status = dealAmount.compareTo(orderAmount) == 0 ? STATUS_DONE : STATUS_PART;

            order.put("status", status);
            order.put("type", order.getBig("price").compareTo(BigDecimal.ZERO) == 0 ? "MARKET" : "LIMIT");
            order.put("side", order.getInt("side") == 1 ? BUY : SELL);
        }
        return orders;
    }

    @Transactional(readOnly = true)
    public List<DataMap> closed(String pair_name, long userId, long startId, int size) {
        ArrayList<Object> params = new ArrayList<>();
        params.add(userId);
        StringBuilder sql = new StringBuilder("SELECT id, pair_name, direction AS side, price, amount, post_only, create_time, remove_time, deal_amount, deal_total AS total FROM tb_order WHERE user_id = ? AND unclosed = 0 AND deal_amount > 0 AND create_time <= NOW()");
        if (startId != 0) {
            sql.append(" AND id < ?");
            params.add(startId);
        }
        if (pair_name != null) {
            sql.append(" AND pair_name = ?");
            params.add(pair_name);
        }
        sql.append(" ORDER BY id DESC LIMIT ?");
        params.add(size);
        List<DataMap> orders = data.query(sql.toString(), params.toArray());

        for (DataMap order : orders) {
            BigDecimal orderAmount = order.getBig("amount");
            BigDecimal dealAmount = order.getBig("deal_amount");
            int status = dealAmount.compareTo(orderAmount) == 0 ? STATUS_DONE : STATUS_PART;

            order.put("status", status);
            order.put("type", order.getBig("price").compareTo(BigDecimal.ZERO) == 0 ? "MARKET" : "LIMIT");
            order.put("side", order.getInt("side") == 1 ? BUY : SELL);
        }
        return orders;
    }
}
