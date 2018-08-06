package com.btctaxi.common;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import io.shardingjdbc.core.api.ShardingDataSourceFactory;
import io.shardingjdbc.core.api.config.ShardingRuleConfiguration;
import io.shardingjdbc.core.api.config.TableRuleConfiguration;
import io.shardingjdbc.core.api.config.strategy.InlineShardingStrategyConfiguration;
import io.shardingjdbc.core.keygen.DefaultKeyGenerator;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.net.InetAddress;
import java.sql.SQLException;
import java.util.*;

/**
 * 数据源配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "datasource")
public class DataSourceConfig {
    private Properties properties;
    private String driverClassName;
    private String columnName;
    private String tables;
    private String keyGeneratorTables;
    private Node node;
    private List<Node> nodes;

    @Data
    public static class Node {
        private String jdbcUrl;
        private String username;
        private String password;
        private int maximumPoolSize;
    }

    public DataSource build() throws SQLException {
        Map<String, DataSource> dataSourceMap = new HashMap<>();

        //默认数据源
        dataSourceMap.put("node", create(node.jdbcUrl, node.username, node.password, node.maximumPoolSize));
        ShardingRuleConfiguration config = new ShardingRuleConfiguration();

        //Sharding数据源
        if (nodes != null && !nodes.isEmpty()) {
            for (int i = 0; i < nodes.size(); i++) {
                Node n = nodes.get(i);
                dataSourceMap.put("node" + i, create(n.jdbcUrl, n.username, n.password, n.maximumPoolSize));
            }
            //表规则
            config.getBindingTableGroups().add(tables);
            String[] logicTables = tables.split(",");
            Set<String> keyGenTables = keyGeneratorTableSet();
            for (String logicTable : logicTables) {
                logicTable = logicTable.trim();
                TableRuleConfiguration cfg = new TableRuleConfiguration();
                cfg.setLogicTable(logicTable);
                cfg.setActualDataNodes("node${0.." + (nodes.size() - 1) + "}." + logicTable);
                if (keyGenTables.contains(logicTable))
                    cfg.setKeyGeneratorColumnName("id");
                config.getTableRuleConfigs().add(cfg);
            }
            //数据源
            config.setDefaultDatabaseShardingStrategyConfig(new InlineShardingStrategyConfiguration(columnName, "node${" + columnName + " % " + nodes.size() + "}"));
            config.setDefaultDataSourceName("node");

            //设置ID生成器
            if (!keyGenTables.isEmpty()) {
                long workerId = 0L;
                try {
                    String hostName = InetAddress.getLocalHost().getHostName();
                    String serverId = hostName.substring(hostName.lastIndexOf("-") + 1);
                    workerId = Long.parseLong(serverId);
                } catch (Throwable e) {
                }
                DefaultKeyGenerator.setWorkerId(workerId);
                config.setDefaultKeyGeneratorClass(DefaultKeyGenerator.class.getName());
            }
        }

        return ShardingDataSourceFactory.createDataSource(dataSourceMap, config, new HashMap<>(), properties);
    }

    private Set<String> keyGeneratorTableSet() {
        Set<String> tables = new HashSet<>();
        if (keyGeneratorTables != null) {
            String[] names = keyGeneratorTables.split(",");
            for (String name : names)
                tables.add(name.trim());
        }
        return tables;
    }

    private HikariDataSource create(String jdbcUrl, String username, String password, int maximumPoolSize) {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(driverClassName);
        config.setJdbcUrl(jdbcUrl);
        config.setUsername(username);
        config.setPassword(password);
        config.setAutoCommit(false);
        config.setTransactionIsolation("TRANSACTION_READ_COMMITTED");
        config.setMaximumPoolSize(maximumPoolSize);
        return new HikariDataSource(config);
    }
}
