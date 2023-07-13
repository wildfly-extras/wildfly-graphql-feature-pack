package org.wildfly.extras.graphql.feature.pack.test.layers.metadata;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.wildfly.glow.Arguments;
import org.wildfly.glow.GlowMessageWriter;
import org.wildfly.glow.GlowSession;
import org.wildfly.glow.ScanResults;
import org.wildfly.glow.maven.MavenResolver;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@GraphQLApi
public class GraphqlLayersMetadataTestCase {

    private static String URL_PROPERTY = "wildfly-glow-galleon-feature-packs-url";
    private static Path ARCHIVES_PATH = Paths.get("target/glow-archives");

    @BeforeClass
    public static void prepareArchivesDirectory() throws Exception {
        Path glowXmlPath = Paths.get("target/test-classes/glow");
        System.out.println(glowXmlPath.toUri());
        System.setProperty(URL_PROPERTY, glowXmlPath.toUri().toString());
        if (Files.exists(ARCHIVES_PATH)) {
            Files.walkFileTree(ARCHIVES_PATH, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        }
        Files.createDirectories(ARCHIVES_PATH);
    }


    private static Path createWebArchive(String archiveName) {
        WebArchive war = ShrinkWrap.create(WebArchive.class);
        war.addClass(GraphqlLayersMetadataTestCase.class);
        ZipExporter exporter = war.as(ZipExporter.class);
        Path path = ARCHIVES_PATH.resolve(archiveName);
        exporter.exportTo(path.toFile());
        return path;
    }

    @Test
    public void testGraphqlDetected() throws Exception {
        Path p = createWebArchive("test.war");

        Arguments arguments = Arguments.scanBuilder().setBinaries(Collections.singletonList(p)).build();
        ScanResults scanResults = GlowSession.scan(MavenResolver.newMavenResolver(), arguments, GlowMessageWriter.DEFAULT);
        Set<String> foundLayers = scanResults.getDiscoveredLayers().stream().map(l -> l.getName()).collect(Collectors.toSet());
        Assert.assertTrue(foundLayers.contains("microprofile-graphql"));

    }
}
