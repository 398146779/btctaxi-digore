package com.btctaxi.gate.service;

import genesis.common.DataMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VersionService extends BaseService {
    @Transactional(readOnly = true)
    public DataMap version(String plat) {
        String sql = "SELECT id, plat, version, url, force_update FROM tb_mob_version WHERE plat = ? ORDER BY id DESC LIMIT 1";
        return data.queryOne(sql, plat);
    }
}
