/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.cli.client.command;

import java.util.Set;

import org.obiba.core.util.StringUtil;
import org.obiba.opal.cli.client.command.options.JobRunningCommandOptions;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.converter.DefaultJobParametersConverter;
import org.springframework.batch.core.launch.JobInstanceAlreadyExistsException;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.launch.JobParametersNotFoundException;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.batch.core.launch.NoSuchJobExecutionException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.batch.support.PropertiesConverter;

public class JobRunningCommand<T extends JobRunningCommandOptions> extends AbstractContextLoadingCommand<T> {

  @Override
  public void executeWithContext() {
    // Ensure that options have been set.
    if(options == null) {
      throw new IllegalStateException("Options not set (setOptions must be called before calling execute)");
    }

    JobOperator jobOperator = getBean("jobOperator");
    JobRepository jobRepository = getBean("jobRepository");

    if(options.isListJobs()) {
      Set<String> jobs = jobOperator.getJobNames();
      for(String jobName : jobs) {
        System.console().format("Job %s\n", jobName);
      }
    } else if(options.isRun()) {
      String jobName = options.getRun();
      try {

        if(options.isNext()) {
          jobOperator.startNextInstance(jobName);
        } else if(options.isForce()) {
          String parameters = StringUtil.collectionToString(options.getJobParameters(), ",");
          JobParameters jobParameters = new DefaultJobParametersConverter().getJobParameters(PropertiesConverter.stringToProperties(parameters));
          JobExecution je = jobRepository.getLastJobExecution(jobName, jobParameters);
          jobOperator.restart(je.getId());
        } else {
          String parameters = StringUtil.collectionToString(options.getJobParameters(), ",");
          jobOperator.start(jobName, parameters);
        }
      } catch(JobInstanceAlreadyCompleteException e) {
        System.console().printf("Job %s as already been run successfully with the specified parameters. It cannot be restarted.\n", jobName);
      } catch(NoSuchJobExecutionException e) {
        System.console().printf("Job %s was never run with the specified parameters. It cannot be restarted.\n", jobName);
      } catch(NoSuchJobException e) {
        System.console().printf("Job %s not found.\n", jobName);
      } catch(JobRestartException e) {
        System.console().printf("Job %s cannot be restart.\n", jobName);
      } catch(JobInstanceAlreadyExistsException e) {
        System.console().printf("Job %s as already been run with the specified parameters. To run this job again, its parameters must be different.\n", jobName);
      } catch(JobParametersNotFoundException e) {
        System.console().printf("Cannot determine parameters for job %s.\n", jobName);
      } catch(JobExecutionAlreadyRunningException e) {
        System.console().printf("Job %s is already running.\n", jobName);
      }
    }
  }
}
