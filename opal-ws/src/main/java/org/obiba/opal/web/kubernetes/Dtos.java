package org.obiba.opal.web.kubernetes;

import org.obiba.opal.core.domain.kubernetes.Container;
import org.obiba.opal.core.domain.kubernetes.PodSpec;
import org.obiba.opal.web.model.K8S;

public class Dtos {

  public static K8S.PodSpecDto asDto(PodSpec spec) {
    return K8S.PodSpecDto.newBuilder()
        .setId(spec.getId())
        .setType(spec.getType())
        .setDescription(spec.getDescription())
        .setNamespace(spec.getNamespace())
        .setContainer(asDto(spec.getContainer()))
        .build();
  }

  private static K8S.ContainerDto asDto(Container container) {
    return K8S.ContainerDto.newBuilder()
        .setName(container.getName())
        .setImage(container.getImage())
        .setPort(container.getPort())
        .putAllEnv(container.getEnv())
        .setResources(asDto(container.getResources()))
        .build();
  }

  private static K8S.ResourceRequirementsDto asDto(Container.ResourceRequirements req) {
    return K8S.ResourceRequirementsDto.newBuilder()
        .setLimits(asDto(req.getLimits()))
        .setRequests(asDto(req.getRequests()))
        .build();
  }

  private static K8S.ResourceListDto asDto(Container.ResourceList list) {
    return K8S.ResourceListDto.newBuilder()
        .setCpu(list.getCpu())
        .setMemory(list.getMemory())
        .build();
  }

  public static PodSpec fromDto(K8S.PodSpecDto dto) {
    return new PodSpec()
        .setId(dto.getId())
        .setType(dto.getType())
        .setNamespace(dto.getNamespace())
        .setDescription(dto.getDescription())
        .setContainer(fromDto(dto.getContainer()));
  }

  private static Container fromDto(K8S.ContainerDto dto) {
    return new Container()
        .setName(dto.getName())
        .setImage(dto.getImage())
        .setPort(dto.getPort())
        .setEnv(dto.getEnvMap())
        .setResources(fromDto(dto.getResources()));
  }

  private static Container.ResourceRequirements fromDto(K8S.ResourceRequirementsDto dto) {
    return new Container.ResourceRequirements()
        .setLimits(fromDto(dto.getLimits()))
        .setRequests(fromDto(dto.getRequests()));
  }

  private static Container.ResourceList fromDto(K8S.ResourceListDto dto) {
    return new Container.ResourceList()
        .setCpu(dto.getCpu())
        .setMemory(dto.getMemory());
  }

}
