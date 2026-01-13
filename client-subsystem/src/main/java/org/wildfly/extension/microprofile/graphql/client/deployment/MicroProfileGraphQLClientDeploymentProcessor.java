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

package org.wildfly.extension.microprofile.graphql.client.deployment;

import io.smallrye.graphql.client.modelbuilder.ClientModelBuilder;
import io.smallrye.graphql.client.model.ClientModels;
import org.jboss.as.controller.capability.CapabilityServiceSupport;
import org.jboss.as.ee.structure.DeploymentType;
import org.jboss.as.ee.structure.DeploymentTypeMarker;
import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.weld.WeldCapability;
import org.jboss.jandex.DotName;
import org.jboss.jandex.IndexView;
import org.wildfly.extension.microprofile.graphql.client.ClientModelsExtension;
import org.wildfly.extension.microprofile.graphql.client._private.MicroProfileGraphQLClientLogger;

import java.util.Collection;
import java.util.stream.Collectors;

public class MicroProfileGraphQLClientDeploymentProcessor implements DeploymentUnitProcessor {

    static final DotName GRAPHQL_CLIENT_API = DotName
            .createSimple("io.smallrye.graphql.client.typesafe.api.GraphQLClientApi");

    private static ClientModels clientModels;

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {

        final DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();
        if (!DeploymentTypeMarker.isType(DeploymentType.WAR, deploymentUnit)) {
            return;
        }
        // see whether this deployment contains GraphQL annotations
        final org.jboss.as.server.deployment.annotation.CompositeIndex compositeIndex = deploymentUnit.getAttachment(Attachments.COMPOSITE_ANNOTATION_INDEX);
        //  final IndexView index = deploymentUnit.getAttachment(Attachments.ANNOTATION_INDEX);
        if (compositeIndex.getAnnotations(GRAPHQL_CLIENT_API).isEmpty()) {
            return;
        }
        final CapabilityServiceSupport support = deploymentUnit.getAttachment(Attachments.CAPABILITY_SERVICE_SUPPORT);
        final WeldCapability weldCapability;
        MicroProfileGraphQLClientLogger.LOGGER.activatingGraphQLForDeployment(deploymentUnit.getName());
        Collection<IndexView> indexViews = compositeIndex.getIndexes().stream()
                .map(index -> (IndexView) index) // Assuming Index has an asIndexView() method
                .collect(Collectors.toList());

        try {
            weldCapability = support.getCapabilityRuntimeAPI(org.jboss.as.weld.Capabilities.WELD_CAPABILITY_NAME, WeldCapability.class);
        } catch (CapabilityServiceSupport.NoSuchCapabilityException e) {
            //We should not be here since the subsystem depends on weld capability. Just in case ...
            throw new RuntimeException("todo");
        }

        if (weldCapability.isPartOfWeldDeployment(deploymentUnit)) {
            final ClientModels clientModels = ClientModelBuilder.build(org.jboss.jandex.CompositeIndex.create(indexViews));
            weldCapability.registerExtensionInstance(new ClientModelsExtension(clientModels), deploymentUnit);
        }

    }
    @Override
    public void undeploy(DeploymentUnit deploymentUnit) {
    }
}
