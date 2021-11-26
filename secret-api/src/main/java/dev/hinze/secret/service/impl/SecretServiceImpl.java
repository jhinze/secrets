package dev.hinze.secret.service.impl;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import dev.hinze.secret.model.Secret;
import dev.hinze.secret.model.CreateSecretRequest;
import dev.hinze.secret.model.CreateSecretResponse;
import dev.hinze.secret.service.SecretService;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.UriInfo;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.isNull;

@ApplicationScoped
@Slf4j
public class SecretServiceImpl implements SecretService {

    private static final String SECRET_NOT_FOUND = "Secret not found";
    private static final String SECRET_MAP = "secret-map";

    @ConfigProperty(name = "secret.ttl-minutes", defaultValue = "1440")
    Long secretTimeToLiveMinutes;

    @ConfigProperty(name = "secret.max-length", defaultValue = "4096")
    Long secretMaxLength;

    @Inject
    HazelcastInstance hazelcastClient;


    @Override
    public CreateSecretResponse createSecret(CreateSecretRequest secretRequest, UriInfo uriInfo) {
        if(secretRequest.getSecret().length() > secretMaxLength)
            throw new BadRequestException("Secret too long");
        var secret = new Secret()
                .setId(UUID.randomUUID())
                .setSecret(secretRequest.getSecret());
        putSecret(secret);
        log.info("Created secret");
        return new CreateSecretResponse()
                .setSecret(secret)
                .setUrl(getSecretUrl(secret, uriInfo));
    }

    @Override
    public Secret getSecret(String uuidString) {
        var uuid = stringToUUID(uuidString);
        var secret = getSecretAndDelete(uuid);
        if(!isNull(secret)) {
            log.info("Delivered secret");
            return new Secret()
                    .setSecret(secret)
                    .setId(uuid);
        } else {
            throw new NotFoundException(SECRET_NOT_FOUND);
        }
    }

    private void putSecret(Secret secret) {
        getSecretMap().put(secret.getId(), secret.getSecret(), secretTimeToLiveMinutes, TimeUnit.MINUTES);
    }

    private String getSecretAndDelete(UUID uuid) {
        var secret = getSecretMap().get(uuid);
        if(!isNull(secret))
            getSecretMap().delete(uuid);
        return secret;
    }

    private IMap<UUID, String> getSecretMap() {
        return hazelcastClient.getMap(SECRET_MAP);
    }

    private UUID stringToUUID(String uuid) {
        try {
            return UUID.fromString(uuid);
        } catch (IllegalArgumentException e) {
            throw new NotFoundException(SECRET_NOT_FOUND);
        }
    }

    private String getSecretUrl(Secret secret, UriInfo uriInfo) {
        return uriInfo.getAbsolutePathBuilder()
                .scheme("https")
                .path(secret.getId().toString())
                .build()
                .toString();
    }

}
