package org.obiba.opal.core.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.protobuf.util.JsonFormat;
import org.apache.commons.compress.utils.Lists;
import org.obiba.opal.core.domain.kubernetes.PodSpec;
import org.obiba.opal.web.kubernetes.Dtos;
import org.obiba.opal.web.model.K8S;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class RockPodSpecFactory {

  private static final Logger log = LoggerFactory.getLogger(RockPodSpecFactory.class);

  public static List<PodSpec> makeRockPodSpecs(String jsonArray) {
    log.info("Parsing pod specifications: {}", jsonArray);
    try {
      JsonArray array = JsonParser.parseString(jsonArray).getAsJsonArray();
      List<PodSpec> podSpecs = Lists.newArrayList();
      for (JsonElement element : array) {
        K8S.PodSpecDto.Builder builder = K8S.PodSpecDto.newBuilder();
        JsonFormat.parser().merge(element.toString(), builder);
        podSpecs.add(Dtos.fromDto(builder.build()));
      }
      return podSpecs.stream()
          .filter(Objects::nonNull)
          .filter(p -> p.getType().equals("rock"))
          .collect(Collectors.toList());
    }  catch (Exception e) {
      log.error("Cannot parse pod specifications: {}", jsonArray, e);
      return Lists.newArrayList();
    }
  }
}
