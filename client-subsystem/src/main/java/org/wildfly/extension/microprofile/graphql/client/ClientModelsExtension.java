package org.wildfly.extension.microprofile.graphql.client;

import io.smallrye.graphql.client.model.ClientModels;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Default;
import jakarta.enterprise.inject.spi.AfterBeanDiscovery;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.Extension;
import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Singleton;

/**
 * CDI extension that registers the {@link ClientModels} instance computed by the deployment processor as a singleton bean.
 * This allows the {@link ClientModels} instance to be injected into other CDI beans.
 *
 * @author mskacelik
 */
public class ClientModelsExtension implements Extension {

    private final ClientModels clientModels;

    public ClientModelsExtension() {
        this(null);
    }

    public ClientModelsExtension(ClientModels clientModels) {
        this.clientModels = clientModels;
    }

    void registerClientModelsBean(@Observes AfterBeanDiscovery afterBeanDiscoveryEvent,
                                  BeanManager beanManager) {
        afterBeanDiscoveryEvent.addBean()
                .types(ClientModels.class)
                .qualifiers(new AnnotationLiteral<Default>() {}, new AnnotationLiteral<Any>() {})
                .scope(Singleton.class)
                .beanClass(ClientModels.class)
                .createWith(creationalContext -> clientModels);
        }
}
