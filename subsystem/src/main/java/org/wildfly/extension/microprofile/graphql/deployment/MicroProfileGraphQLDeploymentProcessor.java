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

import io.smallrye.graphql.servlet.ExecutionServlet;
import io.smallrye.graphql.servlet.SchemaServlet;
import io.undertow.websockets.jsr.WebSocketDeploymentInfo;
import org.jboss.as.ee.structure.DeploymentType;
import org.jboss.as.ee.structure.DeploymentTypeMarker;
import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.deployment.annotation.CompositeIndex;
import org.jboss.as.web.common.WarMetaData;
import org.jboss.jandex.DotName;
import org.jboss.metadata.web.jboss.JBossServletMetaData;
import org.jboss.metadata.web.jboss.JBossServletsMetaData;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.metadata.web.spec.ListenerMetaData;
import org.jboss.metadata.web.spec.ServletMappingMetaData;
import org.wildfly.extension.microprofile.graphql.WildFlyGraphQLServerWebSocket;
import org.wildfly.extension.microprofile.graphql._private.MicroProfileGraphQLLogger;
import org.wildfly.extension.undertow.deployment.UndertowAttachments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MicroProfileGraphQLDeploymentProcessor implements DeploymentUnitProcessor {

    static final DotName ANNOTATION_GRAPHQL_API = DotName.createSimple("org.eclipse.microprofile.graphql.GraphQLApi");
    static final DotName ANNOTATION_SUBSCRIPTION = DotName.createSimple("io.smallrye.graphql.api.Subscription");
    static final DotName ANNOTATION_FEDERATION_EXTENDS = DotName.createSimple("io.smallrye.graphql.api.federation.Extends");
    static final DotName ANNOTATION_FEDERATION_EXTERNAL = DotName.createSimple("io.smallrye.graphql.api.federation.External");
    static final DotName ANNOTATION_FEDERATION_KEY = DotName.createSimple("io.smallrye.graphql.api.federation.Key");
    static final DotName ANNOTATION_FEDERATION_PROVIDES = DotName.createSimple("io.smallrye.graphql.api.federation.Provides");
    static final DotName ANNOTATION_FEDERATION_REQUIRES = DotName.createSimple("io.smallrye.graphql.api.federation.Requires");

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        final DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();
        if (!DeploymentTypeMarker.isType(DeploymentType.WAR, deploymentUnit)) {
            return;
        }

        // see whether this deployment contains GraphQL annotations
        final CompositeIndex compositeIndex = deploymentUnit.getAttachment(Attachments.COMPOSITE_ANNOTATION_INDEX);
        if (compositeIndex.getAnnotations(ANNOTATION_GRAPHQL_API).isEmpty()) {
            return;
        }
        MicroProfileGraphQLLogger.LOGGER.activatingGraphQLForDeployment(deploymentUnit.getName());

        // Scan for GraphQL Federation annotations. If found, automatically activate federation.
        if(hasFederationAnnotations(compositeIndex)) {
            System.setProperty("smallrye.graphql.federation.enabled", "true");
        }


        // steps needed for application initialization
        JBossWebMetaData mergedJBossWebMetaData = deploymentUnit.getAttachment(WarMetaData.ATTACHMENT_KEY).getMergedJBossWebMetaData();
        registerStartupListener(mergedJBossWebMetaData);
        registerExecutionServlet(mergedJBossWebMetaData);
        registerSchemaServlet(mergedJBossWebMetaData);

        // if the GraphQL API contains subscriptions, deploy the relevant web socket endpoint that handles them
        if (!compositeIndex.getAnnotations(ANNOTATION_SUBSCRIPTION).isEmpty()) {
            WebSocketDeploymentInfo webSocketDeploymentInfo = deploymentUnit.getAttachment(UndertowAttachments.WEB_SOCKET_DEPLOYMENT_INFO);
            webSocketDeploymentInfo.addEndpoint(WildFlyGraphQLServerWebSocket.class);
            mergedJBossWebMetaData.setEnableWebSockets(true);
        }

    }

    private boolean hasFederationAnnotations(CompositeIndex index) {
        return !index.getAnnotations(ANNOTATION_FEDERATION_EXTENDS).isEmpty() ||
            !index.getAnnotations(ANNOTATION_FEDERATION_EXTERNAL).isEmpty() ||
            !index.getAnnotations(ANNOTATION_FEDERATION_KEY).isEmpty() ||
            !index.getAnnotations(ANNOTATION_FEDERATION_PROVIDES).isEmpty() ||
            !index.getAnnotations(ANNOTATION_FEDERATION_REQUIRES).isEmpty();
    }


    // register the io.smallrye.graphql.servlet.StartupListener which needs to be called to initialize
    // the application
    private void registerStartupListener(JBossWebMetaData webdata) {
        ListenerMetaData startupListenerMetadata = new ListenerMetaData();
        startupListenerMetadata.setListenerClass("io.smallrye.graphql.servlet.StartupListener");
        List<ListenerMetaData> containerListeners = webdata.getListeners();
        if (containerListeners == null) {
            List<ListenerMetaData> list = new ArrayList<>();
            list.add(startupListenerMetadata);
            webdata.setListeners(list);
        } else {
            containerListeners.add(startupListenerMetadata);
        }
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
            mappings = new ArrayList<>();
            mappings.add(mapping);
            webdata.setServletMappings(mappings);
        }

    }

    private void registerSchemaServlet(JBossWebMetaData webdata) {
        JBossServletMetaData servlet = new JBossServletMetaData();
        servlet.setLoadOnStartup("2");
        servlet.setName("SmallRyeGraphQLSchemaServlet");
        servlet.setServletClass(SchemaServlet.class.getName());
        servlet.setAsyncSupported(false);
        webdata.getServlets().add(servlet);
        ServletMappingMetaData mapping = new ServletMappingMetaData();
        mapping.setServletName("SmallRyeGraphQLSchemaServlet");
        mapping.setUrlPatterns(Collections.singletonList("/graphql/schema.graphql"));    // TODO make configurable
        webdata.getServletMappings().add(mapping);
    }

    @Override
    public void undeploy(DeploymentUnit deploymentUnit) {
    }

}
