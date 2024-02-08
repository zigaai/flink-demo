package com.zigaai;

import com.ververica.cdc.connectors.mysql.source.MySqlSource;
import com.ververica.cdc.connectors.mysql.table.StartupOptions;
import com.ververica.cdc.debezium.JsonDebeziumDeserializationSchema;
import com.zigaai.config.PropertiesConstant;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.sink.SinkFunction;

import java.util.Properties;

public class CdcTest {
    public static void main(String[] args) throws Exception {
        Properties flinkProperties = PropertiesConstant.getFlinkProperties();
        MySqlSource<String> mySqlSource = getMySqlSource();

        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        // 设置 3s 的 checkpoint 间隔
        env.enableCheckpointing(Long.parseLong(flinkProperties.getProperty("env.enableCheckpointing", "3000")));
        env
                .fromSource(mySqlSource, WatermarkStrategy.noWatermarks(), flinkProperties.getProperty("sourceName", "MySQL Source"))
                // 设置 source 节点的并行度为 4
                // .setParallelism(Integer.parseInt(flinkProperties.getProperty("source.parallelism", "1")))
                .addSink(new SinkFunction<String>() {
                    @Override
                    public void invoke(String value, Context context) {
                        System.out.println("sink fun: " + value);
                    }
                });
                // 设置 sink 节点并行度为 1
                // .setParallelism(Integer.parseInt(flinkProperties.getProperty("sink.parallelism", "1")));

        env.execute(flinkProperties.getProperty("jobName", "MySQL Sync"));
    }

    private static MySqlSource<String> getMySqlSource() {
        Properties configProperties = PropertiesConstant.getConfigProperties();
        Properties debeziumProperties = PropertiesConstant.getDebeziumProperties();
        return MySqlSource.<String>builder()
                .hostname(configProperties.getProperty("mysql.hostname", "192.168.100.100"))
                .port(Integer.parseInt(configProperties.getProperty("mysql.port", "3306")))
                .databaseList(configProperties.getProperty("mysql.database").split(",")) // 设置捕获的数据库， 如果需要同步整个数据库，请将 tableList 设置为 ".*".
                .tableList(configProperties.getProperty("mysql.tables").split(",")) // 设置捕获的表
                .username(configProperties.getProperty("mysql.username"))
                .password(configProperties.getProperty("mysql.password"))
                .serverTimeZone(configProperties.getProperty("mysql.serverTimeZone", "UTC"))
                .scanNewlyAddedTableEnabled(Boolean.getBoolean(configProperties.getProperty("mysql.scanNewlyAddedTableEnabled", "false")))
                .debeziumProperties(debeziumProperties)
                .deserializer(new JsonDebeziumDeserializationSchema()) // 将 SourceRecord 转换为 JSON 字符串
                .startupOptions(
                        StartupOptions.timestamp(
                                Long.parseLong(configProperties.getProperty("startup.options.timestamp", String.valueOf(System.currentTimeMillis())))
                        )
                )
                .build();
    }
}