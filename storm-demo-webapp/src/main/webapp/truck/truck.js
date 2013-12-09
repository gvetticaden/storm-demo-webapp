
function ApplicationModel(stompClient) {
  var self = this;

  self.username = ko.observable();
  self.driverMontior = ko.observable(new DriverMonitorModel());
  self.notifications = ko.observableArray();

  self.connect = function() {
    stompClient.connect('', '', function(frame) {

      console.log('Connected ' + frame);
      self.username(frame.headers['user-name']);

      // Loads all the dangerous events for all drivers on page load
      stompClient.subscribe("/app/driverEvents", function(message) {

        self.driverMontior().loadDangerousEvents(JSON.parse(message.body));
      });
      
      //Update page with any new dangerous event that came in
      stompClient.subscribe("/topic/driver_infraction_events", function(message) {

           self.driverMontior().processDangerousEvent(JSON.parse(message.body));
       });
      
      //Update page with any new alerts
      stompClient.subscribe("/topic/driver_alert_notifications", function(message) {
          self.pushNotification(JSON.parse(message.body).alertNotification);
        });
      
    }, function(error) {
      console.log("STOMP protocol error " + error);
    });
  }

  self.pushNotification = function(text) {
    self.notifications.push({notification: text});
    if (self.notifications().length > 5) {
      self.notifications.shift();
    }
  }

  self.logout = function() {
    stompClient.disconnect();
    window.location.href = "../logout.html";
  }
}

function DriverMonitorModel() {
  var self = this;

  self.rows = ko.observableArray();

  var rowLookup = {};

  self.loadDangerousEvents = function(positions) {
	  for ( var i = 0; i < positions.length; i++) {
    	
    	self.loadDangerousEvent(positions[i]);
    }
  };
  
  self.loadDangerousEvent = function (position) {
  	var row = new DriverRow(position);
	self.rows.push(row);
	rowLookup[row.truckDriverEventKey] = row;	  
  }
  
  self.processDangerousEvent = function(driverEvent) {
	 	if (rowLookup.hasOwnProperty(driverEvent.truckDriverEventKey)) {
	 		rowLookup[driverEvent.truckDriverEventKey].highlight();
	 		rowLookup[driverEvent.truckDriverEventKey].updateEvent(driverEvent);
	 		setTimeout(function() {
	 			
	 			rowLookup[driverEvent.truckDriverEventKey].unHighlight();
	 			
	 		}, 2000);
	 		
	 		
	    } else {
	    	self.loadDangerousEvent(driverEvent);
	    }
	  };  

};

function DriverRow(data) {
  var self = this;

  self.truckDriverEventKey = data.truckDriverEventKey;
  self.driverId = data.driverId;
  self.truckId = data.truckId;
  
  self.timeStampString = ko.observable(data.timeStampString);
  self.longitude = ko.observable(data.longitude);
  self.latitude = ko.observable(data.latitude);
  self.infractionEvent = ko.observable(data.infractionEvent);
  self.numberOfInfractions = ko.observable(data.numberOfInfractions);
  self.rowClass=ko.observable("");
  
  self.updateEvent = function(driverEvent) {
	  	
	    self.timeStampString(driverEvent.timeStampString);
	    self.longitude(driverEvent.longitude);
	    self.latitude(driverEvent.latitude);
	    self.infractionEvent(driverEvent.infractionEvent);
	    self.numberOfInfractions(driverEvent.numberOfInfractions);

  };  
  
  self.highlight = function() {
	  self.rowClass("highlight");
  };
 
  self.unHighlight = function() {
	  self.rowClass("");
  };  

};



