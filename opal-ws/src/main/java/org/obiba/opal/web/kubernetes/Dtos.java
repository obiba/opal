package org.obiba.opal.web.kubernetes;

import org.obiba.opal.core.domain.kubernetes.Container;
import org.obiba.opal.core.domain.kubernetes.PodSpec;
import org.obiba.opal.core.domain.kubernetes.Toleration;
import org.obiba.opal.web.model.K8S;

import java.util.stream.Collectors;

public class Dtos {

  public static K8S.PodSpecDto asDto(PodSpec spec) {
    K8S.PodSpecDto.Builder builder = K8S.PodSpecDto.newBuilder()
        .setId(spec.getId())
        .setType(spec.getType())
        .setDescription(spec.getDescription())
        .setNamespace(spec.getNamespace())
        .putAllLabels(spec.getLabels())
        .putAllNodeSelector(spec.getNodeSelector())
        .addAllTolerations(spec.getTolerations().stream().map(Dtos::asDto).toList())
        .setEnabled(spec.isEnabled())
        .setContainer(asDto(spec.getContainer()));

    if (spec.hasNodeName()) builder.setNodeName(spec.getNodeName());

    return builder.build();
  }

  private static K8S.TolerationDto asDto(Toleration tol) {
    K8S.TolerationDto.Builder builder = K8S.TolerationDto.newBuilder()
        .setOperator(tol.getOperator().name())
        .setEffect(tol.getEffect().name());

    if (tol.hasKey()) builder.setKey(tol.getKey());
    if (tol.hasValue()) builder.setValue(tol.getValue());
    if (tol.hasTolerationSeconds()) builder.setTolerationSeconds(tol.getTolerationSeconds());

    return builder.build();
  }

  private static K8S.ContainerDto asDto(Container container) {
    K8S.ContainerDto.Builder builder = K8S.ContainerDto.newBuilder()
        .setName(container.getName())
        .setImage(container.getImage())
        .setImagePullPolicy(container.getImagePullPolicy())
        .setPort(container.getPort())
        .putAllEnv(container.getEnv())
        .setResources(asDto(container.getResources()));

    if (container.hasImagePullSecret())
        builder.setImagePullSecret(container.getImagePullSecret());

    return builder.build();
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
        .setLabels(dto.getLabelsMap())
        .setNodeName(dto.getNodeName())
        .setNodeSelector(dto.getNodeSelectorMap())
        .setDescription(dto.getDescription())
        .setEnabled(dto.getEnabled())
        .setTolerations(dto.getTolerationsList().stream().map(Dtos::fromDto).collect(Collectors.toList()))
        .setContainer(fromDto(dto.getContainer()));
  }

  private static Toleration fromDto(K8S.TolerationDto dto) {
    return new Toleration()
        .setKey(dto.getKey())
        .setOperator(dto.getOperator())
        .setEffect(dto.getEffect())
        .setValue(dto.getValue())
        .setTolerationSeconds(dto.getTolerationSeconds());
  }

  private static Container fromDto(K8S.ContainerDto dto) {
    return new Container()
        .setName(dto.getName())
        .setImage(dto.getImage())
        .setImagePullPolicy(dto.getImagePullPolicy())
        .setImagePullSecret(dto.getImagePullSecret())
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
