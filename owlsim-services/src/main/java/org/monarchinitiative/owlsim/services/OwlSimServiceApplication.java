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
package org.monarchinitiative.owlsim.services;

import java.util.EnumSet;
import java.util.Set;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;

import org.apache.log4j.Logger;
import org.eclipse.jetty.servlets.CrossOriginFilter;
import org.monarchinitiative.owlsim.compute.enrich.EnrichmentMapModule;
import org.monarchinitiative.owlsim.compute.matcher.MatcherMapModule;
import org.monarchinitiative.owlsim.kb.KnowledgeBaseModule;
import org.monarchinitiative.owlsim.services.configuration.ApplicationConfiguration;
import org.semanticweb.owlapi.OWLAPIParsersModule;
import org.semanticweb.owlapi.OWLAPIServiceLoaderModule;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import com.google.inject.Guice;
import com.google.inject.Injector;

import io.dropwizard.Application;
import io.dropwizard.assets.AssetsBundle;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.swagger.jaxrs.config.BeanConfig;
import io.swagger.jaxrs.listing.ApiListingResource;
import uk.ac.manchester.cs.owl.owlapi.OWLAPIImplModule;
import uk.ac.manchester.cs.owl.owlapi.concurrent.Concurrency;

public class OwlSimServiceApplication extends Application<ApplicationConfiguration> {

    private Logger LOG = Logger.getLogger(OwlSimServiceApplication.class);

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
        environment.jersey().register(new ApiListingResource());
        environment.getObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);

        BeanConfig config = new BeanConfig();
        config.setTitle("owlsim - Web Services");
        config.setVersion("1.0.0");
        config.setResourcePackage("org.monarchinitiative.owlsim.services.resources");
        config.setScan(true);
        config.setBasePath(environment.getApplicationContext().getContextPath());
    }

    void configureCors(Environment environment) {
        final FilterRegistration.Dynamic cors = environment.servlets().addFilter("CORS", CrossOriginFilter.class);
  
        // Configure CORS parameters
        cors.setInitParameter("allowedOrigins", "*");
        cors.setInitParameter("allowedHeaders", "X-Requested-With,Content-Type,Accept,Origin");
        cors.setInitParameter("allowedMethods", "OPTIONS,GET,PUT,POST,DELETE,HEAD");
  
        // Add URL mapping
        cors.addMappingForUrlPatterns(EnumSet.allOf(DispatcherType.class), true, "/*");
    }
    
    /***
     * Configure Jackson parameters
     * @param environment
     */
    void configureJackson(Environment environment) {
      // Some classes from commons-math do not have members to serialized. Ignore those or Jackson
      // will throw an exception.
      environment.getObjectMapper().disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    @Override
    public void run(ApplicationConfiguration configuration, Environment environment) throws Exception {
        environment.getApplicationContext().setContextPath("/api");
        configureSwagger(environment);
        configureJackson(environment);
        configureCors(environment);
        
        Concurrency concurrency = Concurrency.CONCURRENT;
        LOG.info("Creating injector...");
        Injector i = Guice.createInjector(
                new OWLAPIImplModule(concurrency),
                new OWLAPIParsersModule(),
                new OWLAPIServiceLoaderModule(),
                new KnowledgeBaseModule(
                        configuration.getOntologyUris(),
                        configuration.getOntologyDataUris(),
                        configuration.getDataTsvs(),
                        configuration.getCuries()
                        ),
                        new EnrichmentMapModule(),
                        new MatcherMapModule());
        LOG.info("BINDINGS ="+i.getAllBindings());
        //Add resources
        Set<ClassInfo> resourceClasses = ClassPath.from(getClass().getClassLoader())
                .getTopLevelClasses("org.monarchinitiative.owlsim.services.resources");
        for (ClassInfo resourceClass: resourceClasses) {
            Class<?> c = resourceClass.load();
            environment.jersey().register(i.getInstance(c));
        }

    }

}
