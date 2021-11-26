package dev.hinze.secret.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.UUID;

@Data
@Accessors(chain = true)
public class Secret implements Serializable {

    private UUID id;
    private String secret;

}
