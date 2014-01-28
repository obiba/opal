/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.reporting.service.r;

import java.io.File;
import java.util.Map;

import org.obiba.opal.core.cfg.OpalConfigurationExtension;
import org.obiba.opal.core.runtime.NoSuchServiceConfigurationException;
import org.obiba.opal.reporting.service.ReportException;
import org.obiba.opal.reporting.service.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;

@Component
public class RReportServiceImpl implements ReportService {

  private static final Logger log = LoggerFactory.getLogger(RReportServiceImpl.class);

  @Value("${org.obiba.opal.R.exec}")
  private String exec;

  @Override
  public void render(String format, Map<String, String> parameters, String reportDesign, String reportOutput)
      throws ReportException {
    log.info("{} -e 'in={} out={}'", exec, reportDesign, reportOutput);
  }

  @Override
  public boolean isRunning() {
    return isEnabled();
  }

  @Override
  public void start() {
    log.info(isEnabled() ? "R report service started." : "R report service unavailable (R executable or opal R package is missing)");
  }

  @Override
  public void stop() {
  }

  @Override
  public String getName() {
    return "rreport";
  }

  @Override
  public OpalConfigurationExtension getConfig() throws NoSuchServiceConfigurationException {
    throw new NoSuchServiceConfigurationException(getName());
  }

  public boolean isEnabled() {
    return !Strings.isNullOrEmpty(exec) && new File(exec).exists() && isOpalPackageInstalled();
  }

  private boolean isOpalPackageInstalled() {
    return true;
  }
}
