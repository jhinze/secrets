package dev.hinze.secret.service.impl;

import dev.hinze.secret.client.google.RecaptchaClient;
import dev.hinze.secret.service.RecaptchaService;
import io.vertx.core.http.HttpServerRequest;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;


import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.util.Optional;

import static java.util.Objects.isNull;

@ApplicationScoped
@Slf4j
public class RecaptchaServiceImpl implements RecaptchaService {

    @ConfigProperty(name = "recaptcha.secret")
    Optional<String> recaptchaSecret;

    @Inject
    @RestClient
    RecaptchaClient recaptchaClient;

    @Override
    public boolean verify(String response, HttpServerRequest request) {
        if(recaptchaSecret.isPresent()) {
            if(!isNull(response) && !response.isBlank()) {
                var recaptchaResponse = recaptchaClient.verify(
                        recaptchaSecret.get(), response, request.remoteAddress().hostAddress()
                );
                log.info("ReCAPTCHA success {}", recaptchaResponse.isSuccess());
                return recaptchaResponse.isSuccess();
            } else {
                log.info("ReCAPTCHA Response null or blank");
                return false;
            }
        } else {
            log.info("Skipping ReCAPTCHA verification");
            return true;
        }
    }


}
