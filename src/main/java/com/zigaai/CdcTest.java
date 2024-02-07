package com.zigaai;

import com.ververica.cdc.connectors.mysql.source.MySqlSource;
import com.ververica.cdc.connectors.mysql.table.StartupOptions;
import com.ververica.cdc.debezium.JsonDebeziumDeserializationSchema;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.streaming.api.datastream.DataStreamSource;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.Properties;

public class CdcTest {
	public static void main(String[] args) throws Exception {
		System.out.println("===================================== CDC 开始 =====================================");
		Properties properties = new Properties();
		properties.setProperty("decimal.handling.mode","string");

		MySqlSource<String> mySqlSource = MySqlSource.<String>builder()
				.hostname("192.168.100.100")
				.port(3306)
				.databaseList("foo") // 设置捕获的数据库， 如果需要同步整个数据库，请将 tableList 设置为 ".*".
				.tableList("foo.user") // 设置捕获的表
				.username("root")
				.password("741852963mysql")
				.serverTimeZone("UTC")
				.scanNewlyAddedTableEnabled(true)
				.debeziumProperties(properties)
				.deserializer(new JsonDebeziumDeserializationSchema()) // 将 SourceRecord 转换为 JSON 字符串
				.startupOptions(StartupOptions.timestamp(1707114200000L))
				.build();

		final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

		// 设置 3s 的 checkpoint 间隔
		env.enableCheckpointing(3000);
		System.out.println("===================================== CDC checkpoint =====================================");

		env
				.fromSource(mySqlSource, WatermarkStrategy.noWatermarks(), "MySQL Source")
				// 设置 source 节点的并行度为 4
				.setParallelism(1)
				.print("==>").setParallelism(1); // 设置 sink 节点并行度为 1

		DataStreamSource<String> mysqlDS =
				env.fromSource(
						mySqlSource,
						WatermarkStrategy.noWatermarks(),
						"MysqlSource");

		mysqlDS.print();
		System.out.println("===================================== CDC 打印 =====================================");

		env.execute("Print MySQL Snapshot + Binlog");
	}
}