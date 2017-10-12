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

import static de.ubleipzig.validator.ApacheClient.getApacheClientResponse;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.rdf.api.RDFSyntax.NTRIPLES;
import static org.apache.jena.riot.WebContent.contentTypeJSONLD;
import static org.apache.jena.rdf.model.ModelFactory.createDefaultModel;
import static org.apache.jena.riot.Lang.N3;
import static org.apache.jena.riot.RDFDataMgr.read;
import static org.slf4j.LoggerFactory.getLogger;

import com.github.jsonldjava.core.JsonLdConsts;
import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JsonUtils;
import de.ubleipzig.vocabulary.JSONReader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.rdf.api.Graph;
import org.apache.commons.rdf.api.Triple;
import org.apache.commons.rdf.jena.JenaRDF;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.slf4j.Logger;
import org.trellisldp.api.IOService;
import org.trellisldp.api.RuntimeRepositoryException;
import org.trellisldp.io.JenaIOService;

public class IO {
    private static Logger LOG = getLogger(IO.class.getName());
    private static final JenaRDF rdf = new JenaRDF();
    private static final IOService ioService = new JenaIOService(null);

    public Graph asGraphfromFile(final String resource, final String context) {
        final Model model = createDefaultModel();
        read(model, getClass().getResourceAsStream(resource), context, N3);
        return rdf.asGraph(model);
    }

    public Model asJenaModelfromFile(final String resource, final String context) {
        final Model model = createDefaultModel();
        read(model, getClass().getResourceAsStream(resource), context, N3);
        return model;
    }

    static Graph getGraphwithService(InputStream stream) {
        final Graph graph = rdf.createGraph();
        ioService.read(stream, null, NTRIPLES).forEach(graph::add);
        LOG.info("graph size is {}", graph.size());
        return graph;
    }

    static InputStream expandDocumentToN3fromFile(final InputStream is)
            throws IOException, JsonLdError {
        JsonLdOptions options = new JsonLdOptions();
        options.format = JsonLdConsts.APPLICATION_NQUADS;
        Object expanded = JsonLdProcessor.toRDF(JsonUtils.fromInputStream(is), options);
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final Writer writer = new OutputStreamWriter(out, UTF_8);
        writer.write(String.valueOf(expanded));
        writer.flush();
        return new ByteArrayInputStream(out.toByteArray());
    }

    static InputStream expandDocumentToN3(final URL testUri)
            throws IOException, JsonLdError {
        JsonLdOptions options = new JsonLdOptions();
        options.format = JsonLdConsts.APPLICATION_NQUADS;
        Object expanded = JsonLdProcessor
                .toRDF(
                        JsonUtils.fromInputStream(
                                getApacheClientResponse(testUri.toString(), contentTypeJSONLD)),
                        options);
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        final Writer writer = new OutputStreamWriter(out, UTF_8);
        writer.write(String.valueOf(expanded));
        writer.flush();
        return new ByteArrayInputStream(out.toByteArray());
    }

    static Optional<? extends Triple> closeableFindAny(Stream<? extends Triple> stream) {
        try (Stream<? extends Triple> s = stream) {
            return s.findAny();
        }
    }

    static Graph getGraph(InputStream stream) {
        final Model model = createDefaultModel();
        final Lang lang = rdf.asJenaLang(NTRIPLES).orElseThrow(() -> new RuntimeRepositoryException(
                "Unsupported RDF Syntax: " + NTRIPLES.mediaType));
        RDFDataMgr.read(model, stream, null, lang);
        return rdf.asGraph(model);
    }

    public static void saveFile(String graphs) throws IOException {
        String p = "expanded.n3";
        //String p = this.getClass().getResource("annotations.n3").getPath();
        PrintWriter writer = new PrintWriter(p);
        writer.write(graphs);
    }

}
