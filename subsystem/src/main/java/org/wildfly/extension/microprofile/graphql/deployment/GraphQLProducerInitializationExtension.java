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

package org.wildfly.extension.microprofile.graphql.deployment;

import io.smallrye.graphql.cdi.producer.GraphQLProducer;
import io.smallrye.graphql.schema.model.Schema;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;

public class GraphQLProducerInitializationExtension implements Extension {

    private final Schema schema;

    public GraphQLProducerInitializationExtension(Schema schema) {
        this.schema = schema;
    }

    // to properly initialize the GraphQL runtime, we need to access the GraphQLProducer bean and initialize it with a schema
    void initializeGraphQLProducer(@Observes final AfterDeploymentValidation avd, BeanManager bm) {
        bm.createInstance().select(GraphQLProducer.class).get().initialize(schema);
    }

}
