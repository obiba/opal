package org.obiba.opal.web.kubernetes;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import org.obiba.opal.core.cfg.PodsService;
import org.obiba.opal.core.domain.kubernetes.PodSpec;
import org.obiba.opal.web.model.K8S;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Path("/pod-specs")
@Tag(name = "Kubernetes", description = "Operations on registered pod specifications")
public class PodSpecsResource {

  private final PodsService podsService;

  @Autowired
  public PodSpecsResource(PodsService podsService) {
    this.podsService = podsService;
  }

  @GET
  @Operation(
    summary = "List pod specifications",
    description = "Retrieves a list of all registered pod specifications"
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Pod specifications list retrieved successfully"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public List<K8S.PodSpecDto> list() {
    return podsService.getSpecs().stream().map(Dtos::asDto).toList();
  }

  @POST
  @Operation(
    summary = "Create or update pod specification",
    description = "Creates a new pod specification or updates an existing one"
  )
  @ApiResponses({
    @ApiResponse(responseCode = "200", description = "Pod specification created or updated successfully"),
    @ApiResponse(responseCode = "400", description = "Invalid pod specification provided"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Response createOrUpdate(K8S.PodSpecDto dto) {
    PodSpec spec = Dtos.fromDto(dto);
    podsService.saveSpec(spec);
    return Response.ok().build();
  }

  @DELETE
  @Operation(
    summary = "Delete all pod specifications",
    description = "Removes all registered pod specifications"
  )
  @ApiResponses({
    @ApiResponse(responseCode = "204", description = "All pod specifications deleted successfully"),
    @ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public Response deleteAll() {
    podsService.deleteSpecs();
    return Response.noContent().build();
  }

}
