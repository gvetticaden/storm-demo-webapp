
function ApplicationModel(stompClient, L) {
  var self = this;

  self.username = ko.observable();
  self.driverMontior = ko.observable(new DriverMonitorModel());
  self.notifications = ko.observableArray();
  self.leaf = L;
  self.truckSymbolSize;
  

  self.connect = function() {
    stompClient.connect('', '', function(frame) {

      console.log('Connected ' + frame);
      self.username(frame.headers['user-name']);

      // Loads all the dangerous events for all drivers on page load
      stompClient.subscribe("/app/driverEvents", function(message) {
    	var jsonResponse = JSON.parse(message.body);
    	var lat = jsonResponse.startLat;
    	var long = jsonResponse.startLong;
    	var zoomLevel = jsonResponse.zoomLevel;
    
    	self.truckSymbolSize=jsonResponse.truckSymbolSize;
    	
        self.driverMontior().loadDangerousEvents(jsonResponse.violationEvents);
        self.driverMontior().initializeMap(lat, long, zoomLevel);
      });
      
      //Update page with any new dangerous event that came in
      //stompClient.subscribe("/topic/driver_infraction_events", function(message) {
      stompClient.subscribe("/topic/driver_infraction_events", function(message) {
           self.driverMontior().processDangerousEvent(JSON.parse(message.body));
       });
      
      stompClient.subscribe("/topic/driver_events", function(message) {
    	  self.driverMontior().renderOnMap(JSON.parse(message.body), self.truckSymbolSize);
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
  
  var driverOnMapLookup = {};
  

  
  self.initializeMap = function(lat, long, zoomLevel) {
	  console.log("inside initialize mapp...");
	  map = L.map('map').setView([lat, long], zoomLevel);

	    // add an OpenStreetMap tile layer
	  L.tileLayer('http://{s}.tile.osm.org/{z}/{x}/{y}.png', {
	        attribution: '&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
	  }).addTo(map);	  
	  
  }
  
  
  self.loadDangerousEvents = function(positions) {
	  for ( var i = 0; i < positions.length; i++) {
    	
    	self.loadDangerousEvent(positions[i]);
    }
  };
  
  self.loadDangerousEvent = function (position) {
  	var row = new DriverRow(position);
	self.rows.push(row);
	rowLookup[row.driverId] = row;	  
  }
  
  self.processDangerousEvent = function(driverEvent) {
	 	if (rowLookup.hasOwnProperty(driverEvent.driverId)) {
	 		rowLookup[driverEvent.driverId].highlight();
	 		rowLookup[driverEvent.driverId].updateEvent(driverEvent);
	 		setTimeout(function() {
	 			
	 			rowLookup[driverEvent.driverId].unHighlight();
	 			
	 		}, 2000);
	 		
	 		
	    } else {
	    	self.loadDangerousEvent(driverEvent);
	    }
	  }; 
	  

  self.renderOnMap = function(driverEvent, truckSymbolSize) {
	  if (driverOnMapLookup.hasOwnProperty(driverEvent.driverId)) {
		  var driverOnMap = driverOnMapLookup[driverEvent.driverId].driverOnMap;
		  var previousDriverEvent = driverOnMapLookup[driverEvent.driverId].driverEvent;
		  
		  driverOnMap.setLatLng([driverEvent.latitude, driverEvent.longitude]);
		  
		  var driverMsg;
		  if(driverEvent.numberOfInfractions == previousDriverEvent.numberOfInfractions) {
			  driverMsg = self.constructMessage(driverEvent.driverId, driverEvent.numberOfInfractions, previousDriverEvent.infractionEvent, driverEvent.driverName, driverEvent.routeId, driverEvent.routeName);
		  
		  } else {
			  driverMsg = self.constructMessage(driverEvent.driverId, driverEvent.numberOfInfractions, driverEvent.infractionEvent, driverEvent.driverName, driverEvent.routeId, driverEvent.routeName);
		  }
		  
		  driverOnMapLookup[driverEvent.driverId].driverEvent = driverEvent;
		  
		  //driverOnMap.bindPopup(driverMsg);
		  if(driverEvent.infractionEvent != 'Normal') {
			  driverOnMap.closePopup();
			  driverOnMap.bindPopup(driverMsg);
			  var newRadius = driverOnMap.getRadius() * 1.1;
			  console.log("New Raidus: " + newRadius);
			  if(newRadius > 50000) {
				  newRadius = driverOnMap.getRadius();			  
			  } 
			  driverOnMap.setRadius(newRadius);
			  driverOnMap.openPopup();
		  } else {
			  if(driverOnMap._popup._isOpen) {
				  //driverOnMap.closePopup();
				  driverOnMap.bindPopup(driverMsg);
				  driverOnMap.openPopup();
			  }
			  
		  }
			 
	  } else {
		  self.renderNewDriverOnMap(driverEvent, truckSymbolSize);
	  }
  }
  
  self.renderNewDriverOnMap = function (driverEvent, truckSymbolSize) {
	    var randomColor = '#' + (Math.random() * 0xFFFFFF << 0).toString(16);
	  	var driverOnMap = L.circle([driverEvent.latitude, driverEvent.longitude], truckSymbolSize, {
	        color: randomColor,
	        fillColor: randomColor,
	        fillOpacity: 0.8
	    }).addTo(map);   
	  	
		var driverMsg = self.constructMessage(driverEvent.driverId, driverEvent.numberOfInfractions, driverEvent.infractionEvent, driverEvent.driverName, driverEvent.routeId, driverEvent.routeName);
	  	driverOnMap.bindPopup(driverMsg);
	  	var driverDetails = {driverEvent:driverEvent, driverOnMap:driverOnMap};
	  	driverOnMapLookup[driverEvent.driverId] = driverDetails;	  
	    
  
  }; 
  
  self.constructMessage = function(driverId, numberOfInfractions, lastViolation, driverName, routeId, routeName) {
	  var message= " <div> " +
	  	"<b>Driver Name: </b> " + driverName +
	  	"</br>" + 
	  	"<b>Route Name: </b> " + routeName +
	  	"</br>" +  
	    "<b>Violation Count: </b>" + numberOfInfractions +
	    "</br>" +
	    "<b>Last Violation: </b>" + lastViolation +
	    "</br>" +
	    "</div>";
	  return message;
	};

};


function DriverRow(data) {
  var self = this;

  self.truckDriverEventKey = data.truckDriverEventKey;
  self.driverId = data.driverId;
  self.driverName = data.driverName;
  
 
  
  self.timeStampString = ko.observable(data.timeStampString);
  self.longitude = ko.observable(data.longitude);
  self.latitude = ko.observable(data.latitude);
  self.infractionEvent = ko.observable(data.infractionEvent);
  self.numberOfInfractions = ko.observable(data.numberOfInfractions);
  self.truckId = ko.observable(data.truckId);
  self.routeId = ko.observable(data.routeId);
  self.routeName = ko.observable(data.routeName);
  self.rowClass=ko.observable("");
  
  self.updateEvent = function(driverEvent) {
	  	
	    self.timeStampString(driverEvent.timeStampString);
	    self.longitude(driverEvent.longitude);
	    self.latitude(driverEvent.latitude);
	    self.infractionEvent(driverEvent.infractionEvent);
	    self.numberOfInfractions(driverEvent.numberOfInfractions);
	    self.routeId(driverEvent.routeId);
	    self.truckId(driverEvent.truckId);

  };  
  
  self.highlight = function() {
	  self.rowClass("highlight");
  };
 
  self.unHighlight = function() {
	  self.rowClass("");
  };  

};



