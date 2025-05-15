package org.obiba.opal.web.kubernetes;

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
public class PodSpecsResource {

  private final PodsService podsService;

  @Autowired
  public PodSpecsResource(PodsService podsService) {
    this.podsService = podsService;
  }

  @GET
  public List<K8S.PodSpecDto> list() {
    return podsService.getSpecs().stream().map(Dtos::asDto).toList();
  }

  @POST
  public Response createOrUpdate(K8S.PodSpecDto dto) {
    PodSpec spec = Dtos.fromDto(dto);
    podsService.saveSpec(spec);
    return Response.ok().build();
  }

  @DELETE
  public Response deleteAll() {
    podsService.deleteSpecs();
    return Response.noContent().build();
  }

}
