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

import static de.ubleipzig.validator.IO.closeableFindAny;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.slf4j.LoggerFactory.getLogger;

import de.ubleipzig.vocabulary.DCElements;
import de.ubleipzig.vocabulary.DCTypes;
import de.ubleipzig.vocabulary.EXIF;
import de.ubleipzig.vocabulary.SC;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.RDFTerm;
import org.apache.commons.rdf.api.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Literal;
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
 * RemoteManifestTest.
 *
 * @author christopher-johnson
 */
public class RemoteManifestTest extends IIIFTestSuite {

    private Logger LOG = getLogger(TestLifecycleLogger.class.getName());

    private static String domain = DomainProperties.testDomain;

    private final ConstraintService svc = new ManifestConstraints();

    @Test
    @Tag("must")
    @DisplayName("r0001 ManifestMustHaveType [3.3]")
    void testr0001() {
        assertTrue(
                closeableFindAny(graph.stream(rdf.createIRI(testResource), RDF.type, SC.Manifest))
                        .isPresent());
    }

    @Test
    @Tag("must")
    @DisplayName("r0002 ManifestIdMustEqualTestResource [3.3]")
    void testr0002() throws MalformedURLException {
        BlankNodeOrIRI id =
                closeableFindAny(graph.stream(null, RDF.type, SC.Manifest)).map(
                        Triple::getSubject).orElseThrow(() ->
                        new RuntimeException(SC.Manifest.getIRIString() + " type not found"));
        URL url = new URL(rdf.asJenaNode(id).getURI());
        LOG.info("manifest IRI is {}", id.toString());
        assertEquals(testResource, url.toString());
    }

    @Test
    @Tag("must")
    @DisplayName("r0003 ManifestIdMustBeIRI [3.3]")
    void testr0003() {
        BlankNodeOrIRI id =
                closeableFindAny(graph.stream(null, RDF.type, SC.Manifest)).map(
                        Triple::getSubject).orElseThrow(() ->
                        new RuntimeException(SC.Manifest.getIRIString() + " type not found"));
        assertTrue(id instanceof IRI);
        LOG.info("Test Resource " + testResource);
        LOG.info("Manifest IRI " + id.toString());
    }

    @Test
    @Tag("must")
    @DisplayName("r0004 ManifestMustHaveLabel [3.1]")
    void testr0004() {
        assertTrue(closeableFindAny(graph.stream(rdf.createIRI(testResource), RDFS.label, null))
                .isPresent());
    }

    @Test
    @Tag("should")
    @DisplayName("r0005 ManifestShouldHaveThumbnail [3.1]")
    void testr0005() {
        assertTrue(closeableFindAny(
                graph.stream(rdf.createIRI(testResource), rdf.createIRI(FOAF.thumbnail.toString()),
                        null)).isPresent());
    }

    @Test
    @Tag("should")
    @DisplayName("r0006 ManifestShouldHaveDescription [3.1]")
    void testr0006() {
        assertTrue(closeableFindAny(
                graph.stream(rdf.createIRI(testResource), DCElements.description, null))
                .isPresent());
    }

    @Test
    @Tag("should")
    @DisplayName("r0007 ManifestShouldHaveMetadata [3.1]")
    void testr0007() {
        assertTrue(
                closeableFindAny(graph.stream(rdf.createIRI(testResource), SC.metadataLabels, null))
                        .isPresent());
    }

    @Test
    @Tag("may")
    @DisplayName("r0008 ManifestMayHaveStructures [5.1]")
    void testr0008() {
        assertTrue(closeableFindAny(graph.stream(rdf.createIRI(testResource), SC.hasRanges, null))
                .isPresent());
    }

    @Test
    @Tag("must")
    @DisplayName("r0009 RangeIdMustBeIRI [3.3]")
    void testr0009() {
        RDFTerm id = closeableFindAny(graph.stream(null, RDF.type, SC.Range)).map(
                Triple::getSubject).orElseThrow(() ->
                new RuntimeException(SC.Range.getIRIString() + " type not found"));
        assertTrue(id instanceof IRI);
    }

    @Test
    @Tag("must")
    @DisplayName("r0010 hasSequenceMustBePresent [5.1]t")
    void testr0010() {
        assertTrue(
                closeableFindAny(graph.stream(rdf.createIRI(testResource), SC.hasSequences, null))
                        .isPresent());
    }

    @Test
    @Tag("must")
    @DisplayName("r0011 SequenceMustHaveType [3.3]")
    void testr0011() {
        assertTrue(closeableFindAny(graph.stream(null, RDF.type, SC.Sequence)).isPresent());
    }

    @Test
    @Tag("must")
    @DisplayName("r0012 SequenceIdMustBeIRI [3.3]")
    void testr0012() {
        BlankNodeOrIRI id =
                closeableFindAny(graph.stream(null, RDF.type, SC.Sequence)).map(
                        Triple::getSubject).orElseThrow(() ->
                        new RuntimeException(SC.Sequence.getIRIString() + " type not found"));
        assertTrue(id instanceof IRI);
    }

    @Test
    @Tag("may")
    @DisplayName("r0013 SequenceMayHaveStartCanvas [3.4]")
    void testr0013() {
        RDFTerm id =
                closeableFindAny(graph.stream(null, SC.hasStartCanvas, null)).map(
                        Triple::getObject).orElseThrow(() ->
                        new RuntimeException(SC.hasStartCanvas.getIRIString() + " type not found"));
        assertNotNull(id);
    }

    @Test
    @Tag("must")
    @DisplayName("r0014 MultipleSequencesMustHaveLabel [3.1]")
    void testr0014() {
        long seqCount;
        try (Stream<? extends Triple> stream = graph.stream(null, RDF.type, SC.Sequence)) {
            seqCount = stream.count();
        }
        assertTrue(seqCount >= 1);

        if (seqCount > 1) {
            String q = "SELECT (COUNT(?label) AS ?labelcount)  \n"
                    + "WHERE { ?manifest " + SC.hasSequences + " ?seqid .\n"
                    + "?seqid " + RDF.first + " ?sequence . \n"
                    + "?sequence " + RDFS.label + " ?label}";
            Query query = QueryFactory.create(q);
            try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
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
    @DisplayName("r0015 SequenceMustHaveCanvas [5.2]")
    void testr0015() {
        long seqCount;
        try (Stream<? extends Triple> stream = graph.stream(null, RDF.type, SC.Sequence)) {
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
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
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
    @DisplayName("r0016 CanvasIdMustBeIRI [3.3]")
    void testr0016() {
        BlankNodeOrIRI id =
                closeableFindAny(graph.stream(null, RDF.type, SC.Canvas)).map(
                        Triple::getSubject).orElseThrow(() ->
                        new RuntimeException(SC.Canvas.getIRIString() + " type not found"));
        assertTrue(id instanceof IRI);
    }

    @Test
    @Tag("must")
    @DisplayName("r0018 CanvasesMustHaveType [3.3]")
    void testr0018() {
        final List<RDFTerm> canvases = new ArrayList<>();
        final List<RDFTerm> canvastypes = new ArrayList<>();
        for (final Triple t : graph.iterate(null, SC.hasImageAnnotations, null)) {
            canvases.add(t.getSubject());
        }
        for (final Triple t : graph.iterate(null, RDF.type, SC.Canvas)) {
            canvastypes.add(t.getSubject());
        }
        assertEquals(canvastypes.size(), canvases.size());
        LOG.info("Canvas Count " + canvases.size());
    }

    @Test
    @Tag("must")
    @DisplayName("r0019 CanvasesMustHaveLabel [3.1]")
    void testr0019() {
        String q = "SELECT (COUNT(?canvas) as ?canvascount) (COUNT(?canvaslabel) as ?labelcount) \n"
                + "WHERE {?sequence " + SC.hasCanvases + " ?canvaslist .\n"
                + "?canvaslist " + RDF.rest + "* ?mid . \n"
                + "?mid " + RDF.first + " ?canvas .\n"
                + "?mid " + RDF.rest + " ?last . \n"
                + "?canvas " + SC.hasImageAnnotations + " ?imageid .\n"
                + "?canvas " + RDFS.label + " ?canvaslabel}";
        Query query = QueryFactory.create(q);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
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
    @DisplayName("r0020 CanvasesMustHaveIntegerBounds [3.3]")
    void testr0020() {
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
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
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
    @DisplayName("r0021 AnnotationsMustHaveType [3.3]")
    void testr0021() {
        final List<RDFTerm> annotations = new ArrayList<>();
        final List<RDFTerm> annotypes = new ArrayList<>();
        for (final Triple t : graph.iterate(null, SC.hasImageAnnotations, null)) {
            annotations.add(t.getObject());
        }
        for (final Triple t : graph.iterate(null, RDF.type, OA.Annotation)) {
            annotypes.add(t.getSubject());
        }
        assertEquals(annotations.size(), annotypes.size());
        LOG.info("Annotations Count " + annotations.size());
    }

    @Test
    @Tag("must")
    @DisplayName("r0022 AnnotationsMustBeMotivated [5.4]")
    void testr0022() {
        final List<RDFTerm> annotations = new ArrayList<>();
        final List<RDFTerm> annotypes = new ArrayList<>();
        for (final Triple t : graph.iterate(null, SC.hasImageAnnotations, null)) {
            annotations.add(t.getObject());
        }
        for (final Triple t : graph.iterate(null, OA.motivatedBy, SC.painting)) {
            annotypes.add(t.getSubject());
        }
        assertEquals(annotations.size(), annotypes.size());
        LOG.info("Annotations Count " + annotations.size());
    }

    @Test
    @Tag("must")
    @DisplayName("r0023 AnnotationsMustHaveTarget [5.4]")
    void testr0023() {
        final List<RDFTerm> annotations = new ArrayList<>();
        final List<RDFTerm> targets = new ArrayList<>();
        for (final Triple t : graph.iterate(null, SC.hasImageAnnotations, null)) {
            annotations.add(t.getObject());
        }
        for (final Triple t : graph.iterate(null, OA.hasTarget, null)) {
            targets.add(t.getObject());
        }
        assertEquals(annotations.size(), targets.size());
        LOG.info("Annotations Count " + annotations.size());
    }

    @Test
    @Tag("must")
    @DisplayName("r0024 AnnotationsMustHaveBody [5.4]")
    void testr0024() {
        final List<RDFTerm> annotations = new ArrayList<>();
        final List<RDFTerm> bodies = new ArrayList<>();
        for (final Triple t : graph.iterate(null, SC.hasImageAnnotations, null)) {
            annotations.add(t.getObject());
        }
        for (final Triple t : graph.iterate(null, OA.hasBody, null)) {
            bodies.add(t.getObject());
        }
        assertEquals(annotations.size(), bodies.size());
        LOG.info("Annotations Count " + annotations.size());
    }

    @Test
    @Tag("must")
    @DisplayName("r0025 BodiesMustHaveType [5.4]")
    void testr0025() {
        String q = "SELECT ?type  \n"
                + "WHERE {?canvas " + SC.hasImageAnnotations + " ?imageid .\n"
                + "?imageid " + RDF.first + " ?resid . \n"
                + "?resid " + OA.hasBody + " ?body .\n"
                + "?body " + RDF.type + " ?type}";
        Query query = QueryFactory.create(q);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
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
    @DisplayName("r0026 BodiesShouldHaveDCType [5.4]")
    void testr0026() {
        String q = "SELECT ?type  \n"
                + "WHERE {?canvas " + SC.hasImageAnnotations + " ?imageid .\n"
                + "?imageid " + RDF.first + " ?resid . \n"
                + "?resid " + OA.hasBody + " ?body .\n"
                + "?body " + RDF.type + " ?type}";
        Query query = QueryFactory.create(q);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
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
    @DisplayName("r0027 BodiesMustBeIRI [5.4]")
    void testr0027() {
        String q = "SELECT ?body  \n"
                + "WHERE {?canvas " + SC.hasImageAnnotations + " ?imageid .\n"
                + "?imageid " + RDF.first + " ?resid . \n"
                + "?resid " + OA.hasBody + " ?body .\n"
                + "?body " + RDF.type + " ?type}";
        Query query = QueryFactory.create(q);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
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
    @DisplayName("r0028 BodiesMayHaveFormat [3.3]")
    void testr0028() {
        String q = "SELECT ?format  \n"
                + "WHERE {?canvas " + SC.hasImageAnnotations + " ?imageid .\n"
                + "?imageid " + RDF.first + " ?resid . \n"
                + "?resid " + OA.hasBody + " ?body .\n"
                + "?body " + DCElements.format + " ?format}";
        Query query = QueryFactory.create(q);
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
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
    @DisplayName("r0029 BodiesMayIntegerBounds [3.3]")
    void testr0029() {
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
        try (QueryExecution qexec = QueryExecutionFactory.create(query, model)) {
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
    @Tag("must")
    @DisplayName("r0031 InvalidPropertyPresent")
    void testr0031() {
        models.forEach(type -> {
            final Optional<ConstraintViolation> res = svc.constrainedBy(type, domain, graph)
                    .filter(v -> v.getConstraint().equals(Trellis.InvalidProperty)).findFirst();
            assertFalse(res.isPresent());
            res.ifPresent(violation -> {
                assertEquals(Trellis.InvalidProperty, violation.getConstraint());
            });
        });
    }

    @Test
    @Tag("must")
    @DisplayName("r0032 InvalidRangePresent")
    void testr0032() {
        models.forEach(type -> {
            final Optional<ConstraintViolation> res = svc.constrainedBy(type, domain, graph)
                    .filter(v -> v.getConstraint().equals(Trellis.InvalidRange)).findFirst();
            assertFalse(res.isPresent());
            res.ifPresent(violation -> {
                assertEquals(Trellis.InvalidRange, violation.getConstraint());
            });
        });
    }
}

