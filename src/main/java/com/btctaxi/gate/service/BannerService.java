package com.btctaxi.gate.service;

import genesis.common.DataMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BannerService extends BaseService {
    @Transactional(readOnly = true)
    public List<DataMap> list(int platform, int slot, String lang) {
        String sql = "SELECT id, picture, action, slot_index FROM tb_banner WHERE lang = ? AND state = 'ONLINE' AND platform = ? AND slot = ? ORDER BY slot_index ASC";
        List<DataMap> banners = data.query(sql, lang, platform, slot);

        if (banners == null || banners.size() == 0)
            banners = data.query(sql, "en", platform, slot);

        return banners;
    }
}
