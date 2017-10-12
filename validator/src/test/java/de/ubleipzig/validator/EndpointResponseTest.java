package de.ubleipzig.validator;

import de.ubleipzig.vocabulary.DCElements;
import de.ubleipzig.vocabulary.DOAP;
import de.ubleipzig.vocabulary.SC;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.RDFTerm;
import org.apache.commons.rdf.api.Triple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.trellisldp.vocabulary.RDF;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import static de.ubleipzig.validator.ApacheClient.headApacheClientResponse;
import static de.ubleipzig.validator.ApacheClient.optionsApacheClientResponse;
import static de.ubleipzig.validator.IO.closeableFindAny;
import static org.apache.jena.riot.WebContent.contentTypeJSONLD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

public class EndpointResponseTest extends IIIFTestSuite {

    @Test
    @Tag("api")
    @DisplayName("a0001 CanvasShouldBeDereferenceable [3.3]")
    void testa0001() throws IOException {
        BlankNodeOrIRI c =
                closeableFindAny(graph.stream(null, RDF.type, SC.Canvas)).map(
                        Triple::getSubject).orElseThrow(() ->
                        new RuntimeException(SC.Canvas.getIRIString() + " type not found"));
        URL url = new URL(rdf.asJenaNode(c).getURI());
        org.apache.http.HttpResponse response;
        try {
            response = headApacheClientResponse(url.toString(), contentTypeJSONLD);
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
    @Tag("api")
    @DisplayName("a0002 BodywithFormatMustBeDereferenceableWithContentType [3.3]")
    void testa0002()
            throws IOException, URISyntaxException, InterruptedException {
        BlankNodeOrIRI s =
                closeableFindAny(graph.stream(null, DCElements.format, null)).map(
                        Triple::getSubject).orElseThrow(() ->
                        new RuntimeException(DCElements.format.getIRIString() + " type not found"));
        URL uri = new URL(rdf.asJenaNode(s).getURI());
        RDFTerm format =
                closeableFindAny(graph.stream(null, DCElements.format, null)).map(
                        Triple::getObject).orElseThrow(() ->
                        new RuntimeException(DCElements.format.getIRIString() + " type not found"));
        org.apache.http.HttpResponse response;
        try {
            response = headApacheClientResponse(uri.toString(), contentTypeJSONLD);
            String ct = null;
            if (response != null) {
                assertEquals(200, response.getStatusLine().getStatusCode());
                ct = response.getFirstHeader("Content-Type").getValue();
                assertEquals(format.ntriplesString().replaceAll("\"", ""), ct);
            }
            LOG.info(ct);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    @Tag("api")
    @DisplayName("a0003 ManifestContentTypeShouldBeJsonLD [4]")
    void testa0003() throws MalformedURLException {
        BlankNodeOrIRI id =
                closeableFindAny(graph.stream(null, RDF.type, SC.Manifest)).map(
                        Triple::getSubject).orElse(null);
        URL uri;
        if (id != null) {
            uri = new URL(rdf.asJenaNode(id).getURI());
        } else {
            uri = new URL(testResource);
        }
        org.apache.http.HttpResponse response;
        try {
            response = headApacheClientResponse(uri.toString(), contentTypeJSONLD);
            String ct = null;
            if (response != null) {
                assertEquals(200, response.getStatusLine().getStatusCode());
                ct = response.getFirstHeader("Content-Type").getValue();
                assertEquals(contentTypeJSONLD, ct);
            }
            LOG.info(ct);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    @Tag("api")
    @DisplayName("a0004 ResponseMustIncludeLinkHeaderWithContext [4]")
    void testa0004() throws MalformedURLException {
        BlankNodeOrIRI id =
                closeableFindAny(graph.stream(null, RDF.type, SC.Manifest)).map(
                        Triple::getSubject).orElse(null);
        URL uri;
        if (id != null) {
            uri = new URL(rdf.asJenaNode(id).getURI());
        } else {
            uri = new URL(testResource);
        }
        org.apache.http.HttpResponse response;
        try {
            response = headApacheClientResponse(uri.toString(), contentTypeJSONLD);
            String link = null;
            if (response != null) {
                assertEquals(200, response.getStatusLine().getStatusCode());
                LOG.info("Status Code is {} ", response.getStatusLine().getStatusCode());
                if (response.containsHeader("Link")) {
                    link = response.getFirstHeader("Link").getValue();
                } else {
                    fail("not found");
                }
                LOG.info("Link Header is {} ", link);
                assertEquals("<" + context + ">; rel=\"http://www" +
                        ".w3.org/ns/json-ld#context\"; type=\"application/ld+json\"", link);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    @Tag("api")
    @DisplayName("a0005 IIIFImageServiceMustAllowOPTIONS [I-5.1]")
    void testa0005()
            throws IOException, URISyntaxException, InterruptedException {
        BlankNodeOrIRI s =
                closeableFindAny(graph.stream(null, DOAP.implement, null)).map(
                        Triple::getSubject).orElse(null);
        URL uri;
        if (s != null) {
            uri = new URL(rdf.asJenaNode(s).getURI() + "/info.json");
        } else {
            uri = new URL(testResource);
        }
        org.apache.http.HttpResponse response;
        try {
            response = optionsApacheClientResponse(uri.toString(), contentTypeJSONLD);
            String allow = null;
            if (response != null) {
                assertEquals(200, response.getStatusLine().getStatusCode());
                allow = response.getFirstHeader("Access-Control-Allow-Origin").getValue();
                assertNotNull(allow);
            }
            LOG.info(allow);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
