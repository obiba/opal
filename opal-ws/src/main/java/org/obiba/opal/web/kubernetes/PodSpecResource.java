package org.obiba.opal.web.kubernetes;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Response;
import org.obiba.opal.core.cfg.PodsService;
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
}
