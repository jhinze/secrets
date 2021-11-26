package dev.hinze.secret.client.google.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.OffsetDateTime;


@Data
@Accessors(chain = true)
public class RecaptchaV2Response {

    private boolean success;
    @JsonProperty("challenge_ts")
    private OffsetDateTime timestamp;
    private String hostname;
    @JsonProperty("error-codes")
    private String[] errorCodes;

}
