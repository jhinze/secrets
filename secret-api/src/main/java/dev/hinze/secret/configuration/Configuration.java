package dev.hinze.secret.configuration;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.core.HazelcastInstance;
import io.quarkus.vertx.web.RouteFilter;
import io.vertx.ext.web.RoutingContext;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;


import java.util.Optional;

import static java.util.Objects.isNull;

@ApplicationScoped
@Slf4j
public class Configuration {

    @Produces
    HazelcastInstance createInstance(@ConfigProperty(name = "k8s.namespace") Optional<String> namespace,
                                     @ConfigProperty(name = "k8s.service") Optional<String> service,
                                     @ConfigProperty(name = "hz.address") Optional<String> address) {
        var clientConfig = new ClientConfig();
        if(namespace.isPresent() && service.isPresent()) {
            log.info("k8s namespace {}", namespace);
            log.info("k8s service {}", service);
            clientConfig.getNetworkConfig()
                    .getKubernetesConfig()
                    .setEnabled(true)
                    .setProperty("namespace", namespace.get())
                    .setProperty("service-name", service.get())
                    .setProperty("service-port", "5701");
        } else {
            log.info("Hazelcast address {}", address);
            clientConfig.getNetworkConfig().addAddress(address.orElse("127.0.0.1"));
        }
        return HazelcastClient.newHazelcastClient(clientConfig);
    }

    @RouteFilter(500) //top priority
    void reactRouterFilter(RoutingContext routingContext) {
        var uri = routingContext.request().uri();
        if(!uri.matches("\\/api\\/.*||\\/q\\/.*||\\/")) {
            var resource = this.getClass().getResource("/META-INF/resources" + uri);
            if(isNull(resource)) {
                log.debug("rerouting {} to /", uri);
                routingContext.reroute("/");
                return;
            }
        }
        routingContext.next();
    }

}
