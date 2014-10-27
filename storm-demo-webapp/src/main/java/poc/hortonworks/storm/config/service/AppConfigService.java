package poc.hortonworks.storm.config.service;


import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HConnection;
import org.apache.hadoop.hbase.client.HConnectionManager;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

@Service
public class AppConfigService {

	private static final Logger LOG = Logger.getLogger(AppConfigService.class);
	private static final String APP_CONFIG_TABLE_NAME = "app_config";
	private static final String COLUMN_FAMILY_NAME = "config";
	
	/* Must be supplied as System Property */
	private String hbaseZookeeperHost;
	
	/* The defaults can be overidden by System properties */
	private String hbaseZookeeperClientPort = "2181";
	private String hbaseZookeeperZnodeParent = "/hbase-unsecure";
			

	private HTable appConfigTable;


	public AppConfigService() {
		this.hbaseZookeeperHost = System.getProperty("hbase.zookeeper.host");
		LOG.info("HBase zookeeper host is " + this.hbaseZookeeperHost);
		if(hbaseZookeeperHost == null) {
			String errMsg = "System Property[hbase.zookeeper.host] must be set";
			LOG.error(errMsg);
			throw new RuntimeException(errMsg);
		}
		
		if(System.getProperty("hbase.zookeeper.client.port") != null) {
			this.hbaseZookeeperClientPort = System.getProperty("hbase.zookeeper.client.port");
		}
		
		if(System.getProperty("hbase.zookeeper.znode.parent") != null) {
			this.hbaseZookeeperZnodeParent = System.getProperty("hbase.zookeeper.znode.parent");
		}
		
		try {
			Configuration config = constructConfiguration();
			HConnection connection = HConnectionManager.createConnection(config);
			this.appConfigTable = (HTable) connection.getTable(APP_CONFIG_TABLE_NAME);
			
		} catch (Exception  e) {
			LOG.error("Error connectiong to HBase", e);
			throw new RuntimeException("Error Connecting to HBase", e);
		} 	
	}
	
	private Configuration constructConfiguration() throws Exception {
		Configuration config = HBaseConfiguration.create();
		config.set("hbase.zookeeper.quorum",
				this.hbaseZookeeperHost);
		config.set("hbase.zookeeper.property.clientPort", this.hbaseZookeeperClientPort);
		config.set("zookeeper.znode.parent", this.hbaseZookeeperZnodeParent);
		return config;
	}	
	
	
	public String getActiveMQHost() {
		return getValueForKey("activemq.host");
	}
	
	public String getHBaseZookeeperHost() {
		return getValueForKey("hbase.zookeeper.host");
	}	
	
	public String getHBaseZookeeperClientPort() {
		return getValueForKey("hbase.zookeeper.client.port");
	}	

	public String getHBaseZookeeperZNodeParent() {
		return getValueForKey("hbase.zookeeper.znode.parent");
	}	
	
	public String getPhoenixConnectionURL() {
		return getValueForKey("phoenix.connection.url");
	}
	
	public String getKafkaBrokerList() {
		return getValueForKey("kafka.broker.list");
	}	
	

	public String getValueForKey(String keyName) throws RuntimeException {
		String value = null;
		byte[] key = Bytes.toBytes(keyName);
		Get get = new Get(key);
		try {
			Result result = this.appConfigTable.get(get);
			value= Bytes.toString(result.getValue(Bytes.toBytes(COLUMN_FAMILY_NAME), Bytes.toBytes("value")));
			if(value == null) {
				String errMsg = "No value for key["+keyName+"] configured";
				LOG.error(errMsg);
				throw new RuntimeException(errMsg);
			}
			LOG.info("Value for key["+keyName + "] is:  " + value);
		} catch (Exception  e) {
			LOG.error("Error retrieving activemq.host", e);
		}
		return value;
	}




	

}
