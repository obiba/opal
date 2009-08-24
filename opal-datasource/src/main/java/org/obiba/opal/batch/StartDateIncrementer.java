package org.obiba.opal.batch;

import java.util.Map;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersIncrementer;
import org.springframework.batch.core.repository.JobRepository;

public class StartDateIncrementer implements JobParametersIncrementer {
  //
  // Constants
  //

  public static final String START_DATE_KEY = "input.startDate";

  //
  // Instance Variables
  //

  private JobRepository jobRepository;

  private String jobName;

  //
  // JobParametersIncrementer Methods
  //

  public JobParameters getNext(JobParameters parameters) {
    JobExecution lastJobExecution = jobRepository.getLastJobExecution(jobName, parameters);
    if(lastJobExecution != null && lastJobExecution.getStartTime() != null) {
      Map<String, JobParameter> parameterMap = parameters.getParameters();
      JobParameter startDate = new JobParameter(lastJobExecution.getStartTime());
      parameterMap.put(START_DATE_KEY, startDate);
      return new JobParameters(parameterMap);
    }

    return parameters;
  }

  //
  // Methods
  //

  public void setJobRepository(JobRepository jobRepository) {
    this.jobRepository = jobRepository;
  }

  public void setJobName(String jobName) {
    this.jobName = jobName;
  }
}
