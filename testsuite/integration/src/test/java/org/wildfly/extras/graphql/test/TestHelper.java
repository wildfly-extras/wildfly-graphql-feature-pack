/*
 * Copyright 2020 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wildfly.extras.graphql.test;

import javax.json.Json;
import javax.json.JsonObject;

public class TestHelper {

    public static final String MEDIATYPE_JSON = "application/json";
    static final String MEDIATYPE_TEXT = "text/plain";
    static final String QUERY = "query";
    static final String VARIABLES = "variables";

    public static String getPayload(String query) {
        JsonObject jsonObject = createRequestBody(query);
        return jsonObject.toString();
    }

    protected static JsonObject createRequestBody(String graphQL) {
        return createRequestBody(graphQL, null);
    }

    protected static JsonObject createRequestBody(String graphQL, JsonObject variables) {
        if (variables == null || variables.isEmpty()) {
            variables = Json.createObjectBuilder().build();
        }
        return Json.createObjectBuilder().add(QUERY, graphQL).add(VARIABLES, variables).build();
    }

}
