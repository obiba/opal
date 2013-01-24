/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.support;

import java.util.ArrayList;
import java.util.Collection;

import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 *
 */
public class TimedExecution {

  private long start;

  private long executionTime;

  public TimedExecution start() {
    start = System.currentTimeMillis();
    return this;
  }

  public TimedExecution end() {
    executionTime = System.currentTimeMillis() - start;
    return this;
  }

  public String formatExecutionTime() {
    long hours = MILLISECONDS.toHours(executionTime);
    long minutes = MILLISECONDS.toMinutes(executionTime) - HOURS.toMinutes(hours);
    long seconds = MILLISECONDS.toSeconds(executionTime) - HOURS.toSeconds(hours) - MINUTES.toSeconds(minutes);
    long millis = executionTime - HOURS.toMillis(hours) - MINUTES.toMillis(minutes) - SECONDS.toMillis(seconds);

    Collection<Object> args = new ArrayList<Object>();
    StringBuilder format = new StringBuilder();
    if(hours > 0) {
      format.append("%d hours ");
      args.add(hours);
    }
    if(minutes > 0) {
      format.append("%d min ");
      args.add(minutes);
    }
    if(seconds > 0) {
      format.append("%d sec ");
      args.add(seconds);
    }
    format.append("%d ms");
    args.add(millis);
    return String.format(format.toString(), args.toArray(new Object[args.size()]));
  }

}
