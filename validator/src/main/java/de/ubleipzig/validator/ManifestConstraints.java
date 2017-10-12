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

import static java.util.Collections.unmodifiableSet;
import static java.util.Objects.requireNonNull;
import static java.util.Optional.of;
import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;
import static org.slf4j.LoggerFactory.getLogger;

import de.ubleipzig.vocabulary.EXIF;
import de.ubleipzig.vocabulary.SC;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apache.commons.rdf.api.BlankNode;
import org.apache.commons.rdf.api.BlankNodeOrIRI;
import org.apache.commons.rdf.api.Graph;
import org.apache.commons.rdf.api.IRI;
import org.apache.commons.rdf.api.Literal;
import org.apache.commons.rdf.api.Triple;
import org.slf4j.Logger;
import org.trellisldp.api.ConstraintService;
import org.trellisldp.api.ConstraintViolation;
import org.trellisldp.vocabulary.OA;
import org.trellisldp.vocabulary.RDF;
import org.trellisldp.vocabulary.RDFS;
import org.trellisldp.vocabulary.Trellis;

/**
 * ManifestConstraints.
 *
 * @author acoburn
 * @author christopher-johnson
 */
public class ManifestConstraints implements ConstraintService {

    private static final Logger LOGGER = getLogger(ManifestConstraints.class);

    // Identify those predicates that are not allowed in the given ixn model
    private static final Predicate<Triple> basicConstraints =
            triple -> triple.getPredicate().equals(OA.hasPurpose);
    //triple -> triple.getPredicate().equals(RDF.value) ||
    //        triple.getPredicate().equals(RDFS.label);

    private static final Map<IRI, Predicate<Triple>> typeMap;

    static {
        typeMap = Map.of(SC.Manifest, basicConstraints);
    }

    private static final Set<IRI> propertiesWithUriRange = Set.of(RDF.type);

    private static final Set<IRI> propertiesWithBnodeRange =
            Set.of(SC.hasAnnotations, SC.hasCanvases, SC.hasCollections, SC.hasContentLayer,
                    SC.hasImageAnnotations, SC.hasManifests, SC.hasRanges, SC.hasSequences,
                    SC.hasStartCanvas, SC.hasLists);

    private static final Set<IRI> propertiesWithBnodeOrIriRange = Set.of(RDF.first, RDF.rest);

    private static final Set<IRI> propertiesWithLiteralRange =
            Set.of(RDFS.label, RDF.value, EXIF.width, EXIF.height);

    private static final Set<IRI> rangeProperties;

    static {
        final Set<IRI> data = new HashSet<>();
        data.addAll(propertiesWithUriRange);
        data.addAll(propertiesWithBnodeRange);
        data.addAll(propertiesWithBnodeOrIriRange);
        data.addAll(propertiesWithLiteralRange);
        rangeProperties = unmodifiableSet(data);
    }

    // Ensure that any properties are appropriate for the interaction model
    private static Predicate<Triple> propertyFilter(final IRI model) {
        return of(model).filter(typeMap::containsKey).map(typeMap::get).orElse(basicConstraints);
    }

    // Verify that the object of a triple is within an acceptable Range of the property
    private static Predicate<Triple> invalidRangeProperty =
            triple -> rangeProperties.contains(triple.getObject());

    // Verify that the range of the property is an IRI (if the property is in rangeProperties)
    private static Predicate<Triple> uriRangeFilter = invalidRangeProperty
            .or(triple -> propertiesWithUriRange.contains(triple.getPredicate()) &&
                    !(triple.getObject() instanceof IRI));

    // Verify that the range of the property is an Bnode (if the property is in rangeProperties)
    private static Predicate<Triple> bnodeRangeFilter = invalidRangeProperty
            .or(triple -> propertiesWithBnodeRange.contains(triple.getPredicate()) &&
                    !(triple.getObject() instanceof BlankNode));


    // Verify that the range of the property is a Blank Node or IRI (if the property is in rangeProperties)
    private static Predicate<Triple> bnodeOrIriRangeFilter = invalidRangeProperty
            .or(triple -> propertiesWithBnodeOrIriRange.contains(triple.getPredicate()) &&
                    !(triple.getObject() instanceof BlankNodeOrIRI));


    // Verify that the range of the property is an Literal (if the property is in rangeProperties)
    private static Predicate<Triple> literalRangeFilter = invalidRangeProperty
            .or(triple -> propertiesWithLiteralRange.contains(triple.getPredicate()) &&
                    !(triple.getObject() instanceof Literal));

    private static Boolean hasValidProps(final Map<IRI, Long> data) {
        Long val = data.getOrDefault(RDF.rest, 0L);
        if (val >= 1L) {
            return true;
        }
        return false;
    }

    private static Predicate<Graph> checkCardinality(final IRI model) {
        return graph -> {
            final Map<IRI, Long> cardinality =
                    graph.stream().filter(triple -> rangeProperties.contains(triple.getPredicate()))
                            .collect(groupingBy(Triple::getPredicate, counting()));

            if (hasValidProps(cardinality)) {
                return true;
            }
            return false;
        };
    }

    private Function<Triple, Stream<ConstraintViolation>> checkModelConstraints(final IRI model,
                                                                                final String
                                                                                        domain) {
        requireNonNull(model, "The interaction model must not be null!");

        return triple -> {
            final Stream.Builder<ConstraintViolation> builder = Stream.builder();

            of(triple).filter(propertyFilter(model))
                    .map(t -> new ConstraintViolation(Trellis.InvalidProperty, t))
                    .ifPresent(builder);

            //  of(triple).filter(typeFilter).map(t -> new ConstraintViolation(Trellis
            // .InvalidType, t))
            //        .ifPresent(builder::accept);

            of(triple).filter(uriRangeFilter)
                    .map(t -> new ConstraintViolation(Trellis.InvalidRange, t)).ifPresent(builder);

            of(triple).filter(bnodeRangeFilter)
                    .map(t -> new ConstraintViolation(Trellis.InvalidRange, t)).ifPresent(builder);

            of(triple).filter(bnodeOrIriRangeFilter)
                    .map(t -> new ConstraintViolation(Trellis.InvalidRange, t)).ifPresent(builder);

            of(triple).filter(literalRangeFilter)
                    .map(t -> new ConstraintViolation(Trellis.InvalidRange, t)).ifPresent(builder);

            //of(triple).filter(inDomainRangeFilter(domain)).map(t -> new ConstraintViolation
            // (Trellis.InvalidRange, t))
            //      .ifPresent(builder::accept);

            return builder.build();
        };
    }

    @Override
    public Stream<ConstraintViolation> constrainedBy(final IRI model, final String domain,
                                                     final Graph graph) {
        return graph.stream().flatMap(checkModelConstraints(model, domain))
                .peek(x -> LOGGER.info("Constraint violation: {}", x));
    }
}

