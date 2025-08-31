package org.obiba.opal.web.kubernetes;

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
public class PodSpecResource {

  @PathParam("id")
  private String id;

  private final PodsService podsService;

  @Autowired
  public PodSpecResource(PodsService podsService) {
    this.podsService = podsService;
  }

  @GET
  public K8S.PodSpecDto get() {
    return Dtos.asDto(podsService.getSpec(id));
  }

  @DELETE
  public Response delete() {
    podsService.deleteSpec(id);
    return Response.noContent().build();
  }

  @GET
  @Path("/pods")
  public List<K8S.PodRefDto> getPods() {
    return podsService.getPods(podsService.getSpec(id)).stream().map(Dtos::asDto).toList();
  }

  @DELETE
  @Path("/pods")
  public Response deletePods() {
    podsService.deletePods(podsService.getSpec(id));
    return Response.noContent().build();
  }

  @DELETE
  @Path("/pod/{name}")
  public Response deletePod(@PathParam("name") String name) {
    PodRef ref = podsService.getPod(podsService.getSpec(id), name);
    if (ref != null) podsService.deletePod(ref);
    return Response.noContent().build();
  }
}
