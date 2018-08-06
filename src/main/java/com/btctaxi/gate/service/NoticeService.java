package com.btctaxi.gate.service;

import genesis.common.DataMap;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 通知
 */
@Service
public class NoticeService extends BaseService {

    /**
     * 通知列表
     *
     * @param lang 地域
     * @return
     */
    @Transactional(readOnly = true)
    public List<DataMap> list(int platform, int slot, String lang) {
        String sql = "SELECT id, content, action, slot_index FROM tb_notice WHERE lang = ? AND state = 'ONLINE' AND platform = ? AND slot = ? ORDER BY slot_index ASC";
        List<DataMap> notices = data.query(sql, lang, platform, slot);

        if (notices == null || notices.size() == 0)
            notices = data.query(sql, "en", platform, slot);

        return notices;
    }
}
