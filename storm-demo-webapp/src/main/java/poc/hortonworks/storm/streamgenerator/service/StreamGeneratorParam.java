package poc.hortonworks.storm.streamgenerator.service;

import java.io.Serializable;

public class StreamGeneratorParam implements Serializable {
	

	private static final long serialVersionUID = 8764713202535596728L;

	private int numberOfEventEmitters;
	private int numberOfEvents;
	String eventEmitterClassName;
	String eventCollectorClassName;
	

	public int getNumberOfEventEmitters() {
		return numberOfEventEmitters;
	}
	public void setNumberOfEventEmitters(int numberOfEventEmitters) {
		this.numberOfEventEmitters = numberOfEventEmitters;
	}
	public int getNumberOfEvents() {
		return numberOfEvents;
	}
	public void setNumberOfEvents(int numberOfEvents) {
		this.numberOfEvents = numberOfEvents;
	}
	public String getEventEmitterClassName() {
		return eventEmitterClassName;
	}
	public void setEventEmitterClassName(String eventEmitterClassName) {
		this.eventEmitterClassName = eventEmitterClassName;
	}
	public String getEventCollectorClassName() {
		return eventCollectorClassName;
	}
	public void setEventCollectorClassName(String eventCollectorClassName) {
		this.eventCollectorClassName = eventCollectorClassName;
	}
	
	
	

}
