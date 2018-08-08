package com.btctaxi.service.tb;

import com.btctaxi.common.Data;
import com.btctaxi.common.DataMap;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.FastDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

@Service
@Slf4j
public class PreSellService   {

    @Autowired
    protected Data data;


    //解锁 & 加钱服务 , 锁量需要加到现有结构中 balance , unavailable 既 : 加到 locking 字段中
    @Transactional(rollbackFor = Throwable.class)
    public Boolean refund(String endDate, Long userId) {
        String now = FastDateFormat.getInstance("yyyy-MM-dd").format(new Date());
//        if (endDate.compareTo(now) > 0 ) return false;
        String sql = "select sum(amount) amount from tb_presell_detail where state = 0 and unlock_time <= ? and uid = ? ";
        Map<String, Object> list = data.queryOne(sql, endDate, userId);

        BigDecimal amount = (BigDecimal) list.get("amount");

        sql = "update tb_presell_detail set state = 1 and unlock_time <= ? and uid = ?";
        int update = data.update(sql, endDate, userId);
        if (update > 0) {

            String balanceSql = "select * from accounting_balance where user_id = ? and currency_name = 'TB'";
            DataMap dataMap = data.queryOne(balanceSql, userId);
            BigDecimal available = (BigDecimal) dataMap.get("available");
            BigDecimal locking = (BigDecimal) dataMap.get("locking");

            BigDecimal availableAmount = available.add(amount);
            BigDecimal lockingAmount = locking.subtract(amount);

            balanceSql = "update accounting_balance set  available = ?, locking=? where user_id = ? and currency_name = 'TB' and available = ? ";
            int updateNum = data.update(balanceSql, availableAmount, lockingAmount, userId, available);
            return updateNum > 0;
        }
        return false;
    }


}
