package dev.hinze.secret.service;

import dev.hinze.secret.model.CreateSecretRequest;
import dev.hinze.secret.model.CreateSecretResponse;
import dev.hinze.secret.model.Secret;

import javax.ws.rs.core.UriInfo;

public interface SecretService {

    CreateSecretResponse createSecret(CreateSecretRequest secretRequest, UriInfo uriInfo);

    Secret getSecret(String uuidString);

}
