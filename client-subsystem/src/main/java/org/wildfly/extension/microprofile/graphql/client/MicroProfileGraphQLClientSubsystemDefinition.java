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

package org.wildfly.extension.microprofile.graphql.client;

import org.jboss.as.controller.AbstractBoottimeAddStepHandler;
import org.jboss.as.controller.AttributeDefinition;
import org.jboss.as.controller.ModelOnlyRemoveStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.PersistentResourceDefinition;
import org.jboss.as.controller.capability.RuntimeCapability;
import org.jboss.as.server.AbstractDeploymentChainStep;
import org.jboss.as.server.DeploymentProcessorTarget;
import org.jboss.as.server.deployment.Phase;
import org.jboss.dmr.ModelNode;
import org.wildfly.extension.microprofile.graphql.client._private.MicroProfileGraphQLClientLogger;
import org.wildfly.extension.microprofile.graphql.client.deployment.MicroProfileGraphQLClientDependencyProcessor;
import org.wildfly.extension.microprofile.graphql.client.deployment.MicroProfileGraphQLClientDeploymentProcessor;

import java.util.Collection;
import java.util.Collections;

import static org.wildfly.extension.microprofile.graphql.client.MicroProfileGraphQLClientExtension.CONFIG_CAPABILITY_NAME;
import static org.wildfly.extension.microprofile.graphql.client.MicroProfileGraphQLClientExtension.SUBSYSTEM_NAME;
import static org.wildfly.extension.microprofile.graphql.client.MicroProfileGraphQLClientExtension.SUBSYSTEM_PATH;
import static org.wildfly.extension.microprofile.graphql.client.MicroProfileGraphQLClientExtension.WELD_CAPABILITY_NAME;

public class MicroProfileGraphQLClientSubsystemDefinition extends PersistentResourceDefinition {

    private static final String GRAPHQL_CAPABILITY_NAME = "org.wildfly.microprofile.graphql.client";

    private static final RuntimeCapability<Void> GRAPHQL_CAPABILITY = RuntimeCapability.Builder
            .of(GRAPHQL_CAPABILITY_NAME)
            .addRequirements(WELD_CAPABILITY_NAME)
            .addRequirements(CONFIG_CAPABILITY_NAME)
            .build();

    public MicroProfileGraphQLClientSubsystemDefinition() {
        super(
                new Parameters(
                        SUBSYSTEM_PATH,
                        MicroProfileGraphQLClientExtension.getResourceDescriptionResolver(SUBSYSTEM_NAME))
                        .setAddHandler(AddHandler.INSTANCE)
                        .setRemoveHandler(new ModelOnlyRemoveStepHandler())
                        .setCapabilities(GRAPHQL_CAPABILITY)
        );
    }

    @Override
    public Collection<AttributeDefinition> getAttributes() {
        return Collections.emptyList();
    }

    static class AddHandler extends AbstractBoottimeAddStepHandler {

        static AddHandler INSTANCE = new AddHandler();

        private AddHandler() {
            super(Collections.emptyList());
        }

        @Override
        protected void performBoottime(OperationContext context, ModelNode operation, ModelNode model) throws OperationFailedException {
            super.performBoottime(context, operation, model);

            context.addStep(new AbstractDeploymentChainStep() {
                public void execute(DeploymentProcessorTarget processorTarget) {
                    final int DEPENDENCIES_MICROPROFILE_GRAPHQL = 6288;
                    processorTarget.addDeploymentProcessor(SUBSYSTEM_NAME, Phase.DEPENDENCIES, DEPENDENCIES_MICROPROFILE_GRAPHQL, new MicroProfileGraphQLClientDependencyProcessor());
                    final int POST_MODULE_MICROPROFILE_GRAPHQL = 14241;
                    processorTarget.addDeploymentProcessor(SUBSYSTEM_NAME, Phase.POST_MODULE, POST_MODULE_MICROPROFILE_GRAPHQL, new MicroProfileGraphQLClientDeploymentProcessor());
                }
            }, OperationContext.Stage.RUNTIME);

            MicroProfileGraphQLClientLogger.LOGGER.activatingSubsystem();
        }
    }
}
