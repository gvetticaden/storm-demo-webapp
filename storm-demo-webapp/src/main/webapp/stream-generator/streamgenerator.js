
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
}