package org.obiba.opal.web.kubernetes;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.obiba.opal.core.cfg.PodsService;
import org.obiba.opal.core.domain.kubernetes.PodRef;
import org.obiba.opal.core.domain.kubernetes.PodSpec;
import org.obiba.opal.web.model.K8S;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Scope("request")
@Path("/pod-spec/{id}")
@Tag(name = "Kubernetes", description = "Operations on registered pod specifications")
public class PodSpecResource {

  @PathParam("id")
  private String id;

  private final PodsService podsService;

  @Autowired
  public PodSpecResource(PodsService podsService) {
    this.podsService = podsService;
  }

  @GET
  @Operation(
    summary = "Get pod specification",
    description = "Retrieves detailed information about a specific pod specification"
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Pod specification retrieved successfully"),
    @ApiResponse(responseCode = "404", description = "Pod specification not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public K8S.PodSpecDto get() {
    return Dtos.asDto(podsService.getSpec(id));
  }

  @DELETE
  @Operation(
    summary = "Delete pod specification",
    description = "Removes a specific pod specification"
  )
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Pod specification deleted successfully"),
    @ApiResponse(responseCode = "404", description = "Pod specification not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Response delete() {
    podsService.deleteSpec(id);
    return Response.noContent().build();
  }

  @GET
  @Path("/pods")
  @Operation(
    summary = "Get pods for specification",
    description = "Retrieves a list of pods associated with a specific pod specification"
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Pods list retrieved successfully"),
    @ApiResponse(responseCode = "404", description = "Pod specification not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public List<K8S.PodRefDto> getPods() {
    return podsService.getPods(podsService.getSpec(id)).stream().map(Dtos::asDto).toList();
  }

  @DELETE
  @Path("/pods")
  @Operation(
    summary = "Delete all pods for specification",
    description = "Removes all pods associated with a specific pod specification"
  )
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "All pods deleted successfully"),
    @ApiResponse(responseCode = "404", description = "Pod specification not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Response deletePods() {
    podsService.deletePods(podsService.getSpec(id));
    return Response.noContent().build();
  }

  @DELETE
  @Path("/pod/{name}")
  @Operation(
    summary = "Delete specific pod",
    description = "Removes a specific pod by name from a pod specification"
  )
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "Pod deleted successfully"),
    @ApiResponse(responseCode = "404", description = "Pod specification or pod not found"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Response deletePod(@PathParam("name") String name) {
    PodRef ref = podsService.getPod(podsService.getSpec(id), name);
    if (ref != null) podsService.deletePod(ref);
    return Response.noContent().build();
  }
}
