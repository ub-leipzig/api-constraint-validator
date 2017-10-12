/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.ubleipzig.validator;

import static de.ubleipzig.validator.IO.expandDocumentToN3;
import static de.ubleipzig.validator.IO.getGraph;
import static java.util.Arrays.asList;

import com.github.jsonldjava.core.JsonLdError;
import de.ubleipzig.vocabulary.JSONReader;
import de.ubleipzig.vocabulary.SC;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.rdf.api.Graph;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.jena.JenaRDF;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.junit.jupiter.api.BeforeAll;

/**
 * TestSuite.
 *
 * @author christopher-johnson
 */
abstract class IIIFTestSuite implements TestLifecycleLogger {

    static InputStream context = DomainProperties.context();

    static Graph graph;

    static Model model;

    final List<IRI> models = asList(SC.Manifest);

    static List<String> testResources = new ArrayList<>(getTestResourcesFromJson().values());

    static String testResource;

    private static String testCLIResource = System.getProperty("test.resource");

    static final JenaRDF rdf = new JenaRDF();

    @BeforeAll
    static void setUp() throws IOException, JsonLdError {
        LOG.info("test.resource property is {}", testCLIResource);
        testResource = testCLIResource;
        LOG.info("test resource is {}", testResource);
        if (testResource == null) {
            testResource = getTestResourcesFromJson().get(DomainProperties.testResourceKey());
        }
        try {
            URL uri = new URL(testResource);
            InputStream is = expandDocumentToN3(uri);
            graph = getGraph(is);
            org.apache.jena.graph.Graph jenaGraph = rdf.asJenaGraph(graph);
            model = ModelFactory.createModelForGraph(jenaGraph);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    private static Map<String, String> getTestResourcesFromJson() {
        final URL res = JSONReader.class.getResource(DomainProperties.testResources);
        final JSONReader svc = new JSONReader(res.getPath());
        return svc.getNamespaces();
    }
}

