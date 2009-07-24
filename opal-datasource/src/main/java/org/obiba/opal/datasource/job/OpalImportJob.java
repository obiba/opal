package org.obiba.opal.datasource.job;

import java.util.Set;

import org.springframework.batch.core.Job;

public interface OpalImportJob extends Job {

  public Set<ImportJobParameter> getJobParameters();

}
