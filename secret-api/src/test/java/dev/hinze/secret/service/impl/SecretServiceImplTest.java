package dev.hinze.secret.service.impl;

import com.hazelcast.config.Config;
import com.hazelcast.instance.impl.DefaultNodeContext;
import com.hazelcast.instance.impl.HazelcastInstanceImpl;
import com.hazelcast.map.IMap;
import dev.hinze.secret.model.CreateSecretRequest;
import dev.hinze.secret.model.Secret;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.InjectMock;
import io.quarkus.test.junit.mockito.InjectSpy;
import org.apache.commons.lang3.RandomStringUtils;
import org.jboss.resteasy.specimpl.ResteasyUriInfo;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


@QuarkusTest
public class SecretServiceImplTest {

    @Alternative
    @Priority(1)
    @ApplicationScoped
    public static class MockableHazelcast extends HazelcastInstanceImpl {

        public MockableHazelcast() {
            super("mock", new Config("mock"), new DefaultNodeContext());
        }

    }

    @InjectMock
    MockableHazelcast hazelcastInstance;
    @InjectSpy
    SecretServiceImpl secretService;

    private UriInfo uriInfo;
    private IMap<Object, Object> distributedMap;

    @BeforeEach
    public void before() {
        MockitoAnnotations.openMocks(this);
        distributedMap = Mockito.mock(IMap.class);
        when(hazelcastInstance.getMap(eq("secret-map")))
                .thenReturn(distributedMap);
        uriInfo = new ResteasyUriInfo(URI.create("https://hinze.dev"));
    }

    @Test
    public void shouldCreateSecret() {
        var request = new CreateSecretRequest().setSecret("secret");
        var response = secretService.createSecret(request, uriInfo);
        verify(distributedMap, Mockito.times(1))
                .put(
                        eq(response.getSecret().getId()),
                        eq(request.getSecret()),
                        eq(secretService.secretTimeToLiveMinutes),
                        eq(TimeUnit.MINUTES)
                );
        Assertions.assertNotNull(response.getSecret().getId());
        Assertions.assertEquals(request.getSecret(), response.getSecret().getSecret());
    }

    @Test
    public void shouldGetAndDeleteSecret() {
        var secretId = UUID.randomUUID();
        var secret = new Secret().setSecret("secret").setId(secretId);
        when(distributedMap.get(eq(secretId)))
                .thenReturn(secret.getSecret());
        var response = secretService.getSecret(secretId.toString());
        Assertions.assertEquals(response, secret);
        verify(distributedMap, times(1))
                .get(eq(secretId));
        verify(distributedMap, times(1))
                .delete(eq(secretId));
    }

    @Test
    public void shouldThrowBadRequestWhenSecretTooLong() {
        Assertions.assertThrows(BadRequestException.class, () -> {
            var request = new CreateSecretRequest()
                    .setSecret(RandomStringUtils.random(secretService.secretMaxLength.intValue() + 1));
            secretService.createSecret(request, uriInfo);
        });
    }

    @Test
    public void shouldThrowNotFoundWhenSecretNotInMap() {
        Assertions.assertThrows(NotFoundException.class, () -> {
            var secretId = UUID.randomUUID();
            when(distributedMap.get(eq(secretId)))
                    .thenReturn(null);
            secretService.getSecret(secretId.toString());
        });
    }

    @Test
    public void shouldThrowNotFoundWhenUUIDMalformed() {
        Assertions.assertThrows(NotFoundException.class, () -> {
            secretService.getSecret("Not-A-UUID");
        });
    }

}
