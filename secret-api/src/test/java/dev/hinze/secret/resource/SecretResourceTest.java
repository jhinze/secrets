package dev.hinze.secret.resource;

import dev.hinze.secret.model.CreateSecretRequest;
import dev.hinze.secret.model.CreateSecretResponse;
import dev.hinze.secret.model.Secret;
import dev.hinze.secret.service.RecaptchaService;
import dev.hinze.secret.service.SecretService;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.restassured.http.ContentType;
import io.vertx.core.http.HttpServerRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import javax.ws.rs.core.UriInfo;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

@QuarkusTest
public class SecretResourceTest {

    @InjectMock
    RecaptchaService recaptchaService;

    @InjectMock
    SecretService secretService;


    @Test
    public void shouldGetSecret() {
        var uuid = UUID.randomUUID();
        Mockito.when(secretService.getSecret(eq(uuid.toString())))
                .thenReturn(new Secret().setSecret("abc").setId(uuid));
        given().when()
                .get("/api/secret/" + uuid.toString())
                .then()
                .statusCode(200)
                .body(is("{\"id\":\"" + uuid.toString() + "\",\"secret\":\"abc\"}"));
    }

    @Test
    public void shouldPostSecret() {
        var uuid = UUID.randomUUID();
        var createSecretResponse = new CreateSecretResponse()
                .setUrl("http://foo")
                .setSecret(new Secret().setSecret("abc").setId(uuid));
        Mockito.when(secretService.createSecret(any(CreateSecretRequest.class), any(UriInfo.class)))
                .thenReturn(createSecretResponse);
        Mockito.when(recaptchaService.verify(any(), any(HttpServerRequest.class)))
                .thenReturn(true);
        given().when()
                .body(new CreateSecretRequest().setSecret("abc"))
                .contentType(ContentType.JSON)
                .post("/api/secret")
                .then()
                .statusCode(200)
                .body(is("{\"secret\":{\"id\":\"" + uuid.toString() + "\",\"secret\":\"abc\"},\"url\":\"http://foo\"}"));
    }

    @Test
    public void shouldReturnForbiddenWhenRecaptchaFails() {
        Mockito.when(recaptchaService.verify(any(), any(HttpServerRequest.class)))
                .thenReturn(false);
        given().when()
                .body(new CreateSecretRequest().setSecret("abc"))
                .contentType(ContentType.JSON)
                .post("/api/secret")
                .then()
                .statusCode(403);
    }

}
