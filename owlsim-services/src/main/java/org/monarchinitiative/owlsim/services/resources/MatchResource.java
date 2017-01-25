package org.monarchinitiative.owlsim.services.resources;

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
import org.monarchinitiative.owlsim.compute.matcher.NegationAwareProfileMatcher;
import org.monarchinitiative.owlsim.compute.matcher.ProfileMatcher;
import org.monarchinitiative.owlsim.kb.filter.AnonIndividualFilter;
import org.monarchinitiative.owlsim.kb.filter.TypeFilter;
import org.monarchinitiative.owlsim.kb.filter.UnknownFilterException;
import org.monarchinitiative.owlsim.model.match.MatchSet;
import org.monarchinitiative.owlsim.model.match.ProfileQuery;
import org.monarchinitiative.owlsim.model.match.ProfileQueryFactory;
import org.monarchinitiative.owlsim.services.exceptions.NonNegatedMatcherException;
import org.monarchinitiative.owlsim.services.exceptions.UnknownMatcherException;

import com.codahale.metrics.annotation.Timed;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

import io.dropwizard.jersey.caching.CacheControl;

@Path("/match")
@Api(value = "/match", description = "match services")
@Produces({MediaType.APPLICATION_JSON})
public class MatchResource {

  // TODO: this needs to be updated for Dropwizard 1.0, use HK2
  @Inject
  Map<String, ProfileMatcher> matchers = new HashMap<>();

  @GET
  @Path("/matchers")
  @ApiOperation(value = "Get registered profile matchers", response = Collection.class,
      notes = "Additional notes on the matchers resource.")
  public Collection<String> getMatchers() {
    return matchers.keySet();
  }

  @GET
  @Path("/{matcher}")
  @Timed
  @CacheControl(maxAge = 2, maxAgeUnit = TimeUnit.HOURS)
  @ApiOperation(value = "Match", response = MatchSet.class,
      notes = "Additional notes on the match resource.")
  public MatchSet getMatches(
      @ApiParam(value = "The name of the matcher to use",
          required = true) @PathParam("matcher") String matcherName,
      @ApiParam(value = "Class IDs to be matched", required = false,
          allowMultiple = true) @QueryParam("id") Set<String> ids,
      @ApiParam(value = "Negated Class IDs",
          required = false) @QueryParam("negatedId") Set<String> negatedIds,
      @ApiParam(value = "Target Class IDs",
          required = false) @QueryParam("targetClassId") Set<String> targetClassIds,
      @ApiParam(value = "Filter individuals by type",
          required = false) @QueryParam("filterClassId") String filterId,
      @ApiParam(value = "cutoff limit", required = false) @QueryParam("limit") Integer limit)
      throws UnknownFilterException, IncoherentStateException {
    if (!matchers.containsKey(matcherName)) {
      throw new UnknownMatcherException(matcherName);
    }
    ProfileMatcher matcher = matchers.get(matcherName);

    // Verify that matcher is negation aware if negated IDs are used
    if (!negatedIds.isEmpty()
        && !NegationAwareProfileMatcher.class.isAssignableFrom(matcher.getClass())) {
      throw new NonNegatedMatcherException(matcherName);
    }
    ProfileQuery query = ProfileQueryFactory.createQueryWithNegation(ids, negatedIds);

    if (limit != null)
      query.setLimit(limit);
    if (filterId != null) {
      TypeFilter filter = new TypeFilter(filterId, false, false);
      query.setFilter(filter);
    }
    if (!targetClassIds.isEmpty()) {
      ProfileQuery targetPQ = ProfileQueryFactory.createQuery(targetClassIds);
      AnonIndividualFilter filter = new AnonIndividualFilter(targetPQ);
      query.setFilter(filter);
    }

    return matcher.findMatchProfile(query);
  }

  // TODO - API for comparing two entities

}
