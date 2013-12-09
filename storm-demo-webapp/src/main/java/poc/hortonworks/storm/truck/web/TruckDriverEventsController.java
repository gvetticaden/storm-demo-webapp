/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package poc.hortonworks.storm.truck.web;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.annotation.SubscribeMapping;
import org.springframework.stereotype.Controller;

import poc.hortonworks.domain.transport.TruckDriverViolationEvent;
import poc.hortonworks.storm.truck.service.DriverEventsService;


@Controller
public class TruckDriverEventsController {

	private static final Log logger = LogFactory.getLog(TruckDriverEventsController.class);

	private final DriverEventsService driverEventsService;
	


	@Autowired
	public TruckDriverEventsController(DriverEventsService driverEventsService) {
		this.driverEventsService = driverEventsService;
	}

	@SubscribeMapping("/driverEvents")
	public Collection<TruckDriverViolationEvent> getDriverEvents() throws Exception {
		return driverEventsService.getLatestEventsForAllDrivers();
	}


}
