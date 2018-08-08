package com.btctaxi.gate.service;


import com.btctaxi.common.DataMap;
import com.btctaxi.gate.error.ServiceError;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PreSellService extends BaseService {

    @Transactional(readOnly = true)
    public Map<String, Object> query(long id) {
        Map<String, Object> map = new HashMap<>();

        String sql = "SELECT total, locked_year, bonus, state FROM tb_presell_total WHERE uid = ?";
        DataMap total = data.queryOne(sql, id);

        if (total == null)
            return null;

        map.put("total", total);

        if (total.getInt("state") != 0) {
            sql = "SELECT amount, unlock_time, state FROM tb_presell_detail WHERE uid = ?";
            List<DataMap> list = data.query(sql, id);

            map.put("list", list);
        }

        return map;
    }

    @Transactional(rollbackFor = Throwable.class)
    public void lock(long amount, long id) {
        String sql = "SELECT total, locked_year, bonus, state FROM tb_presell_total WHERE uid = ?";
        DataMap total = data.queryOne(sql, id);

        if (total == null || total.getInt("state") != 0 || total.getLong("total") < amount)
            throw new ServiceError(ServiceError.SERVER_DATA_EXCEPTION);

        double bonus = Math.ceil(amount * 0.2);
        sql = "UPDATE tb_presell_total SET locked_year = ?, bonus = ?, state = ? WHERE uid = ?";
        data.update(sql, amount, bonus, 1, id);

        sql = "INSERT INTO tb_presell_detail(uid, amount, unlock_time, state) VALUES (?, ?, ?, ?)";
        double quarter = Math.ceil((total.getLong("total") - amount) / 4);
        data.update(sql, id, quarter, "2018-08-01 00:00:00", 0);
        data.update(sql, id, quarter, "2018-11-01 00:00:00", 0);
        data.update(sql, id, quarter, "2019-02-01 00:00:00", 0);
        data.update(sql, id, total.getLong("total") - amount - quarter * 3, "2019-05-01 00:00:00", 0);
    }
}

//package genesis.gate.service;
//
//import genesis.common.DataMap;
//import genesis.gate.error.ServiceError;
//
//import org.apache.commons.lang3.time.FastDateFormat;
//import org.joda.time.DateTime;
//import org.joda.time.Period;
//import org.joda.time.PeriodType;
//import org.joda.time.format.DateTimeFormat;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.math.BigDecimal;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@Service
//public class PreSellService extends BaseService {
//
//    @Transactional(readOnly = true)
//    public Map<String, Object> query(long id) {
//        Map<String, Object> map = new HashMap<>();
//
//        String sql = "SELECT total, locked_year, bonus, state FROM tb_presell_total WHERE uid = ?";
//        DataMap total = data.queryOne(sql, id);
//
//        if (total == null)
//            return null;
//
//        map.put("total", total);
//
//        if (total.getInt("state") != 0) {
//            sql = "SELECT amount, unlock_time, state FROM tb_presell_detail WHERE uid = ?";
//            List<DataMap> list = data.query(sql, id);
//
//            map.put("list", list);
//        }
//
//        return map;
//    }
//
//    /**
//     * 2018-08-01 00:00:00 前发 25%, 之后每天发, 精度归到最后一天
//     *
//     * @param amount
//     * @param id
//     */
//    @Transactional(rollbackFor = Throwable.class)
//    public boolean lock(Long amount, long id) {
//        String sql = "SELECT total, locked_year, bonus, state FROM tb_presell_total WHERE uid = ?";
//        DataMap total = data.queryOne(sql, id);
//
//
//        if (total == null || total.getInt("state") != 0 || total.getLong("total") < amount) {
//            throw new ServiceError(ServiceError.SERVER_DATA_EXCEPTION);
//        }
//
//        Long totalAmount = total.getLong("total");
//
//        //0 初始化, 1,用户设置&拆分, 2:完事
//        long bonus = (long) Math.ceil(amount * 0.2);
//        sql = "UPDATE tb_presell_total SET locked_year = ?, bonus = ?, state = ? WHERE uid = ?";
//        data.update(sql, amount, bonus, 1, id);
//
//
//        String startDayStr = "2018-08-01";
//
//        String presellSql = "INSERT INTO tb_presell_detail(uid, amount, unlock_time, state) VALUES (?, ?, ?, ?)";
//        long quarter = (long) Math.ceil(amount / 4);
//        data.update(presellSql, id, quarter, startDayStr, 0);
//
//
//        DateTime startDay = DateTimeFormat.forPattern("yyyy-MM-dd").parseDateTime(startDayStr);
//        DateTime endDay = startDay.plusMonths(9);
//        //剩余的 270天 分完
//        int days = new Period(startDay, endDay, PeriodType.days()).getDays();
//
//        long avgLockAmount = (amount - quarter) / days;
//        for (int i = 2; i <= days + 1; i++) {
//            if (i == days) {
//                long lastAmount = totalAmount + bonus - quarter - avgLockAmount * (days - 1);
//                data.update(presellSql, id, lastAmount, startDay.plusDays(i).toDate(), 0);
//            } else {
//                data.update(presellSql, id, avgLockAmount, startDay.plusDays(i).toDate(), 0);
//            }
//        }
//
//        //balance + locking
//        String balanceSql = "select * from accounting_balance where user_id = ? and currency_name = 'TB'";
//        DataMap dataMap = data.queryOne(balanceSql, id);
//        BigDecimal lockingAmount = new BigDecimal(totalAmount + bonus);
//        if (dataMap != null) {
//            BigDecimal locking = (BigDecimal) dataMap.get("locking");
//            lockingAmount = locking.add(lockingAmount);
//            balanceSql = "update accounting_balance set  locking = ? where user_id = ? and currency_name = 'TB' and locking = ? ";
//            int update = data.update(balanceSql, lockingAmount, id, locking);
//            return update > 0;
//        } else {
//            balanceSql = " INSERT INTO accounting_balance (user_id, currency_name, available, ordering, withdrawing, locking)" +
//                    " VALUES (?, 'TB', 0.0 , 0.0 , 0.0 , ?);";
//            long insert = data.update(balanceSql, id, lockingAmount);
//            return insert > 0;
//        }
//
//
//    }
//
//
//    //解锁 & 加钱服务 , 锁量需要加到现有结构中 balance , unavailable 既 : 加到 locking 字段中
//    @Transactional(rollbackFor = Throwable.class)
//    public Boolean refund(String endDate, Long userId) {
//        String now = FastDateFormat.getInstance("yyyy-MM-dd").format(new Date());
////        if (endDate.compareTo(now) > 0 ) return false;
//        String sql = "select sum(amount) amount from tb_presell_detail where state = 0 and unlock_time <= ? and uid = ? ";
//        Map<String, Object> list = data.queryOne(sql, endDate, userId);
//
//        BigDecimal amount = (BigDecimal) list.get("amount");
//
//        sql = "update tb_presell_detail set state = 1 and unlock_time <= ? and uid = ?";
//        int update = data.update(sql, endDate, userId);
//        if (update > 0) {
//
//            String balanceSql = "select * from accounting_balance where user_id = ? and currency_name = 'TB'";
//            DataMap dataMap = data.queryOne(balanceSql, userId);
//            BigDecimal available = (BigDecimal) dataMap.get("available");
//            BigDecimal locking = (BigDecimal) dataMap.get("locking");
//
//            BigDecimal availableAmount = available.add(amount);
//            BigDecimal lockingAmount = locking.subtract(amount);
//
//            balanceSql = "update accounting_balance set  available = ?, locking=? where user_id = ? and currency_name = 'TB' and available = ? ";
//            int updateNum = data.update(balanceSql, availableAmount, lockingAmount, userId, available);
//            return updateNum > 0;
//        }
//        return false;
//    }
//
//    //给前段提供接口
//
//
//}
