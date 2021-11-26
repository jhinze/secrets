package dev.hinze.secret.service;

import io.vertx.core.http.HttpServerRequest;

public interface RecaptchaService {

    boolean verify(String response, HttpServerRequest request);

}
