package dev.hinze.secret.client.google;

import dev.hinze.secret.client.google.model.RecaptchaV2Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.annotations.jaxrs.QueryParam;

import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

@Path("/siteverify")
@ApplicationScoped
@RegisterRestClient(configKey="recaptcha-api")
public interface RecaptchaClient {

    @POST
    RecaptchaV2Response verify(@QueryParam String secret, @QueryParam String response, @QueryParam String remoteip);

}
