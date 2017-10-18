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

package de.ubleipzig.vocabulary;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toSet;

import java.lang.reflect.Field;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.jena.graph.Factory;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.RDFParser;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author acoburn
 */
public abstract class AbstractVocabularyTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractVocabularyTest.class);

    private static final String ACCEPT =
            "text/turtle, application/rdf+xml, application/json, application/ld+json";

    public abstract String namespace();


    public abstract Class vocabulary();

    public Boolean isStrict() {
        return true;
    }

    private static Graph getVocabulary(final String url) {
        final Graph graph = Factory.createDefaultGraph();
        RDFParser.source(url).httpAccept(ACCEPT).parse(graph);
        return graph;
    }

    @Test
    public void testVocabulary() {
        final Graph graph = getVocabulary(namespace());

        final Set<String> subjects =
                graph.find(Node.ANY, Node.ANY, Node.ANY).mapWith(Triple::getSubject)
                        .filterKeep(Node::isURI).mapWith(Node::getURI).filterKeep(Objects::nonNull)
                        .toSet();

        fields().forEach(field -> {
            if (isStrict()) {
                Assert.assertTrue("Field definition is not in published ontology! " + field,
                        subjects.contains(namespace() + field));
            } else if (!subjects.contains(namespace() + field)) {
                LOGGER.warn("Field definition is not in published ontology! {}", field);
            }
        });
    }

    @Test
    public void testVocabularyRev() {
        final Graph graph = getVocabulary(namespace());

        final Set<String> subjects = fields().map(namespace()::concat).collect(toSet());

        Assert.assertTrue("Unable to extract field definitions!", subjects.size() > 0);

        graph.find(Node.ANY, Node.ANY, Node.ANY).mapWith(Triple::getSubject).filterKeep(Node::isURI)
                .mapWith(Node::getURI).filterKeep(Objects::nonNull)
                .filterKeep(uri -> uri.startsWith(namespace())).filterDrop(namespace()::equals)
                .filterDrop(subjects::contains).forEachRemaining(uri -> {
            LOGGER.warn("{} not defined in {} class", uri, vocabulary().getName());
        });
    }

    @Test
    public void testNamespace() throws Exception {
        final Optional<Field> uri =
                stream(vocabulary().getFields()).filter(field -> field.getName().equals("URI"))
                        .findFirst();

        Assert.assertTrue(vocabulary().getName() + " does not contain a 'URI' field!",
                uri.isPresent());
        Assert.assertEquals("Namespaces do not match!", namespace(), uri.get().get(null));
    }

    private Stream<String> fields(Object type) {
        return stream(vocabulary().getFields()).filter(field -> field.getType().equals(type))
                .map(Field::getName)
                .map(name -> name.endsWith("_") ? name.substring(0, name.length() - 1) : name)
                .map(name -> name.replaceAll("_", "-")).filter(field -> !field.equals("URI"));

    }

    private Stream<String> fields() {
        return stream(vocabulary().getFields()).map(Field::getName)
                .map(name -> name.endsWith("_") ? name.substring(0, name.length() - 1) : name)
                .map(name -> name.replaceAll("_", "-")).filter(field -> !field.equals("URI"));
    }
}

