/**
 * Copyright (C) 2014 The SciGraph authors
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

import static com.google.common.collect.Sets.newHashSet;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.monarchinitiative.owlsim.compute.matcher.ProfileMatcher;
import org.monarchinitiative.owlsim.model.match.MatchSet;
import org.monarchinitiative.owlsim.model.match.ProfileQuery;
import org.monarchinitiative.owlsim.model.match.ProfileQueryFactory;

import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;

@Path("/match")
@Api(value = "/match", description = "match services")
@Produces({ MediaType.APPLICATION_JSON })
public class MatchResource {

	@Inject
	Map<String, ProfileMatcher> matchers;

	@GET
	@Path("/matchers")
	@ApiOperation(value = "Get registered profile matchers", response = Collection.class)
	public Collection<String> getMatchers() {
		return matchers.keySet();
	}

	@GET
	@Path("/{matcher}")
	@ApiOperation(value = "Match", response = MatchSet.class)
	public MatchSet getMatches(
			@PathParam("matcher") String matcherName,
			@QueryParam("id") Set<String> ids) {
		if (!matchers.containsKey(matcherName)) {
			throw new WebApplicationException(Status.NOT_FOUND);
		}
		ProfileMatcher matcher = matchers.get(matcherName);
		ProfileQuery query = ProfileQueryFactory.createQuery(newHashSet("http://x.org/cephalopod"));
		return matcher.findMatchProfile(query);
	}


}
