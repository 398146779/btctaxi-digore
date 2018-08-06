package com.btctaxi.controller.tb;

import com.btctaxi.service.tb.PreSellService;
import com.btctaxi.common.Data;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
@RequestMapping("/gate/presell")
public class PreSellController   {

    @Autowired
    private PreSellService preSellService;

    @Autowired
    protected Data data;


    @RequestMapping("/refund")
    public void refund(@RequestParam(required = false) String day) {
        if (StringUtils.isEmpty(day)){
            day = FastDateFormat.getInstance("yyyy-MM-dd").format(new Date());
        }

        String sql = "select uid from tb_presell_total group by uid";
        String finalDay = day;
        data.queryForList(sql).forEach(map -> {
            Long uid = (Long) map.get("uid");
            preSellService.refund(finalDay, uid);
        });

    }
}
