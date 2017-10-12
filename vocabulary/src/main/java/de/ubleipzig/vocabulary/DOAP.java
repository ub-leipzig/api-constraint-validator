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

import org.apache.commons.rdf.api.IRI;

/**
 * RDF Terms from the Dublin Core Vocabulary
 *
 * @author acoburn
 * @see <a href="http://usefulinc.com/ns/doap#">DOAP Vocabulary</a>
 */
public final class DOAP extends BaseVocabulary {

    /* Namespace */
    public static final String URI = "http://usefulinc.com/ns/doap#";

    /* Properties */
    public static final IRI implement = createIRI(URI + "implements");


    private DOAP() {
        super();
    }
}
