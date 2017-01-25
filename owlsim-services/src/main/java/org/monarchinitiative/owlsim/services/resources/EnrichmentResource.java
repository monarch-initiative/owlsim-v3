/**
 * Copyright (C) 2014 The OwlSim authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.monarchinitiative.owlsim.services.resources;

import io.dropwizard.jersey.caching.CacheControl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.monarchinitiative.owlsim.compute.cpt.IncoherentStateException;
import org.monarchinitiative.owlsim.compute.enrich.EnrichmentEngine;
import org.monarchinitiative.owlsim.compute.enrich.EnrichmentQuery;
import org.monarchinitiative.owlsim.compute.enrich.EnrichmentResultSet;
import org.monarchinitiative.owlsim.compute.enrich.impl.EnrichmentQueryImpl;
import org.monarchinitiative.owlsim.kb.filter.TypeFilter;
import org.monarchinitiative.owlsim.kb.filter.UnknownFilterException;
import org.monarchinitiative.owlsim.model.match.ProfileQuery;
import org.monarchinitiative.owlsim.model.match.ProfileQueryFactory;
import org.monarchinitiative.owlsim.services.exceptions.UnknownMatcherException;

import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@Path("/enrichment")
@Api(value = "/enrichment", description = "enrichment services")
@Produces({ MediaType.APPLICATION_JSON })
public class EnrichmentResource {

    // TODO: this needs to be updated for Dropwizard 1.0, use HK2
	@Inject
	Map<String, EnrichmentEngine> engines = new HashMap<>();

	@GET
	@Path("/engines")
	@ApiOperation(value = "Get registered profile engines", response = Collection.class,
	notes= "Additional notes on the engines resource.")
	public Collection<String> getEngines() {
		return engines.keySet();
	}

	@GET
	@Path("/{engine}")
	@Timed
	@CacheControl(maxAge = 2, maxAgeUnit = TimeUnit.HOURS)
	@ApiOperation(value = "Result", response = EnrichmentResultSet.class, 
	notes= "Additional notes on the match resource.")
	public EnrichmentResultSet getResults(
			@ApiParam( value = "The name of the engine to use", required = true)
			@PathParam("engine") String engineName,
			@ApiParam( value = "Class ID to be matched", required = false)
			@QueryParam("classId") String classId,
            @ApiParam( value = "Individual IDs", required = false)
            @QueryParam("individualId") Set<String> individualIds,
            @ApiParam( value = "Filter individuals by type", required = false)
            @QueryParam("filterClassId") String filterId,
			@ApiParam( value = "cutoff limit", required = false)
			@QueryParam("limit") Integer limit
	        ) throws UnknownFilterException, IncoherentStateException {
		if (!engines.containsKey(engineName)) {
			throw new UnknownMatcherException(engineName);
		}
		EnrichmentEngine engine = engines.get(engineName);
		EnrichmentQuery query = EnrichmentQueryImpl.create(classId, individualIds);
		
		if (limit != null)
		    query.setLimit(limit);
		if (filterId != null) {
		    TypeFilter filter = new TypeFilter(filterId, false, false);
		    query.setFilter(filter);
		}
		return engine.calculateEnrichmentAgainstKb(query);
	}

}
