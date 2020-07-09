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

import io.smallrye.graphql.schema.SchemaBuilder;
import io.smallrye.graphql.schema.model.Schema;
import io.smallrye.graphql.servlet.ExecutionServlet;
import org.jboss.as.controller.capability.CapabilityServiceSupport;
import org.jboss.as.ee.structure.DeploymentType;
import org.jboss.as.ee.structure.DeploymentTypeMarker;
import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.deployment.annotation.CompositeIndex;
import org.jboss.as.web.common.WarMetaData;
import org.jboss.as.weld.WeldCapability;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.jboss.metadata.web.jboss.JBossServletMetaData;
import org.jboss.metadata.web.jboss.JBossServletsMetaData;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.metadata.web.spec.ServletMappingMetaData;
import org.wildfly.extension.microprofile.graphql._private.MicroProfileGraphQLLogger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.jboss.as.weld.Capabilities.WELD_CAPABILITY_NAME;

public class MicroProfileGraphQLDeploymentProcessor implements DeploymentUnitProcessor {

    static final DotName ANNOTATION_GRAPHQL_API = DotName.createSimple("org.eclipse.microprofile.graphql.GraphQLApi");

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        final DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();
        if (!DeploymentTypeMarker.isType(DeploymentType.WAR, deploymentUnit)) {
            return;
        }

        // see whether this deployment contains GraphQL
        final CompositeIndex compositeIndex = deploymentUnit.getAttachment(Attachments.COMPOSITE_ANNOTATION_INDEX);
        Index usedIndex = locateIndexToUseForSchema(compositeIndex);
        if (usedIndex == null) {
            return;
        }
        MicroProfileGraphQLLogger.LOGGER.activatingGraphQLForDeployment(deploymentUnit.getName());

        // compute the schema and initialize the GraphQLProducer bean
        Schema schema = SchemaBuilder.build(usedIndex);
        initializeGraphQLProducer(deploymentUnit, schema);
        MicroProfileGraphQLLogger.LOGGER.foundOperations(schema.getQueries().size(),
                schema.getMutations().size());

        // add execution and schema servlets
        registerExecutionServlet(deploymentUnit.getAttachment(WarMetaData.ATTACHMENT_KEY).getMergedJBossWebMetaData());
        // TODO: schema servlet
    }

    private void initializeGraphQLProducer(DeploymentUnit deploymentUnit, Schema schema) throws DeploymentUnitProcessingException {
        final CapabilityServiceSupport support = deploymentUnit.getAttachment(Attachments.CAPABILITY_SERVICE_SUPPORT);
        final WeldCapability weldCapability;
        try {
            weldCapability = support.getCapabilityRuntimeAPI(WELD_CAPABILITY_NAME, WeldCapability.class);
            if (weldCapability.isPartOfWeldDeployment(deploymentUnit)) {
                weldCapability.registerExtensionInstance(new GraphQLProducerInitializationExtension(schema), deploymentUnit);
            } else {
                throw new DeploymentUnitProcessingException("blabla");
            }
        } catch (CapabilityServiceSupport.NoSuchCapabilityException e) {
            throw new DeploymentUnitProcessingException(e);
        }
    }

    private Index locateIndexToUseForSchema(CompositeIndex compositeIndex) throws DeploymentUnitProcessingException {
        List<AnnotationInstance> graphQlApis = new ArrayList<>();
        Index usedIndex = null;
        for (Index index : compositeIndex.getIndexes()) {
            List<AnnotationInstance> annotations = index.getAnnotations(ANNOTATION_GRAPHQL_API);
            if (!annotations.isEmpty()) {
                graphQlApis.addAll(annotations);
                usedIndex = index;
            }
        }
        if (graphQlApis.size() == 0) {
            return null;
        }
        if (graphQlApis.size() > 1) {
            throw MicroProfileGraphQLLogger.LOGGER.multipleGraphQLApiAnnotations();
        }
        return usedIndex;
    }

    private void registerExecutionServlet(JBossWebMetaData webdata) {
        JBossServletMetaData servlet = new JBossServletMetaData();
        servlet.setLoadOnStartup("1");
        servlet.setName("SmallRyeGraphQLExecutionServlet");
        servlet.setServletClass(ExecutionServlet.class.getName());
        servlet.setAsyncSupported(false);

        if (webdata.getServlets() == null) {
            webdata.setServlets(new JBossServletsMetaData());
        }
        webdata.getServlets().add(servlet);
        ServletMappingMetaData mapping = new ServletMappingMetaData();
        mapping.setServletName("SmallRyeGraphQLExecutionServlet");
        mapping.setUrlPatterns(Collections.singletonList("/graphql"));  // TODO make configurable
        List<ServletMappingMetaData> mappings = webdata.getServletMappings();
        if (mappings != null) {
            mappings.add(mapping);
        } else {
            webdata.setServletMappings(Collections.singletonList(mapping));
        }

    }

    @Override
    public void undeploy(DeploymentUnit deploymentUnit) {
    }

}
