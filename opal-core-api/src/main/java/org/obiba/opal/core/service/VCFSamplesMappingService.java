package org.obiba.opal.core.service;

import org.obiba.opal.core.domain.VCFSamplesMapping;

import javax.validation.ConstraintViolationException;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface VCFSamplesMappingService extends SystemService {

  String TABLE_ENTITY_TYPE = "Sample";

  Iterable<VCFSamplesMapping> getVCFSamplesMappings();

  @NotNull
  VCFSamplesMapping getVCFSamplesMapping(@NotNull String name) throws NoSuchProjectException;

  boolean hasVCFSamplesMapping(@NotNull String name);

  void save(@NotNull VCFSamplesMapping project) throws ConstraintViolationException;

  void delete(@NotNull String name) throws NoSuchVCFSamplesMappingException;

  void deleteProjectSampleMappings(@NotNull String project);

  List<String> getFilteredSampleIds(@NotNull String projectName, @NotNull String filteringTable, boolean withControl);

  List<String> getControls(@NotNull String projectName);

  Map<String, String> findParticipantIdBySampleId(@NotNull String projectName, @NotNull Collection<String> samplesIds);
}
