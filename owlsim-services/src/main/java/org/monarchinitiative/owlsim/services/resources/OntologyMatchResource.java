package org.monarchinitiative.owlsim.services.resources;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
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

import org.monarchinitiative.owlsim.compute.classmatch.ClassMatcher;
import org.monarchinitiative.owlsim.compute.classmatch.SimpleClassMatch;
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
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

import io.dropwizard.jersey.caching.CacheControl;

@Path("/ontomatch")
@Api(value = "/ontomatch", description = "ontology match services")
@Produces({MediaType.APPLICATION_JSON})
public class OntologyMatchResource {

    @Inject
    ClassMatcher classMatcher;

    @GET
    @Path("/{queryOntology}/{targetOntology}")
    @Timed
    @CacheControl(maxAge = 2, maxAgeUnit = TimeUnit.HOURS)
    @ApiOperation(value = "Match", response = MatchSet.class,
    notes = "Additional notes on the match resource.")
    public List<SimpleClassMatch> getMatches(
            @ApiParam(value = "base ontology, e.g. MP",
            required = true) @PathParam("queryOntology") String queryOntology,
            @ApiParam(value = "ontology to be matched, e.g. HP",
            required = true) @PathParam("targetOntology") String targetOntology)
                    throws UnknownFilterException, IncoherentStateException {
        List<SimpleClassMatch> matches = 
                classMatcher.matchOntologies(queryOntology, targetOntology);
        return matches;
    }

    // TODO - API for comparing two entities

}
