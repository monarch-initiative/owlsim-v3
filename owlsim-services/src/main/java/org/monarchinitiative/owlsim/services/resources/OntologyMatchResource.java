package org.monarchinitiative.owlsim.services.resources;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.monarchinitiative.owlsim.compute.classmatch.ClassMatcher;
import org.monarchinitiative.owlsim.compute.classmatch.SimpleClassMatch;
import org.monarchinitiative.owlsim.compute.cpt.IncoherentStateException;
import org.monarchinitiative.owlsim.kb.filter.UnknownFilterException;
import org.monarchinitiative.owlsim.model.match.MatchSet;
import org.prefixcommons.CurieUtil;

import com.codahale.metrics.annotation.Timed;

import io.dropwizard.jersey.caching.CacheControl;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

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
    public List<SimpleClassMatch> getOntoMatches(
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

    @GET
    @Path("/single/{entity}/{ontology}")
    @Timed
    @CacheControl(maxAge = 2, maxAgeUnit = TimeUnit.HOURS)
    @ApiOperation(value = "Match", response = MatchSet.class,
    notes = "Additional notes on the match resource.")
    public List<SimpleClassMatch> getEntityMatches(
            @ApiParam(value = "entity, e.g. MP:0001951",
            required = true) @PathParam("entity") String entity,
            @ApiParam(value = "ontology to be matched, e.g. HP",
            required = true) @PathParam("ontology") String ontology)
                    throws UnknownFilterException, IncoherentStateException {
        List<SimpleClassMatch> matches = 
                classMatcher.matchEntity(entity, ontology);
		return matches;
	}

}
