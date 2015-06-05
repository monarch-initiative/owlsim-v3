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
package org.monarchinitiative.owlsim.services;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

import java.util.Set;

import org.monarchinitiative.owlsim.kb.KnowledgeBaseModule;
import org.monarchinitiative.owlsim.services.configuration.ApplicationConfiguration;

import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.wordnik.swagger.config.ConfigFactory;
import com.wordnik.swagger.config.ScannerFactory;
import com.wordnik.swagger.config.SwaggerConfig;
import com.wordnik.swagger.jaxrs.config.DefaultJaxrsScanner;
import com.wordnik.swagger.jaxrs.listing.ApiDeclarationProvider;
import com.wordnik.swagger.jaxrs.listing.ApiListingResourceJSON;
import com.wordnik.swagger.jaxrs.listing.ResourceListingProvider;
import com.wordnik.swagger.jaxrs.reader.DefaultJaxrsApiReader;
import com.wordnik.swagger.reader.ClassReaders;

public class OwlSimServiceApplication extends Application<ApplicationConfiguration> {

	public static void main(String[] args) throws Exception {
		new OwlSimServiceApplication().run(args);
	}

	@Override
	public String getName() {
		return "owlsim Web Services";
	}

	@Override
	public void initialize(Bootstrap<ApplicationConfiguration> bootstrap) {
		initializeSwaggger(bootstrap);
	}

	void initializeSwaggger(Bootstrap<ApplicationConfiguration> bootstrap) {
		bootstrap.addBundle(new AssetsBundle("/swagger/", "/docs", "index.html"));
	}

	/***
	 * The context path must be set before configuring swagger
	 * @param environment
	 */
	void configureSwagger(Environment environment) {
		environment.jersey().register(new ApiListingResourceJSON());
		environment.jersey().register(new ApiDeclarationProvider());
		environment.jersey().register(new ResourceListingProvider());
		ScannerFactory.setScanner(new DefaultJaxrsScanner());
		ClassReaders.setReader(new DefaultJaxrsApiReader());
		SwaggerConfig config = ConfigFactory.config();
		config.setApiVersion("1.0.1");
		config.setBasePath(".." + environment.getApplicationContext().getContextPath());
	}

	@Override
	public void run(ApplicationConfiguration configuration, Environment environment) throws Exception {
		environment.getApplicationContext().setContextPath("/api");
		configureSwagger(environment);

		Injector i = Guice.createInjector(
				new KnowledgeBaseModule(configuration.getOntologyUris(), configuration.getOntologyDataUris()),
				new MatcherMapModule());

		//Add resources
		Set<ClassInfo> resourceClasses = ClassPath.from(getClass().getClassLoader())
				.getTopLevelClasses("org.monarchinitiative.owlsim.services.resources");
		for (ClassInfo resourceClass: resourceClasses) {
			environment.jersey().register(i.getInstance(resourceClass.load()));
		}

	}

}
