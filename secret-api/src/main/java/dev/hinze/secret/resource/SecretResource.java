package dev.hinze.secret.resource;

import dev.hinze.secret.model.Secret;
import dev.hinze.secret.model.CreateSecretResponse;
import dev.hinze.secret.model.CreateSecretRequest;
import dev.hinze.secret.service.RecaptchaService;
import dev.hinze.secret.service.SecretService;
import io.vertx.core.http.HttpServerRequest;
import org.jboss.resteasy.annotations.jaxrs.QueryParam;

import javax.inject.Inject;
import javax.validation.Valid;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

@Path("/secret")
public class SecretResource {

    @Inject
    SecretService secretService;

    @Inject
    RecaptchaService recaptchaService;

    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Secret getSecret(@PathParam("id") String id) {
        return secretService.getSecret(id);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public CreateSecretResponse postSecret(@Valid CreateSecretRequest createSecretRequest,
                                           @QueryParam String recaptcha,
                                           @Context UriInfo uriInfo,
                                           @Context HttpServerRequest request) {
        if(recaptchaService.verify(recaptcha, request))
            return secretService.createSecret(createSecretRequest, uriInfo);
        else
            throw new ForbiddenException("ReCAPTCHA failed");
    }

}