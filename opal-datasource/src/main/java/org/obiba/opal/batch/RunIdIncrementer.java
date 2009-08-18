/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.batch;

import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersIncrementer;

/**
 *
 */
public class RunIdIncrementer implements JobParametersIncrementer {

  public static final String RUN_ID_KEY = "run.id";

  public JobParameters getNext(JobParameters parameters) {
    if(parameters == null || parameters.isEmpty()) {
      return new JobParametersBuilder().addLong(RUN_ID_KEY, 1L).toJobParameters();
    }
    long id = parameters.getLong(RUN_ID_KEY, 1L) + 1;
    return new JobParametersBuilder().addLong(RUN_ID_KEY, id).toJobParameters();
  }

}
