package org.monarchinitiative.owlsim.app;


import com.google.inject.AbstractModule;

/**
 * Guice module
 * 
 * TODO: EXPERIMENTAL. May be removed.
 * 
 * @author cjm
 *
 */
@Deprecated
public class ConfigModule extends AbstractModule {

	@Override 
	protected void configure() {
		//bind(Query.class).to(QueryImpl.class);
	}
}
