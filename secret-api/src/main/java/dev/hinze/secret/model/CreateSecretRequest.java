package dev.hinze.secret.model;

import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;


@Data
@Accessors(chain = true)
public class CreateSecretRequest {

    @NotBlank
    private String secret;

}
