package poc.hortonworks.storm.truck.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.HConnection;
import org.apache.hadoop.hbase.client.HConnectionManager;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import poc.hortonworks.domain.transport.TruckDriverViolationEvent;
import poc.hortonworks.storm.config.service.AppConfigService;

@Service
public class DriverEventsService {
	
	private static final String DRIVER_EVENTS_TABLE = "driver_dangerous_events";
	private static String DRIVER_EVENTS_COLUMN_FAMILY_NAME = "events";
	
	private static final String DRIVER_EVENTS_COUNT_TABLE = "driver_dangerous_events_count";
	private static String DRIVER_EVENTS_COUNT_COLUMN_FAMILY_NAME = "counters";	
	
	private static final Logger LOG = Logger.getLogger(DriverEventsService.class);
	
	
	private HTableInterface driverEventsTable;
	private HTableInterface driverEventsCountTable;
	private Connection phoenixConnection;
	
	private AppConfigService appConfigService;
	
	@Autowired
	public DriverEventsService(AppConfigService appConfigService) {
		try {
			this.appConfigService = appConfigService;
			Configuration config = constructConfiguration();
			HConnection connection = HConnectionManager.createConnection(config);
			driverEventsTable = connection.getTable(DRIVER_EVENTS_TABLE);
			driverEventsCountTable = connection.getTable(DRIVER_EVENTS_COUNT_TABLE);
			
			this.phoenixConnection = DriverManager.getConnection(appConfigService.getPhoenixConnectionURL());
			this.phoenixConnection.setAutoCommit(true);			
		} catch (Exception e) {
			LOG.error("Error connectiong to HBase", e);
			throw new RuntimeException("Error Connecting to HBase", e);
		} 
	}

	public Collection<TruckDriverViolationEvent> getLatestEventsForAllDrivers() {
		try {
			ResultScanner resultScanner = driverEventsTable.getScanner(Bytes.toBytes(DRIVER_EVENTS_COLUMN_FAMILY_NAME));
			Map<Integer, TruckDriverViolationEvent> eventsMap = new HashMap<Integer, TruckDriverViolationEvent> ();
			for(Result result = resultScanner.next(); result != null ; result = resultScanner.next()) {
				
				//String key = Bytes.toString(result.getRow());
				int driverId = Bytes.toInt(result.getValue(Bytes.toBytes(DRIVER_EVENTS_COLUMN_FAMILY_NAME), Bytes.toBytes("driverId")));
				if(eventsMap.get(driverId) == null) {
					int truckId = Bytes.toInt(result.getValue(Bytes.toBytes(DRIVER_EVENTS_COLUMN_FAMILY_NAME), Bytes.toBytes("truckId")));
					String truckDriverEventKey = driverId + "|" + truckId;
					
					long eventTimeLong = Bytes.toLong(result.getValue(Bytes.toBytes(DRIVER_EVENTS_COLUMN_FAMILY_NAME), Bytes.toBytes("eventTime")));
					SimpleDateFormat sdf = new SimpleDateFormat();
					String timeStampString = sdf.format(eventTimeLong);	
					
					double longitude = Bytes.toDouble(result.getValue(Bytes.toBytes(DRIVER_EVENTS_COLUMN_FAMILY_NAME), Bytes.toBytes("longitudeColumn")));
					double latitude = Bytes.toDouble(result.getValue(Bytes.toBytes(DRIVER_EVENTS_COLUMN_FAMILY_NAME), Bytes.toBytes("longitudeColumn")));
					
					String lastInfraction = Bytes.toString(result.getValue(Bytes.toBytes(DRIVER_EVENTS_COLUMN_FAMILY_NAME), Bytes.toBytes("eventType")));
					long numberOfInfractions = getInfractionCountForDriver(driverId);
					
					int routeId = Bytes.toInt(result.getValue(Bytes.toBytes(DRIVER_EVENTS_COLUMN_FAMILY_NAME), Bytes.toBytes("routeId")));
					String driverName = Bytes.toString(result.getValue(Bytes.toBytes(DRIVER_EVENTS_COLUMN_FAMILY_NAME), Bytes.toBytes("driverName")));
		
					String routeName = Bytes.toString(result.getValue(Bytes.toBytes(DRIVER_EVENTS_COLUMN_FAMILY_NAME), Bytes.toBytes("routeName")));
					
					TruckDriverViolationEvent event = new TruckDriverViolationEvent(truckDriverEventKey, driverId, truckId, eventTimeLong, 
										timeStampString, longitude, latitude, lastInfraction, numberOfInfractions, driverName, routeId, routeName );
					
					eventsMap.put(driverId, event);
				}
			}
			return eventsMap.values();
		} catch (Exception e) {
			LOG.error("Error getting driver events", e);
			throw new RuntimeException("Error getting driver events", e);
		}
	}
	
	public Collection<TruckDriverViolationEvent> getLatestEventsForAllDriversPhoenix() {
		try {
			
			
			PreparedStatement statement = phoenixConnection.prepareStatement("select * from  truck.dangerous_events order by driver_id asc, event_time desc");
			ResultSet resultSet = statement.executeQuery();
			Map<Integer, TruckDriverViolationEvent> eventsMap = new HashMap<Integer, TruckDriverViolationEvent> ();
				
			while(resultSet.next()) {
				int driverId = resultSet.getInt("driver_id");
				if(eventsMap.get(driverId) == null) {
					
					int truckId = resultSet.getInt("truck_id");
					String truckDriverEventKey = driverId + "|" + truckId;
					
					
					Timestamp eventTime = resultSet.getTimestamp("event_time");
					long eventTimeLong = eventTime.getTime();
					SimpleDateFormat sdf = new SimpleDateFormat();
					String timeStampString = sdf.format(eventTimeLong);						
					
					double latitude = resultSet.getDouble("latitude");
					double longitude = resultSet.getDouble("longitude");
					
					String lastInfraction = resultSet.getString("event_type");
					long numberOfInfractions = getInfractionCountForDriver(driverId);
					
					String driverName = resultSet.getString("driver_name");
					
					int routeId = resultSet.getInt("route_id");
					
					String routeName = resultSet.getString("route_name");	
					
					TruckDriverViolationEvent event = new TruckDriverViolationEvent(truckDriverEventKey, driverId, truckId, eventTimeLong, 
							timeStampString, longitude, latitude, lastInfraction, numberOfInfractions, driverName, routeId, routeName );
		
					eventsMap.put(driverId, event);					
					
				}
				
			}

			return eventsMap.values();
		} catch (Exception e) {
			LOG.error("Error getting driver events", e);
			throw new RuntimeException("Error getting driver events", e);
		}
	}	
	
	private long getInfractionCountForDriver(int driverId) {
		try {
			byte[] driverCount = Bytes.toBytes(driverId);
			Get get = new Get(driverCount);
			Result result = driverEventsCountTable.get(get);
			long count = Bytes.toLong(result.getValue(Bytes.toBytes(DRIVER_EVENTS_COUNT_COLUMN_FAMILY_NAME), Bytes.toBytes("incidentRunningTotal")));
			return count;
		} catch (Exception e) {
			LOG.error("Error getting infraction count", e);
			throw new RuntimeException("Error getting infraction count");
		}
	}

	private Configuration constructConfiguration() throws Exception {
		Configuration config = HBaseConfiguration.create();
		config.set("hbase.zookeeper.quorum",
				appConfigService.getHBaseZookeeperHost());
		config.set("hbase.zookeeper.property.clientPort", appConfigService.getHBaseZookeeperClientPort());
		config.set("zookeeper.znode.parent", appConfigService.getHBaseZookeeperZNodeParent());
		return config;
	}		

}
