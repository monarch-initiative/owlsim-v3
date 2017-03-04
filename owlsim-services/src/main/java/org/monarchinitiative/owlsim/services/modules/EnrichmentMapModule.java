package org.monarchinitiative.owlsim.services.modules;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.monarchinitiative.owlsim.compute.enrich.EnrichmentEngine;
import org.monarchinitiative.owlsim.compute.enrich.impl.HypergeometricEnrichmentEngine;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;

public class EnrichmentMapModule extends AbstractModule {

	private Logger LOG = Logger.getLogger(EnrichmentMapModule.class);

	@Override
	protected void configure() {
	}

	/***
	 * <p>
	 * <em>Note:</em> The class must be injectable by Guice.
	 * 
	 * @param injector
	 * @return A mapping of ProfileMatchers
	 * @throws IOException
	 */
	@Provides
	Map<String, EnrichmentEngine> getEnrichmentEngines(Injector injector) throws IOException {

		Map<String, EnrichmentEngine> engineMap = new HashMap<>();
		EnrichmentEngine e = (EnrichmentEngine) injector.getInstance(HypergeometricEnrichmentEngine.class);
		engineMap.put(e.getShortName(), e);

		return engineMap;
	}

}
