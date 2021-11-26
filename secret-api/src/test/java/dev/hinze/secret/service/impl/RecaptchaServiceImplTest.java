package dev.hinze.secret.service.impl;

import dev.hinze.secret.client.google.RecaptchaClient;
import dev.hinze.secret.client.google.model.RecaptchaV2Response;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.quarkus.test.junit.mockito.InjectSpy;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.net.SocketAddress;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;



import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@QuarkusTest
public class RecaptchaServiceImplTest {

    @Mock
    HttpServerRequest httpServerRequest;

    @Mock
    SocketAddress socketAddress;

    @InjectMock
    @RestClient
    RecaptchaClient recaptchaClient;

    @InjectSpy
    RecaptchaServiceImpl recaptchaService;

    @BeforeEach
    public void before() {
        MockitoAnnotations.openMocks(this);
        Mockito.when(socketAddress.hostAddress())
                .thenReturn("255.255.255.255");
        Mockito.when(httpServerRequest.remoteAddress())
                .thenReturn(socketAddress);
    }

    @Test
    public void shouldReturnRecaptchaStatus() {
        recaptchaService.recaptchaSecret = Optional.of("secret");
        Mockito.when(recaptchaClient.verify(any(), any(), any()))
                .thenReturn(new RecaptchaV2Response().setSuccess(true));
        var success = recaptchaService.verify("foo", httpServerRequest);
        Assertions.assertTrue(success);
        Mockito.verify(recaptchaClient, Mockito.times(1))
                .verify(eq("secret"), eq("foo"), eq("255.255.255.255"));
    }

    @Test
    public void shouldReturnFalseWhenRecaptchaResponseNull() {
        recaptchaService.recaptchaSecret = Optional.of("secret");
        var success = recaptchaService.verify(null, httpServerRequest);
        Assertions.assertFalse(success);
        Mockito.verifyNoInteractions(recaptchaClient);
    }

    @Test
    public void shouldReturnFalseWhenRecaptchaResponseBlank() {
        recaptchaService.recaptchaSecret = Optional.of("secret");
        var success = recaptchaService.verify("", httpServerRequest);
        Assertions.assertFalse(success);
        Mockito.verifyNoInteractions(recaptchaClient);
    }

    @Test
    public void shouldReturnTrueWhenRecaptchaSecretNotPresent() {
        var success = recaptchaService.verify("", httpServerRequest);
        Assertions.assertTrue(success);
        Mockito.verifyNoInteractions(recaptchaClient);
    }

}
