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
import org.monarchinitiative.owlsim.compute.matcher.NegationAwareProfileMatcher;
import org.monarchinitiative.owlsim.compute.matcher.ProfileMatcher;
import org.monarchinitiative.owlsim.kb.filter.UnknownFilterException;
import org.monarchinitiative.owlsim.model.match.MatchSet;
import org.monarchinitiative.owlsim.model.match.ProfileQuery;
import org.monarchinitiative.owlsim.model.match.ProfileQueryFactory;
import org.monarchinitiative.owlsim.services.exceptions.NonNegatedMatcherException;
import org.monarchinitiative.owlsim.services.exceptions.UnknownMatcherException;

import com.codahale.metrics.annotation.Timed;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

@Path("/match")
@Api(value = "/match", description = "match services")
@Produces({ MediaType.APPLICATION_JSON })
public class MatchResource {

	@Inject
	Map<String, ProfileMatcher> matchers;

	@GET
	@Path("/matchers")
	@ApiOperation(value = "Get registered profile matchers", response = Collection.class,
	notes= "Additional notes on the matchers resource.")
	public Collection<String> getMatchers() {
		return matchers.keySet();
	}

	@GET
	@Path("/{matcher}")
	@Timed
	@CacheControl(maxAge = 2, maxAgeUnit = TimeUnit.HOURS)
	@ApiOperation(value = "Match", response = MatchSet.class, 
	notes= "Additional notes on the match resource.")
	public MatchSet getMatches(
			@ApiParam( value = "The name of the matcher to use", required = true)
			@PathParam("matcher") String matcherName,
			@ApiParam( value = "IDs that should match", required = false)
			@QueryParam("id") Set<String> ids,
			@ApiParam( value = "IDs that should not match", required = false)
			@QueryParam("negatedId") Set<String> negatedIds) throws UnknownFilterException, IncoherentStateException {
		if (!matchers.containsKey(matcherName)) {
			throw new UnknownMatcherException(matcherName);
		}
		ProfileMatcher matcher = matchers.get(matcherName);
		// Verify that matcher is negation aware if negated IDs are used
		if (!negatedIds.isEmpty() && !NegationAwareProfileMatcher.class.isAssignableFrom(matcher.getClass())) {
			throw new NonNegatedMatcherException(matcherName);
		}
		ProfileQuery query = ProfileQueryFactory.createQueryWithNegation(ids, negatedIds);
		return matcher.findMatchProfile(query);
	}

}
