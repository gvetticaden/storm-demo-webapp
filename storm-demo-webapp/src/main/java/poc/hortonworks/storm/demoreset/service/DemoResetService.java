package poc.hortonworks.storm.demoreset.service;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.TableNotFoundException;
import org.apache.hadoop.hbase.client.HBaseAdmin;
import org.apache.hadoop.hbase.client.HConnection;
import org.apache.hadoop.hbase.client.HConnectionManager;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import poc.hortonworks.storm.demoreset.web.DemoResetParam;

import com.hortonworks.streaming.impl.domain.transport.Truck;
import com.hortonworks.streaming.impl.domain.transport.TruckConfiguration;

@Service
public class DemoResetService {
	
	private static Logger LOG = Logger.getLogger(DemoResetService.class);
	
	private static final  String EVENTS_TABLE_NAME = "driver_dangerous_events";
	private static final  String EVENTS_COUNT_TABLE_NAME = "driver_dangerous_events_count";
	
	private HBaseAdmin admin;
	private HTable driverEventsTable;
	private HTable driverEventsCountTable;
	
	
	public DemoResetService() {
		try {
			Configuration config = constructConfiguration();
			admin = createHBaseAdmin(config);
			HConnection connection = HConnectionManager.createConnection(config);
			driverEventsTable = (HTable) connection.getTable(EVENTS_TABLE_NAME);
			driverEventsCountTable = (HTable) connection.getTable(EVENTS_COUNT_TABLE_NAME);

		} catch (Exception e) {
			LOG.error("Error connectiong to HBase", e);
			throw new RuntimeException("Error Connecting to HBase", e);
		}	
	}
	
	public void resetDemo(DemoResetParam param) {
		if(param.isTruncateHbaseTables()) {
			truncateHBaseTables();
		}
		resetStreamingSimulator();
	}
	
	public void resetStreamingSimulator() {
		TruckConfiguration.reset();
	}
	
	public void truncateHBaseTables() {
		try {
			
			truncateTable(driverEventsTable);
			truncateTable(driverEventsCountTable);
		} catch (Exception  e) {
			LOG.error("Error truncating HBase tables", e);
			//do nothing
		}			
	}
	
	private void truncateTable(HTable table)
			throws IOException, TableNotFoundException {	
		
		HTableDescriptor tableDescriptor = table.getTableDescriptor();
		TableName tableName = table.getName();
		admin.disableTable(tableName);
		admin.deleteTable(tableName);
		admin.createTable(tableDescriptor, table.getStartKeys());
	}
	
	private HBaseAdmin createHBaseAdmin(Configuration config) throws Exception {
		HBaseAdmin admin = new HBaseAdmin(config);
		return admin;
	}


	private Configuration constructConfiguration() throws Exception {
		Configuration config = HBaseConfiguration.create();
		config.set("hbase.zookeeper.quorum",
				"gvetticaden-central3.secloud.hortonworks.com");
		config.set("hbase.zookeeper.property.clientPort", "2181");
		config.set("zookeeper.znode.parent", "/hbase-unsecure");
		//HBaseAdmin.checkHBaseAvailable(config);
		return config;
	}	
}