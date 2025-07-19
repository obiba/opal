package org.obiba.opal.core.service;

import com.google.common.base.Strings;
import org.obiba.opal.core.domain.kubernetes.Container;
import org.obiba.opal.core.domain.kubernetes.PodSpec;

public class RockPodSpecFactory {

  public static PodSpec makeRockPodSpec(String name, String image,  String cpu, String memory) {
    return new PodSpec(name)
        .setType("rock")
        .setContainer(new Container()
            .setName("default")
            .setImage(image)
            .setPort(8085)
            .setResources(new Container.ResourceRequirements()
                .setRequests(new Container.ResourceList()
                    .setCpu(Strings.isNullOrEmpty(cpu) ? "1000m" : cpu)
                    .setMemory(Strings.isNullOrEmpty(memory) ? "500Mi" : memory))
                .setLimits(new Container.ResourceList()
                    .setCpu(Strings.isNullOrEmpty(cpu) ? "1000m" : cpu)
                    .setMemory(Strings.isNullOrEmpty(memory) ? "1Gi" : memory))));
  }
}
