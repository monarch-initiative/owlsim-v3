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
import org.monarchinitiative.owlsim.compute.mica.MICAStore;
import org.monarchinitiative.owlsim.compute.mica.MostInformativeCommonAncestorCalculator;
import org.monarchinitiative.owlsim.compute.mica.MostInformativeCommonAncestorCalculator.ClassInformationContentPair;
import org.monarchinitiative.owlsim.kb.BMKnowledgeBase;
import org.monarchinitiative.owlsim.kb.filter.UnknownFilterException;
import org.monarchinitiative.owlsim.model.match.MatchSet;

import com.codahale.metrics.annotation.Timed;
import com.googlecode.javaewah.EWAHCompressedBitmap;
import com.wordnik.swagger.annotations.Api;
import com.wordnik.swagger.annotations.ApiOperation;
import com.wordnik.swagger.annotations.ApiParam;

@Path("/ancestor")
@Api(value = "/match", description = "match services")
@Produces({ MediaType.APPLICATION_JSON })
public class AncestorResource {
   
    @Inject
    MostInformativeCommonAncestorCalculator micaCalculator;
    
    @Inject
    BMKnowledgeBase knowledgeBase;

    @GET
    @Path("/ancestors")
    @Timed
    @CacheControl(maxAge = 2, maxAgeUnit = TimeUnit.HOURS)
    @ApiOperation(value = "Match", response = MatchSet.class, 
    notes= "Additional notes on the match resource.")
    public Set<String> getAncestors(
            @ApiParam( value = "The name of the matcher to use", required = true)
            @PathParam("matcher") String matcherName,
            @ApiParam( value = "Class IDs to be matched", required = false)
            @QueryParam("classIds") Set<String> classIds,
            @ApiParam( value = "cutoff limit", required = false)
            @QueryParam("limit") Integer limit
            ) throws UnknownFilterException, IncoherentStateException {
        
        EWAHCompressedBitmap superBM = knowledgeBase.getSuperClassesBM(classIds);
        return knowledgeBase.getClassIds(superBM);
    }

	@GET
	@Path("/mica")
	@Timed
	@CacheControl(maxAge = 2, maxAgeUnit = TimeUnit.HOURS)
	@ApiOperation(value = "ICPair", response = ClassInformationContentPair.class, 
	notes= "Additional notes on the match resource.")
	public ClassInformationContentPair getMatches(
            @ApiParam( value = "Class IDs to be matched", required = false)
            @QueryParam("classIds1") Set<String> classIds1,
            @ApiParam( value = "Class IDs to be matched", required = false)
            @QueryParam("classIds2") Set<String> classIds2,
			@ApiParam( value = "cutoff limit", required = false)
			@QueryParam("limit") Integer limit
	        ) throws UnknownFilterException, IncoherentStateException {
		
	    
	    ClassInformationContentPair mica = micaCalculator.getMostInformativeCommonAncestorWithIC(classIds1, classIds2);
		return mica;
	}

}
