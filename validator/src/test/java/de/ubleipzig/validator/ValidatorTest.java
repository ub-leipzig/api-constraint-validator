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

import static de.ubleipzig.validator.ApacheClient.headApacheClientResponse;
import static de.ubleipzig.validator.IO.closeableFindAny;
import static de.ubleipzig.validator.IO.expandDocumentToN3fromFile;
import static de.ubleipzig.validator.IO.getGraph;
import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.slf4j.LoggerFactory.getLogger;

import com.github.jsonldjava.core.JsonLdError;
import de.ubleipzig.vocabulary.DCElements;
import de.ubleipzig.vocabulary.DCTypes;
import de.ubleipzig.vocabulary.EXIF;
import de.ubleipzig.vocabulary.SC;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.rdf.api.BlankNode;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.Graph;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDFTerm;
import org.apache.commons.rdf.api.Triple;
import org.apache.commons.rdf.jena.JenaRDF;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.vocabulary.FOAF;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.trellisldp.api.ConstraintService;
import org.trellisldp.api.ConstraintViolation;
import org.trellisldp.vocabulary.OA;
import org.trellisldp.vocabulary.RDF;
import org.trellisldp.vocabulary.RDFS;
import org.trellisldp.vocabulary.Trellis;
import org.trellisldp.vocabulary.XSD;

/**
 * Validator Test.
 *
 * @author christopher-johnson
 */
public class ValidatorTest {

    private static final JenaRDF rdf = new JenaRDF();
    private static Logger LOG = getLogger(ValidatorTest.class.getName());
    private static final String TEST_DIR = "/iiif/p2/testcases";
    private static String testResource;
    private static Graph g;
    private static Model m;
    private final List<IRI> models = asList(SC.Manifest);
    private final ConstraintService svc = new ManifestConstraints();

    private InputStream getTestResourceFromFile(String path) throws IOException {
        return getClass().getResourceAsStream(path);
    }

    @Test
    @Tag("self")
    @DisplayName("0001 ManifestMustHaveType [3.3]")
    void test0001() throws IOException, JsonLdError {
        testResource = "0001-in.jsonld";
        g = getGraph(
                expandDocumentToN3fromFile(getTestResourceFromFile(TEST_DIR + "/" + testResource)));
        assertTrue(closeableFindAny(g.stream(null, RDF.type, SC.Manifest)).isPresent());
    }

    @Test
    @Tag("must")
    @DisplayName("0002 ManifestIdMustEqualTestResource [3.3]")
    void test0002() throws IOException, JsonLdError {
        testResource = "0002-in.jsonld";
        g = getGraph(
                expandDocumentToN3fromFile(getTestResourceFromFile(TEST_DIR + "/" + testResource)));
        BlankNodeOrIRI id =
                closeableFindAny(g.stream(null, RDF.type, SC.Manifest)).map(Triple::getSubject)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        SC.Manifest.getIRIString() + " type not found"));
        assertNotEquals(testResource, id.toString());
    }

    @Test
    @Tag("must")
    @DisplayName("0003 ManifestIdMustBeIRI [3.3]")
    void test0003() throws IOException, JsonLdError {
        testResource = "0003-in.jsonld";
        g = getGraph(
                expandDocumentToN3fromFile(getTestResourceFromFile(TEST_DIR + "/" + testResource)));
        BlankNodeOrIRI id =
                closeableFindAny(g.stream(null, RDF.type, SC.Manifest)).map(Triple::getSubject)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        SC.Manifest.getIRIString() + " type not found"));
        assertFalse(id instanceof BlankNode);
        assertTrue(id instanceof IRI);
        LOG.info("Test Resource " + testResource);
        LOG.info("Manifest IRI " + id.toString());
    }

    @Test
    @Tag("must")
    @DisplayName("0004 ManifestMustHaveLabel [3.1]")
    void test0004() throws IOException, JsonLdError {
        testResource = "0004-in.jsonld";
        g = getGraph(
                expandDocumentToN3fromFile(getTestResourceFromFile(TEST_DIR + "/" + testResource)));
        assertTrue(closeableFindAny(g.stream(null, RDFS.label, null)).isPresent());
    }

    @Test
    @Tag("should")
    @DisplayName("0005 ManifestShouldHaveThumbnail [3.1]")
    void test0005() throws IOException, JsonLdError {
        testResource = "0005-in.jsonld";
        g = getGraph(
                expandDocumentToN3fromFile(getTestResourceFromFile(TEST_DIR + "/" + testResource)));
        assertTrue(closeableFindAny(g.stream(null, rdf.createIRI(FOAF.thumbnail.toString()), null))
                .isPresent());
    }

    @Test
    @Tag("should")
    @DisplayName("0006 ManifestShouldHaveDescription [3.1]")
    void test0006() throws IOException, JsonLdError {
        testResource = "0006-in.jsonld";
        g = getGraph(
                expandDocumentToN3fromFile(getTestResourceFromFile(TEST_DIR + "/" + testResource)));
        assertTrue(closeableFindAny(g.stream(null, DCElements.description, null)).isPresent());
    }

    @Test
    @Tag("should")
    @DisplayName("0007 ManifestShouldHaveMetadata [3.1]")
    void test0007() throws IOException, JsonLdError {
        testResource = "0007-in.jsonld";
        g = getGraph(
                expandDocumentToN3fromFile(getTestResourceFromFile(TEST_DIR + "/" + testResource)));
        assertTrue(closeableFindAny(g.stream(null, SC.metadataLabels, null)).isPresent());
    }

    @Test
    @Tag("may")
    @DisplayName("0008 ManifestMayHaveStructures [5.1]")
    void test0008() throws IOException, JsonLdError {
        testResource = "0008-in.jsonld";
        g = getGraph(
                expandDocumentToN3fromFile(getTestResourceFromFile(TEST_DIR + "/" + testResource)));
        assertTrue(closeableFindAny(g.stream(null, SC.hasRanges, null)).isPresent());
    }

    @Test
    @Tag("must")
    @DisplayName("0009 RangeIdMustBeIRI [3.3]")
    void test0009() throws IOException, JsonLdError {
        testResource = "0009-in.jsonld";
        g = getGraph(
                expandDocumentToN3fromFile(getTestResourceFromFile(TEST_DIR + "/" + testResource)));
        RDFTerm id = closeableFindAny(g.stream(null, RDF.type, SC.Range)).map(Triple::getSubject)
                .orElseThrow(() ->
                        new RuntimeException(SC.Range.getIRIString() + " type not found"));
        assertTrue(id instanceof IRI);
    }

    @Test
    @Tag("must")
    @DisplayName("0010 hasSequenceMustBePresent [5.1]")
    void test0010() throws IOException, JsonLdError {
        testResource = "0010-in.jsonld";
        g = getGraph(
                expandDocumentToN3fromFile(getTestResourceFromFile(TEST_DIR + "/" + testResource)));
        assertTrue(closeableFindAny(g.stream(null, SC.hasSequences, null)).isPresent());
    }

    @Test
    @Tag("must")
    @DisplayName("0011 SequenceMustHaveType [3.3]")
    void test0011() throws IOException, JsonLdError {
        testResource = "0011-in.jsonld";
        g = getGraph(
                expandDocumentToN3fromFile(getTestResourceFromFile(TEST_DIR + "/" + testResource)));
        assertTrue(closeableFindAny(g.stream(null, RDF.type, SC.Sequence)).isPresent());
    }

    @Test
    @Tag("must")
    @DisplayName("0012 SequenceIdMustBeIRI [3.3]")
    void test0012() throws IOException, JsonLdError {
        testResource = "0012-in.jsonld";
        g = getGraph(
                expandDocumentToN3fromFile(getTestResourceFromFile(TEST_DIR + "/" + testResource)));
        BlankNodeOrIRI id =
                closeableFindAny(g.stream(null, RDF.type, SC.Sequence)).map(Triple::getSubject)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        SC.Sequence.getIRIString() + " type not found"));
        assertTrue(id instanceof IRI);
    }

    @Test
    @Tag("may")
    @DisplayName("0013 SequenceMayHaveStartCanvas [3.4]")
    void test0013() throws IOException, JsonLdError {
        testResource = "0013-in.jsonld";
        g = getGraph(
                expandDocumentToN3fromFile(getTestResourceFromFile(TEST_DIR + "/" + testResource)));
        RDFTerm id = closeableFindAny(g.stream(null, SC.hasStartCanvas, null)).map
                (Triple::getObject).orElseThrow(() ->
                new RuntimeException(SC.hasStartCanvas.getIRIString() + " type not found"));
        assertNotNull(id);
    }

    @Test
    @Tag("must")
    @DisplayName("0014 MultipleSequencesMustHaveLabel [3.1]")
    void test0014() throws IOException, JsonLdError {
        testResource = "0014-in.jsonld";
        g = getGraph(
                expandDocumentToN3fromFile(getTestResourceFromFile(TEST_DIR + "/" + testResource)));
        org.apache.jena.graph.Graph jenaGraph = rdf.asJenaGraph(g);
        m = ModelFactory.createModelForGraph(jenaGraph);
        long seqCount;
        try (Stream<? extends Triple> stream = g.stream(null, RDF.type, SC.Sequence)) {
            seqCount = stream.count();
        }
        assertTrue(seqCount >= 1);

        if (seqCount > 1) {
            String q = "SELECT (COUNT(?label) AS ?labelcount)  \n"
                    + "WHERE { ?manifest " + SC.hasSequences + " ?seqid .\n"
                    + "?seqid " + RDF.first + " ?sequence . \n"
                    + "?sequence " + RDFS.label + " ?label}";
            Query query = QueryFactory.create(q);
            try (QueryExecution qexec = QueryExecutionFactory.create(query, m)) {
                ResultSet results = qexec.execSelect();
                if (results.hasNext()) {
                    while (results.hasNext()) {
                        QuerySolution qs = results.next();
                        Literal lc = qs.getLiteral("labelcount").asLiteral();
                        assertEquals(1, lc.getInt());
                    }
                } else {
                    fail("not found");
                }
            }
        }
    }

    @Test
    @Tag("must")
    @DisplayName("0015 SequenceMustHaveCanvas [5.2]")
    void test0015() throws IOException, JsonLdError {
        testResource = "0015-in.jsonld";
        g = getGraph(
                expandDocumentToN3fromFile(getTestResourceFromFile(TEST_DIR + "/" + testResource)));
        org.apache.jena.graph.Graph jenaGraph = rdf.asJenaGraph(g);
        m = ModelFactory.createModelForGraph(jenaGraph);
        long seqCount;
        try (Stream<? extends Triple> stream = g.stream(null, RDF.type, SC.Sequence)) {
            seqCount = stream.count();
        }
        assumeTrue(seqCount >= 1);
        String q = "SELECT (COUNT(?canvas) AS ?canvascount)  \n"
                + "WHERE { ?manifest " + SC.hasSequences + " ?seqid .\n"
                + "?seqid " + RDF.first + " ?sequence . \n"
                + "?sequence " + SC.hasCanvases + " ?canvaslist .\n"
                + "?canvaslist " + RDF.rest + "* ?mid . \n"
                + "?mid " + RDF.first + " ?canvas .\n"
                + "?mid " + RDF.rest + " ?last} \n";
        Query query = QueryFactory.create(q);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, m)) {
            ResultSet results = qexec.execSelect();
            if (results.hasNext()) {
                while (results.hasNext()) {
                    QuerySolution qs = results.next();
                    Literal cc = qs.getLiteral("canvascount").asLiteral();
                    assertTrue(cc.getInt() >= 1);
                }
            } else {
                fail("not found");
            }
        }
    }

    @Test
    @Tag("must")
    @DisplayName("0016 CanvasIdMustBeIRI [3.3]")
    void test0016() throws IOException, JsonLdError {
        testResource = "0016-in.jsonld";
        g = getGraph(
                expandDocumentToN3fromFile(getTestResourceFromFile(TEST_DIR + "/" + testResource)));
        org.apache.jena.graph.Graph jenaGraph = rdf.asJenaGraph(g);
        m = ModelFactory.createModelForGraph(jenaGraph);
        BlankNodeOrIRI id =
                closeableFindAny(g.stream(null, RDF.type, SC.Canvas)).map(Triple::getSubject)
                        .orElseThrow(() ->
                                new RuntimeException(SC.Canvas.getIRIString() + " type not found"));
        assertTrue(id instanceof IRI);
    }

    @Test
    @Tag("api")
    @DisplayName("0017 CanvasShouldBeDereferenceable [3.3]")
    void test0017() throws IOException, JsonLdError {
        testResource = "0017-in.jsonld";
        g = getGraph(
                expandDocumentToN3fromFile(getTestResourceFromFile(TEST_DIR + "/" + testResource)));
        BlankNodeOrIRI c = closeableFindAny(g.stream(null, RDF.type, SC.Canvas)).map
                (Triple::getSubject).orElseThrow(
                () ->
                        new RuntimeException(SC.Canvas.getIRIString() + " type not found"));
        URL url = new URL(rdf.asJenaNode(c).getURI());
        String accept = "";
        org.apache.http.HttpResponse response;
        try {
            response = headApacheClientResponse(url.toString(), accept);
            String ct = null;
            if (response != null) {
                assertEquals(200, response.getStatusLine().getStatusCode());
            }
            LOG.info(ct);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    @Tag("must")
    @DisplayName("0018 CanvasesMustHaveType [3.3]")
    void test0018() throws IOException, JsonLdError {
        testResource = "0018-in.jsonld";
        g = getGraph(
                expandDocumentToN3fromFile(getTestResourceFromFile(TEST_DIR + "/" + testResource)));
        final List<RDFTerm> canvases = new ArrayList<>();
        final List<RDFTerm> canvastypes = new ArrayList<>();
        for (final Triple t : g.iterate(null, SC.hasImageAnnotations, null)) {
            canvases.add(t.getSubject());
        }
        for (final Triple t : g.iterate(null, RDF.type, SC.Canvas)) {
            canvastypes.add(t.getSubject());
        }
        assertEquals(canvastypes.size(), canvases.size());
        LOG.info("Canvas Count " + canvases.size());
    }

    @Test
    @Tag("must")
    @DisplayName("0019 CanvasesMustHaveLabel [3.1]")
    void test0019() throws IOException, JsonLdError {
        testResource = "0019-in.jsonld";
        g = getGraph(
                expandDocumentToN3fromFile(getTestResourceFromFile(TEST_DIR + "/" + testResource)));
        org.apache.jena.graph.Graph jenaGraph = rdf.asJenaGraph(g);
        m = ModelFactory.createModelForGraph(jenaGraph);
        String q = "SELECT (COUNT(?canvas) as ?canvascount) (COUNT(?canvaslabel) as ?labelcount) \n"
                + "WHERE {?sequence " + SC.hasCanvases + " ?canvaslist .\n"
                + "?canvaslist " + RDF.rest + "* ?mid . \n"
                + "?mid " + RDF.first + " ?canvas .\n"
                + "?mid " + RDF.rest + " ?last . \n"
                + "?canvas " + SC.hasImageAnnotations + " ?imageid .\n"
                + "?canvas " + RDFS.label + " ?canvaslabel}";
        Query query = QueryFactory.create(q);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, m)) {
            ResultSet results = qexec.execSelect();
            if (results.hasNext()) {
                while (results.hasNext()) {
                    QuerySolution qs = results.next();
                    Literal cc = qs.getLiteral("canvascount").asLiteral();
                    Literal lc = qs.getLiteral("labelcount").asLiteral();
                    assertEquals(cc, lc);
                }
            } else {
                fail("not found");
            }
        }
    }

    @Test
    @Tag("must")
    @DisplayName("0020 CanvasesMustHaveIntegerBounds [3.3]")
    void test0020() throws IOException, JsonLdError {
        testResource = "0020-in.jsonld";
        g = getGraph(
                expandDocumentToN3fromFile(getTestResourceFromFile(TEST_DIR + "/" + testResource)));
        org.apache.jena.graph.Graph jenaGraph = rdf.asJenaGraph(g);
        m = ModelFactory.createModelForGraph(jenaGraph);
        String q = "SELECT (COUNT(?canvas) as ?canvascount) ?height (COUNT(?height) as "
                + "?heightcount) \n"
                + "?width (COUNT  (?width) as ?widthcount)  \n"
                + "WHERE {?sequence " + SC.hasCanvases + " ?canvaslist .\n"
                + "?canvaslist " + RDF.rest + "* ?mid . \n"
                + "?mid " + RDF.first + " ?canvas .\n"
                + "?mid " + RDF.rest + " ?last . \n"
                + "?canvas " + SC.hasImageAnnotations + " ?imageid .\n"
                + "?canvas " + EXIF.height + " ?height . \n"
                + "?canvas " + EXIF.width + " ?width} GROUP BY ?height ?width";
        Query query = QueryFactory.create(q);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, m)) {
            ResultSet results = qexec.execSelect();
            if (results.hasNext()) {
                while (results.hasNext()) {
                    QuerySolution qs = results.next();
                    Literal cc = qs.getLiteral("canvascount").asLiteral();
                    Literal hc = qs.getLiteral("heightcount").asLiteral();
                    Literal wc = qs.getLiteral("widthcount").asLiteral();
                    Literal w = qs.getLiteral("width").asLiteral();
                    Literal h = qs.getLiteral("height").asLiteral();
                    assertEquals(cc, hc);
                    assertEquals(cc, wc);
                    assertEquals(XSD.integer.getIRIString(), w.getDatatypeURI());
                    assertEquals(XSD.integer.getIRIString(), h.getDatatypeURI());
                }
            } else {
                fail("not found");
            }
        }
    }

    @Test
    @Tag("must")
    @DisplayName("0021 AnnotationsMustHaveType [3.3]")
    void test0021() throws IOException, JsonLdError {
        testResource = "0021-in.jsonld";
        g = getGraph(
                expandDocumentToN3fromFile(getTestResourceFromFile(TEST_DIR + "/" + testResource)));
        final List<RDFTerm> annotations = new ArrayList<>();
        final List<RDFTerm> annotypes = new ArrayList<>();
        for (final Triple t : g.iterate(null, SC.hasImageAnnotations, null)) {
            annotations.add(t.getObject());
        }
        for (final Triple t : g.iterate(null, RDF.type, OA.Annotation)) {
            annotypes.add(t.getSubject());
        }
        assertEquals(annotations.size(), annotypes.size());
        LOG.info("Annotations Count " + annotations.size());
    }

    @Test
    @Tag("must")
    @DisplayName("0022 AnnotationsMustBeMotivated [5.4]")
    void test0022() throws IOException, JsonLdError {
        testResource = "0022-in.jsonld";
        g = getGraph(
                expandDocumentToN3fromFile(getTestResourceFromFile(TEST_DIR + "/" + testResource)));
        final List<RDFTerm> annotations = new ArrayList<>();
        final List<RDFTerm> annotypes = new ArrayList<>();
        for (final Triple t : g.iterate(null, SC.hasImageAnnotations, null)) {
            annotations.add(t.getObject());
        }
        for (final Triple t : g.iterate(null, OA.motivatedBy, SC.painting)) {
            annotypes.add(t.getSubject());
        }
        assertEquals(annotations.size(), annotypes.size());
        LOG.info("Annotations Count " + annotations.size());
    }

    @Test
    @Tag("must")
    @DisplayName("0023 AnnotationsMustHaveTarget [5.4]")
    void test0023() throws IOException, JsonLdError {
        testResource = "0023-in.jsonld";
        g = getGraph(
                expandDocumentToN3fromFile(getTestResourceFromFile(TEST_DIR + "/" + testResource)));
        final List<RDFTerm> annotations = new ArrayList<>();
        final List<RDFTerm> targets = new ArrayList<>();
        for (final Triple t : g.iterate(null, SC.hasImageAnnotations, null)) {
            annotations.add(t.getObject());
        }
        for (final Triple t : g.iterate(null, OA.hasTarget, null)) {
            targets.add(t.getObject());
        }
        assertEquals(annotations.size(), targets.size());
        LOG.info("Annotations Count " + annotations.size());
    }

    @Test
    @Tag("must")
    @DisplayName("0024 AnnotationsMustHaveBody [5.4]")
    void test0024() throws IOException, JsonLdError {
        testResource = "0024-in.jsonld";
        g = getGraph(
                expandDocumentToN3fromFile(getTestResourceFromFile(TEST_DIR + "/" + testResource)));
        final List<RDFTerm> annotations = new ArrayList<>();
        final List<RDFTerm> bodies = new ArrayList<>();
        for (final Triple t : g.iterate(null, SC.hasImageAnnotations, null)) {
            annotations.add(t.getObject());
        }
        for (final Triple t : g.iterate(null, OA.hasBody, null)) {
            bodies.add(t.getObject());
        }
        assertEquals(annotations.size(), bodies.size());
        LOG.info("Annotations Count " + annotations.size());
    }

    @Test
    @Tag("must")
    @DisplayName("0025 BodiesMustHaveType [5.4]")
    void test0025() throws IOException, JsonLdError {
        testResource = "0025-in.jsonld";
        g = getGraph(
                expandDocumentToN3fromFile(getTestResourceFromFile(TEST_DIR + "/" + testResource)));
        org.apache.jena.graph.Graph jenaGraph = rdf.asJenaGraph(g);
        m = ModelFactory.createModelForGraph(jenaGraph);
        String q = "SELECT ?type  \n"
                + "WHERE {?canvas " + SC.hasImageAnnotations + " ?imageid .\n"
                + "?imageid " + RDF.first + " ?resid . \n"
                + "?resid " + OA.hasBody + " ?body .\n"
                + "?body " + RDF.type + " ?type}";
        Query query = QueryFactory.create(q);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, m)) {
            ResultSet results = qexec.execSelect();
            if (results.hasNext()) {
                while (results.hasNext()) {
                    QuerySolution qs = results.next();
                    Resource type = qs.getResource("type").asResource();
                    assertNotNull(type.getURI());
                    LOG.info(type.getURI());
                }
            } else {
                fail("not found");
            }
        }
    }

    @Test
    @Tag("should")
    @DisplayName("0026 BodiesShouldHaveDCType [5.4]")
    void test0026() throws IOException, JsonLdError {
        testResource = "0026-in.jsonld";
        g = getGraph(
                expandDocumentToN3fromFile(getTestResourceFromFile(TEST_DIR + "/" + testResource)));
        org.apache.jena.graph.Graph jenaGraph = rdf.asJenaGraph(g);
        m = ModelFactory.createModelForGraph(jenaGraph);
        String q = "SELECT ?type  \n"
                + "WHERE {?canvas " + SC.hasImageAnnotations + " ?imageid .\n"
                + "?imageid "+ RDF.first + " ?resid . \n"
                + "?resid "+ OA.hasBody + " ?body .\n"
                + "?body "+ RDF.type + " ?type}";
        Query query = QueryFactory.create(q);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, m)) {
            ResultSet results = qexec.execSelect();
            if (results.hasNext()) {
                while (results.hasNext()) {
                    QuerySolution qs = results.next();
                    Resource type = qs.getResource("type").asResource();
                    assertEquals(DCTypes.Image.getIRIString(), type.getURI());
                    LOG.info(type.getURI());
                }
            } else {
                fail("not found");
            }
        }
    }

    @Test
    @Tag("must")
    @DisplayName("0027 BodiesMustBeIRI [5.4]")
    void test0027() throws IOException, JsonLdError {
        testResource = "0027-in.jsonld";
        g = getGraph(
                expandDocumentToN3fromFile(getTestResourceFromFile(TEST_DIR + "/" + testResource)));
        org.apache.jena.graph.Graph jenaGraph = rdf.asJenaGraph(g);
        m = ModelFactory.createModelForGraph(jenaGraph);
        String q = "SELECT ?body  \n"
                + "WHERE {?canvas " + SC.hasImageAnnotations + " ?imageid .\n"
                + "?imageid "+ RDF.first + " ?resid . \n"
                + "?resid "+ OA.hasBody + " ?body .\n"
                + "?body "+ RDF.type + " ?type}";
        Query query = QueryFactory.create(q);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, m)) {
            ResultSet results = qexec.execSelect();
            if (results.hasNext()) {
                while (results.hasNext()) {
                    QuerySolution qs = results.next();
                    Resource body = qs.getResource("body").asResource();
                    IRI b = rdf.createIRI(body.getURI());
                    assertTrue(b instanceof IRI);
                    LOG.info(body.getURI());
                }
            } else {
                fail("not found");
            }
        }
    }

    @Test
    @Tag("may")
    @DisplayName("0028 BodiesMayHaveFormat [3.3]")
    void test0028() throws IOException, JsonLdError {
        testResource = "0028-in.jsonld";
        g = getGraph(
                expandDocumentToN3fromFile(getTestResourceFromFile(TEST_DIR + "/" + testResource)));
        org.apache.jena.graph.Graph jenaGraph = rdf.asJenaGraph(g);
        m = ModelFactory.createModelForGraph(jenaGraph);
        String q = "SELECT ?format  \n"
                + "WHERE {?canvas " + SC.hasImageAnnotations + " ?imageid .\n"
                + "?imageid " + RDF.first + " ?resid . \n"
                + "?resid " + OA.hasBody + " ?body .\n"
                + "?body " + DCElements.format + " ?format}";
        Query query = QueryFactory.create(q);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, m)) {
            ResultSet results = qexec.execSelect();
            if (results.hasNext()) {
                while (results.hasNext()) {
                    QuerySolution qs = results.next();
                    Literal format = qs.getLiteral("format").asLiteral();
                    assertNotNull(format.getString());
                    LOG.info(format.getString());
                }
            } else {
                fail("not found");
            }
        }
    }

    @Test
    @Tag("may")
    @DisplayName("0029 BodiesMayHaveIntegerBounds [3.3]")
    void test0029() throws IOException, JsonLdError {
        testResource = "0029-in.jsonld";
        g = getGraph(
                expandDocumentToN3fromFile(getTestResourceFromFile(TEST_DIR + "/" + testResource)));
        org.apache.jena.graph.Graph jenaGraph = rdf.asJenaGraph(g);
        m = ModelFactory.createModelForGraph(jenaGraph);
        String q = "SELECT (COUNT(?canvas) as ?canvascount) ?height (COUNT(?height) as "
                + "?heightcount) \n"
                + "?width (COUNT  (?width) as ?widthcount)  \n"
                + "WHERE {?sequence " + SC.hasCanvases + " ?canvaslist .\n"
                + "?canvaslist " + RDF.rest + "* ?mid . \n"
                + "?mid " + RDF.first + " ?canvas .\n"
                + "?mid " + RDF.rest + " ?last . \n"
                + "?canvas " + SC.hasImageAnnotations + " ?imageid .\n"
                + "?imageid " + RDF.first + " ?resid . \n"
                + "?resid " + OA.hasBody + " ?body .\n"
                + "?body " + EXIF.height + " ?height . \n"
                + "?body " + EXIF.width + " ?width} GROUP BY ?height ?width";
        Query query = QueryFactory.create(q);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, m)) {
            ResultSet results = qexec.execSelect();
            if (results.hasNext()) {
                while (results.hasNext()) {
                    QuerySolution qs = results.next();
                    Literal cc = qs.getLiteral("canvascount").asLiteral();
                    Literal hc = qs.getLiteral("heightcount").asLiteral();
                    Literal wc = qs.getLiteral("widthcount").asLiteral();
                    Literal w = qs.getLiteral("width").asLiteral();
                    Literal h = qs.getLiteral("height").asLiteral();
                    assertEquals(cc, hc);
                    assertEquals(cc, wc);
                    assertEquals(XSD.integer.getIRIString(), w.getDatatypeURI());
                    assertEquals(XSD.integer.getIRIString(), h.getDatatypeURI());
                    LOG.info("Image width " + w.getString() + " Image height " + h.getString());
                }
            } else {
                fail("not found");
            }
        }
    }

    @Test
    @Tag("self")
    @DisplayName("0030 TypeNotPresentException")
    void test0030() throws IOException, JsonLdError {
        testResource = "0030-in.jsonld";
        g = getGraph(
                expandDocumentToN3fromFile(getTestResourceFromFile(TEST_DIR + "/" + testResource)));
        Throwable exception = assertThrows(RuntimeException.class, () -> {
            closeableFindAny(g.stream(null, RDF.type, SC.Range)).map(Triple::getSubject)
                    .orElseThrow(() ->
                            new RuntimeException(SC.Range.getIRIString() + " type not found"));
        });
        assertEquals(SC.Range.getIRIString() + " type not found", exception.getMessage());
        LOG.info("exception {} thrown", exception.getMessage());
    }

    @Test
    @Tag("constraint")
    @DisplayName("0031 InvalidPropertyPresent")
    void test0031() throws IOException, JsonLdError {
        testResource = "0031-in.jsonld";
        g = getGraph(
                expandDocumentToN3fromFile(getTestResourceFromFile(TEST_DIR + "/" + testResource)));
        models.forEach(type -> {
            final Optional<ConstraintViolation> res = svc.constrainedBy(type, null, g)
                    .filter(v -> v.getConstraint().equals(Trellis.InvalidProperty)).findFirst();
            assertTrue(res.isPresent());
            res.ifPresent(violation -> {
                assertEquals(Trellis.InvalidProperty, violation.getConstraint());
            });
        });
    }

    @Test
    @Tag("must")
    @DisplayName("0032 InvalidLiteralRange")
    void test0032() throws IOException, JsonLdError {
        testResource = "0032-in.jsonld";
        g = getGraph(
                expandDocumentToN3fromFile(getTestResourceFromFile(TEST_DIR + "/" + testResource)));
        models.forEach(type -> {
            final Optional<ConstraintViolation> res = svc.constrainedBy(type, null, g)
                    .filter(v -> v.getConstraint().equals(Trellis.InvalidRange)).findFirst();
            assertTrue(res.isPresent());
            res.ifPresent(violation -> {
                assertEquals(Trellis.InvalidRange, violation.getConstraint());
            });
        });
    }

    @Test
    @Tag("must")
    @DisplayName("0033 InvalidBNodeRange")
    void test0033() throws IOException, JsonLdError {
        testResource = "0033-in.jsonld";
        g = getGraph(
                expandDocumentToN3fromFile(getTestResourceFromFile(TEST_DIR + "/" + testResource)));
        models.forEach(type -> {
            final Optional<ConstraintViolation> res = svc.constrainedBy(type, null, g)
                    .filter(v -> v.getConstraint().equals(Trellis.InvalidRange)).findFirst();
            assertTrue(res.isPresent());
            res.ifPresent(violation -> {
                assertEquals(Trellis.InvalidRange, violation.getConstraint());
            });
        });
    }
}

