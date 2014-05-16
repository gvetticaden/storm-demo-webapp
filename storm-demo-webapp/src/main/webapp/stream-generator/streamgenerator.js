
function ApplicationModel(stompClient) {
  var self = this;

  self.username = ko.observable();
  self.streamGenerator = ko.observable(new StreamGeneratorModel(stompClient));

  self.logout = function() {
	    stompClient.disconnect();
	    window.location.href = "../logout.html";
  }  
}



function StreamGeneratorModel(stompClient) {
	  var self = this;

	  self.eventEmitterClass = ko.observable("com.hortonworks.streaming.impl.domain.transport.Truck");
	  self.eventCollectorClass = ko.observable("com.hortonworks.streaming.impl.collectors.KafkaEventCollector");
	  self.eventEmitters = ko.observable(20);
	  self.events = ko.observable(200);
	  
	  
	  self.eventEmitterClass2 = ko.observable("com.hortonworks.streaming.impl.domain.transport.Truck");
	  self.eventCollectorClass2 = ko.observable("com.hortonworks.streaming.impl.collectors.KafkaEventCollector");
	  self.events2 = ko.observable(200);
	  //self.routeDirectory = ko.observable("/mnt/workspaces/stream-simulator/routes");
	  self.routeDirectory = ko.observable("/Users/gvetticaden/Dropbox/Hortonworks/Development/Git/storm-demo-webapp/storm-demo-webapp/routes");
	  self.centerCoordinatesLat = ko.observable("40.743878");
	  self.centerCoordinatesLong = ko.observable("-73.571853");
	  self.zoomLevel=ko.observable("10");
	  self.truckSymbolSize = ko.observable("500");

	 

	  self.simulateStream = function() {

	    var streamGeneratorData = {
	        "eventEmitterClassName" : self.eventEmitterClass(),
	        "eventCollectorClassName": self.eventCollectorClass(),
	        "numberOfEventEmitters": self.eventEmitters(),
	        "numberOfEvents": self.events()
	      };
	    console.log(streamGeneratorData);
	    stompClient.send("/app/simulateStreaming", {}, JSON.stringify(streamGeneratorData));
	  }
	  
	  
	  self.mapRoutes = function() {

		    var streamGeneratorData = {
		        "eventEmitterClassName" : self.eventEmitterClass2(),
		        "eventCollectorClassName": self.eventCollectorClass2(),
		        "numberOfEvents": self.events2(),
		        "routeDirectory": self.routeDirectory(),
		    	"centerCoordinatesLat": self.centerCoordinatesLat(),
		    	"centerCoordinatesLong": self.centerCoordinatesLong(),
		    	"zoomLevel": self.zoomLevel(),
		    	"truckSymbolSize": self.truckSymbolSize()
		      };
		    console.log(streamGeneratorData);
		    stompClient.send("/app/mapTruckRoutes", {}, JSON.stringify(streamGeneratorData));
		  }	  
}