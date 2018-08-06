package com.btctaxi.gate.service;

import genesis.common.DataMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 地区
 */
@Service
public class RegionService extends BaseService {
    @Transactional(readOnly = true)
    public List<DataMap> list() {
        String sql = "SELECT id, name, fullname, cname FROM tb_region WHERE state = ?";
        return data.query(sql, 1);
    }

    @Transactional(readOnly = true)
    public DataMap getRegion(long ip) {
        String sql = "SELECT region, code FROM tb_ip_region WHERE begin_ip <= ? AND end_ip >= ?";
        return data.queryOne(sql, ip, ip);
    }
}
