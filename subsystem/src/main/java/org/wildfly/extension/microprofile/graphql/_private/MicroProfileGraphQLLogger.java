/*
 * Copyright 2019 Red Hat, Inc.
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

package org.wildfly.extension.microprofile.graphql._private;

import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

import static org.jboss.logging.Logger.Level.INFO;
import static org.jboss.logging.Logger.Level.WARN;

@MessageLogger(projectCode = "WFLYGRAPHQL", length = 4)
public interface MicroProfileGraphQLLogger extends BasicLogger {

    MicroProfileGraphQLLogger LOGGER = Logger.getMessageLogger(MicroProfileGraphQLLogger.class, "org.wildfly.extension.microprofile.graphql.smallrye");

    @LogMessage(level = INFO)
    @Message(id = 1, value = "Activating Eclipse MicroProfile GraphQL Subsystem")
    void activatingSubsystem();

    @LogMessage(level = INFO)
    @Message(id = 2, value = "Activating GraphQL for deployment %s")
    void activatingGraphQLForDeployment(String deployment);

    @LogMessage(level = INFO)
    @Message(id = 3, value = "Found %s queries and %s mutations in the API")
    void foundOperations(int queries, int mutations);

    @LogMessage(level = WARN)
    @Message(id = 4, value = "Could not update %s because the contents are not as expected")
    void couldNotUpdateRenderJs(String path);

    @Message(id = 100, value = "Multiple GraphQLApi annotations were found")
    DeploymentUnitProcessingException multipleGraphQLApiAnnotations();

}
