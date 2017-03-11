package org.obiba.opal.core.service;

import org.obiba.opal.core.domain.VCFSamplesMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.validation.ConstraintViolationException;
import javax.validation.constraints.NotNull;


@Component
public class VCFSamplesMappingServiceImpl implements VCFSamplesMappingService {

  private static final Logger log = LoggerFactory.getLogger(VCFSamplesMappingServiceImpl.class);

  @Autowired
  private OrientDbService orientDbService;


  @Override
  @PostConstruct
  public void start() {
    orientDbService.createUniqueIndex(VCFSamplesMapping.class);
  }

  @Override
  public void stop() {
  }

  @Override
  public Iterable<VCFSamplesMapping> getVCFSamplesMappings() {
    return orientDbService.list(VCFSamplesMapping.class);
  }

  @Override
  public VCFSamplesMapping getVCFSamplesMapping(@NotNull String projectName) throws NoSuchProjectException {
    VCFSamplesMapping vcfSamplesMapping = orientDbService.findUnique(new VCFSamplesMapping(projectName));
    if(vcfSamplesMapping == null) throw new NoSuchVCFSamplesMappingException(projectName);
    return vcfSamplesMapping;
  }

  @Override
  public boolean hasVCFSamplesMapping(@NotNull String projectName) {
    try {
      getVCFSamplesMapping(projectName);
      return true;
    } catch(NoSuchProjectException e) {
      return false;
    }
  }

  @Override
  public void save(@NotNull VCFSamplesMapping vcfSamplesMapping) throws ConstraintViolationException {
    orientDbService.save(vcfSamplesMapping, vcfSamplesMapping);
  }

  @Override
  public void delete(@NotNull String projectName) throws NoSuchVCFSamplesMappingException {
    VCFSamplesMapping vcfSamplesMapping = getVCFSamplesMapping(projectName);
    orientDbService.delete(vcfSamplesMapping, vcfSamplesMapping);
  }

}
