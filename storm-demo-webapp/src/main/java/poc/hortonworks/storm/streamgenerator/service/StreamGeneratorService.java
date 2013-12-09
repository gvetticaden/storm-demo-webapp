package poc.hortonworks.storm.streamgenerator.service;

import java.util.Random;

import org.springframework.stereotype.Service;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.actor.UntypedActorFactory;

import com.hortonworks.streaming.impl.messages.StartSimulation;
import com.hortonworks.streaming.impl.messages.StopSimulation;
import com.hortonworks.streaming.listeners.SimulatorListener;
import com.hortonworks.streaming.masters.SimulationMaster;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

@Service
public class StreamGeneratorService {
	
	public void generateTruckEventsStream(final StreamGeneratorParam param) {

		try {
			final Class eventEmitterClass = Class.forName(param.getEventEmitterClassName());
			final Class eventCollectorClass = Class.forName(param.getEventCollectorClassName());
			Config config= ConfigFactory.load();
			
			ActorSystem system = ActorSystem.create("EventSimulator", config, getClass().getClassLoader());
			final ActorRef listener = system.actorOf(
					Props.create(SimulatorListener.class), "listener");
			final ActorRef eventCollector = system.actorOf(
					Props.create(eventCollectorClass), "eventCollector");
			System.out.println(eventCollector.path());
			final long demoId = new Random().nextLong();
			final ActorRef master = system.actorOf(new Props(
					new UntypedActorFactory() {
						public UntypedActor create() {
							return new SimulationMaster(
									param.getNumberOfEventEmitters(),
									eventEmitterClass, listener, param.getNumberOfEvents(), demoId);
						}
					}), "master");
//			Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
//				public void run() {
//					System.out.println("inside shutdown");
//					master.tell(new StopSimulation(), master);
//				}
//			}));
			master.tell(new StartSimulation(), master);
		} catch (Exception e) {
			throw new RuntimeException("Error running truck stream generator", e);
		} 
	
	}

}


//20 -1 com.hortonworks.streaming.impl.domain.transport.Truck com.hortonworks.streaming.impl.collectors.KafkaEventCollector