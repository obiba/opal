package org.obiba.opal.core.service;

import org.obiba.opal.core.domain.VCFSamplesMapping;

import javax.validation.ConstraintViolationException;
import javax.validation.constraints.NotNull;
import java.util.List;

public interface VCFSamplesMappingService extends SystemService {

  Iterable<VCFSamplesMapping> getVCFSamplesMappings();

  @NotNull
  VCFSamplesMapping getVCFSamplesMapping(@NotNull String name) throws NoSuchProjectException;

  boolean hasVCFSamplesMapping(@NotNull String name);

  void save(@NotNull VCFSamplesMapping project) throws ConstraintViolationException;

  void delete(@NotNull String name) throws NoSuchVCFSamplesMappingException;

  List<String> getFilteredSampleIds(@NotNull String projectName, @NotNull String filteringTable, boolean withControl);
}