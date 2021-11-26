package dev.hinze.secret.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.util.UUID;

@Data
@Accessors(chain = true)
public class CreateSecretResponse {

    private Secret secret;
    private String url;

}
