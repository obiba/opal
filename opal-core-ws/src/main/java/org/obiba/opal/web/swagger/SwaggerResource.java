package org.obiba.opal.web.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.servers.Server;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Component
public class SwaggerResource extends io.swagger.v3.jaxrs2.integration.resources.OpenApiResource {

  @GET
  @Produces("application/json")
  @Override
  public Response getOpenApi(@Context HttpHeaders headers, @Context UriInfo uriInfo, @PathParam("type") String type) throws Exception {
    // Parse the OpenAPI string to an OpenAPI object
    String openAPIStr = super.getOpenApi(headers, uriInfo, type).readEntity(String.class);
    OpenAPI openAPI = io.swagger.v3.core.util.Json.mapper().readValue(openAPIStr, OpenAPI.class);

    // Add server information explicitly, otherwise the /ws prefix is missing in the generated spec
    URI baseUri = uriInfo.getBaseUri();
    List<Server> servers = new ArrayList<>();
    Server server = new Server();
    server.setUrl(baseUri.toString());
    server.setDescription("OPAL API Server");
    servers.add(server);
    openAPI.setServers(servers);

    // Dump the modified OpenAPI object back to a string
    openAPIStr = io.swagger.v3.core.util.Json.mapper().writeValueAsString(openAPI);
    return Response.ok(openAPIStr).build();
  }
}