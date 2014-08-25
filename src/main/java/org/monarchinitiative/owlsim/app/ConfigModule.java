package org.monarchinitiative.owlsim.app;


import org.monarchinitiative.owlsim.model.match.Query;
import org.monarchinitiative.owlsim.model.match.impl.QueryImpl;

import com.google.inject.AbstractModule;

/**
 * Guice module
 * 
 * TODO: EXPERIMENTAL. May be removed.
 * 
 * @author cjm
 *
 */
public class ConfigModule extends AbstractModule {

	@Override 
	protected void configure() {
		//bind(Query.class).to(QueryImpl.class);
	}
}
