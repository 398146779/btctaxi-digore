package com.btctaxi.gate.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

@Service
public class TransferService extends BaseService {

    private AccountingService accountingService;

    public TransferService(AccountingService accountingService) {
        this.accountingService = accountingService;
    }

    public JSONArray list(long userId) {
        JSONArray currencies = accountingService.post("/currency/list");
        JSONObject cs = new JSONObject();
        for (int i = 0; i < currencies.size(); i++) {
            JSONObject item = currencies.getJSONObject(i);
            String chainName = item.getString("chain_name");
            String currencyName = item.getString("currency_name");
            cs.put(chainName + currencyName, item);
        }

        JSONArray deposits = accountingService.post("/deposit/list", "user_id", userId);
        for (int i = 0; i < deposits.size(); i++) {
            JSONObject deposit = deposits.getJSONObject(i);
            String chainName = deposit.getString("chain_name");
            String currencyName = deposit.getString("currency_name");
            String key = chainName + currencyName;

            deposit.put("type", "IN");
            int confirm = deposit.getIntValue("confirm");
            JSONObject item = cs.getJSONObject(key);
            int withdrawConfirm = item.getIntValue("withdraw_confirm");
            deposit.put("state", confirm < withdrawConfirm ? 101 : 102);
            String txid = deposit.getString("txid");
            deposit.put("scan_url", item.getString("scan_url").replace("{txid}", txid));
            deposit.put("create_time", new Timestamp(deposit.getLong("create_time")));
            deposit.put("exchange_confirm", item.getIntValue("exchange_confirm"));
            deposit.put("withdraw_confirm", item.getIntValue("withdraw_confirm"));
        }

        JSONArray withdraws = accountingService.post("/withdraw/list", "user_id", userId);
        for (int i = 0; i < withdraws.size(); i++) {
            JSONObject withdraw = withdraws.getJSONObject(i);

            String chainName = withdraw.getString("chain_name");
            String currencyName = withdraw.getString("currency_name");
            String key = chainName + currencyName;
            JSONObject item = cs.getJSONObject(key);

            String txid = withdraw.getString("txid");
            int state = withdraw.getIntValue("state");
            withdraw.put("scan_url", txid == null ? "" : item.getString("scan_url").replace("{txid}", txid));
            withdraw.put("create_time", new Timestamp(withdraw.getLong("create_time")));
            withdraw.put("exchange_confirm", item.getIntValue("exchange_confirm"));
            withdraw.put("withdraw_confirm", item.getIntValue("withdraw_confirm"));

            withdraw.put("type", "OUT");
            int viewState;
            switch (state) {
                case 1:
                    viewState = 202;
                    break;
                case 2:
                    viewState = 203;
                    break;
                case 3:
                    viewState = 204;
                    break;
                case 4:
                    viewState = 205;
                    break;
                case 5:
                    viewState = 206;
                    break;
                case 6:
                    viewState = 206;
                    break;
                case -1:
                    viewState = 209;
                    break;
                case -2:
                    viewState = 208;
                    break;
                case -3:
                    viewState = 208;
                    break;
                case -4:
                    viewState = 207;
                    break;
                case -5:
                    viewState = 207;
                    break;
                default:
                    viewState = 207;
                    break;
            }
            withdraw.put("state", viewState);
        }

        deposits.addAll(withdraws);
        deposits.sort((o1, o2) -> {
            JSONObject json1 = (JSONObject) o1;
            JSONObject json2 = (JSONObject) o2;
            Timestamp date1 = json1.getTimestamp("create_time");
            Timestamp date2 = json2.getTimestamp("create_time");
            return date2.compareTo(date1);
        });

        return deposits;
    }

    public JSONArray recent(long userId) {
        JSONArray currencies = accountingService.post("/currency/list");
        JSONObject cs = new JSONObject();
        for (int i = 0; i < currencies.size(); i++) {
            JSONObject item = currencies.getJSONObject(i);
            String chainName = item.getString("chain_name");
            String currencyName = item.getString("currency_name");
            cs.put(chainName + currencyName, item);
        }

        JSONArray deposits = accountingService.post("/deposit/list", "user_id", userId);
        for (int i = 0; i < deposits.size(); i++) {
            JSONObject deposit = deposits.getJSONObject(i);
            String chainName = deposit.getString("chain_name");
            String currencyName = deposit.getString("currency_name");
            String key = chainName + currencyName;
            JSONObject item = cs.getJSONObject(key);
            deposit.put("create_time", new Timestamp(deposit.getLong("create_time")));
            deposit.putAll(item);
        }

        JSONArray withdraws = accountingService.post("/withdraw/list", "user_id", userId);
        for (int i = 0; i < withdraws.size(); i++) {
            JSONObject withdraw = withdraws.getJSONObject(i);
            String chainName = withdraw.getString("chain_name");
            String currencyName = withdraw.getString("currency_name");
            String key = chainName + currencyName;
            JSONObject item = cs.getJSONObject(key);
            withdraw.put("create_time", new Timestamp(withdraw.getLong("create_time")));
            withdraw.putAll(item);
        }

        deposits.addAll(withdraws);
        deposits.sort((o1, o2) -> {
            JSONObject json1 = (JSONObject) o1;
            JSONObject json2 = (JSONObject) o2;
            Timestamp date1 = json1.getTimestamp("create_time");
            Timestamp date2 = json2.getTimestamp("create_time");
            return date2.compareTo(date1);
        });

        //去重
        Set<String> deduplicate = new HashSet<>();
        JSONArray result = new JSONArray();
        for (int i = 0, j = 0; i < deposits.size(); i++) {
            JSONObject o = deposits.getJSONObject(i);
            String chainName = o.getString("chain_name");
            String currencyName = o.getString("currency_name");
            String key = chainName + currencyName;
            if (!deduplicate.contains(chainName + currencyName)) {
                deduplicate.add(key);
                result.add(deposits.getJSONObject(i));
                if (++j == 6)
                    break;
            }
        }

        return result;
    }
}
