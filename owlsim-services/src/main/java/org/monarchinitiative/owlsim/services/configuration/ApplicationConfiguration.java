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
package org.monarchinitiative.owlsim.services.configuration;

import io.dropwizard.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.validation.constraints.NotNull;

public class ApplicationConfiguration extends Configuration {

    @NotNull
    private Set<String> ontologyUris;

    @NotNull
    private Set<String> ontologyDataUris;

    @NotNull
    private Set<String> dataTsvs;
    
    private Map<String, String> curies = new HashMap<String, String>();

    public Set<String> getOntologyUris() {
        return ontologyUris;
    }

    public Set<String> getOntologyDataUris() {
        return ontologyDataUris;
    }

    public Set<String> getDataTsvs() {
        return dataTsvs;
    }

    public Map<String, String> getCuries() {
        return curies;
    }

}
