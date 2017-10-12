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

import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

class ApacheClient {

    static InputStream getApacheClientResponse(String uri, String accept) throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpGet get = new HttpGet(uri);
        get.setHeader("Accept", accept);
        HttpResponse response = client.execute(get);
        HttpEntity out = response.getEntity();
        return out.getContent();
    }

    static HttpResponse headApacheClientResponse(String requestUri, String accept)
            throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpHead head = new HttpHead(requestUri);
        head.setHeader("Accept", accept);
        return client.execute(head);
    }

    static HttpResponse optionsApacheClientResponse(String requestUri, String accept)
            throws IOException {
        CloseableHttpClient client = HttpClients.createDefault();
        HttpOptions options = new HttpOptions(requestUri);
        options.setHeader("Accept", accept);
        return client.execute(options);
    }
}
