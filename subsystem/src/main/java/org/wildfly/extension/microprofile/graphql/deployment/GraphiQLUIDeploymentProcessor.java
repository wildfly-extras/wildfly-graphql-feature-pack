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

import org.jboss.as.ee.component.EEModuleDescription;
import org.jboss.as.ee.structure.DeploymentType;
import org.jboss.as.ee.structure.DeploymentTypeMarker;
import org.jboss.as.server.deployment.Attachments;
import org.jboss.as.server.deployment.DeploymentPhaseContext;
import org.jboss.as.server.deployment.DeploymentUnit;
import org.jboss.as.server.deployment.DeploymentUnitProcessingException;
import org.jboss.as.server.deployment.DeploymentUnitProcessor;
import org.jboss.as.server.deployment.module.ResourceRoot;
import org.jboss.as.web.common.WarMetaData;
import org.jboss.metadata.web.jboss.JBossWebMetaData;
import org.jboss.vfs.VFS;
import org.jboss.vfs.VirtualFile;
import org.wildfly.extension.microprofile.graphql._private.MicroProfileGraphQLLogger;

import java.io.Closeable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * This processor locates the META-INF/resources/graphql-ui/render.js file in GraphQL deployments that
 * contain Graphiql UI (as a library jar) and updates them on-the-fly to contain the correct path to the right /graphql endpoint,
 * that is including context root of the application that is being deployed.
 *
 * FIXME: this assumes that graphql-ui jar is part of the WAR deployment. A better solution
 * could be to distribute it with WildFly as a static module and somehow append it to GraphQL deployments.
 */
public class GraphiQLUIDeploymentProcessor implements DeploymentUnitProcessor {

    private Closeable mountedOverlay;

    @Override
    public void deploy(DeploymentPhaseContext phaseContext) throws DeploymentUnitProcessingException {
        final DeploymentUnit deploymentUnit = phaseContext.getDeploymentUnit();
        if (!DeploymentTypeMarker.isType(DeploymentType.WAR, deploymentUnit)) {
            return;
        }
        List<ResourceRoot> rootList = deploymentUnit.getAttachmentList(Attachments.RESOURCE_ROOTS);
        try {
            for (ResourceRoot resourceRoot : rootList) {
                // see if there is a file named META-INF/resources/graphql-ui/render.js
                VirtualFile renderJsFile = resourceRoot.getRoot().getChild("META-INF/resources/graphql-ui/render.js");
                if (renderJsFile != null && renderJsFile.isFile()) {
                    // change the render.js by updating the /graphql path to the actual path
                    String ctxRoot = determineContextRoot(deploymentUnit,
                            deploymentUnit.getAttachment(WarMetaData.ATTACHMENT_KEY).getMergedJBossWebMetaData());
                    Path originalPath = renderJsFile.getPhysicalFile().toPath();
                    Path pathToUpdatedRenderJs = Files.createTempFile(null, "render.js");
                    String graphQlPath = ctxRoot.endsWith("/") ? ctxRoot + "graphql" : ctxRoot + "/graphql";
                    updateApiUrl(originalPath, pathToUpdatedRenderJs,  graphQlPath);
                    mountedOverlay = VFS.mountReal(pathToUpdatedRenderJs.toFile(), renderJsFile);
                }
            }
        } catch (Exception e) {
            throw new DeploymentUnitProcessingException(e);
        }
    }

    // TODO: this must be available somehow else, I can't believe I have to determine the context root in such complicated way
    static String determineContextRoot(final DeploymentUnit deploymentUnit, final JBossWebMetaData metaData) {
        if (metaData.getContextRoot() == null) {
            final EEModuleDescription description = deploymentUnit.getAttachment(org.jboss.as.ee.component.Attachments.EE_MODULE_DESCRIPTION);
            if (description != null) {
                String moduleName = description.getModuleName();
                return "/" + (moduleName.equals("ROOT") ? "" : moduleName);
            } else {
                if(deploymentUnit.getName().equals("ROOT.war")) {
                    return "/";
                }
                return "/" + deploymentUnit.getName().substring(0, deploymentUnit.getName().length() - 4);
            }
        } else {
            String pathName = metaData.getContextRoot();
            if (pathName.length() > 0 && pathName.charAt(0) != '/') {
                return "/" + pathName;
            } else {
                return pathName;
            }
        }
    }

    private void updateApiUrl(Path renderJs, Path targetFile, String graphqlPath) throws IOException {
        String content = new String(Files.readAllBytes(renderJs), StandardCharsets.UTF_8);
        String result = updateApiUrl(content, graphqlPath);
        if (result.equals(content)) {
            MicroProfileGraphQLLogger.LOGGER.couldNotUpdateRenderJs(renderJs.toString());
        }
        Files.write(targetFile, result.getBytes(StandardCharsets.UTF_8));
    }

    public String updateApiUrl(String original, String graphqlPath) {
        return original.replace("const api = '/graphql';", "const api = '" + graphqlPath + "';");
    }

    @Override
    public void undeploy(DeploymentUnit context) {
        if (mountedOverlay != null) {
            try {
                mountedOverlay.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
