/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.runtime.upgrade;

import java.io.File;
import java.io.IOException;

import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.AbstractUpgradeStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

/**
 * Create custom-context.xml file if it does not exist in ${OPAL_HOME}/conf
 */
public class CreateMissingCustomContextXml extends AbstractUpgradeStep {

  private final Logger log = LoggerFactory.getLogger(getClass());

  @Value("${OPAL_HOME}/conf/custom-context.xml")
  private String filePath;

  @Override
  public void execute(Version currentVersion) {
    File file = new File(filePath);
    if(file.exists()) return;

    log.info("Create missing config file " + filePath);
    try {
      Files.write(
          "<beans xmlns=\"http://www.springframework.org/schema/beans\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
              "       xsi:schemaLocation=\"http://www.springframework.org/schema/beans http://www.springframework.org/schema/beans/spring-beans-3.0.xsd\">\n\n" +
              "  <!--Uncomment this to use Atlassian Crowd Realms-->\n" +
              "  <!--<import resource=\"file:${OPAL_HOME}/conf/crowd/crowd-context.xml\" />-->\n\n" +
              "  <!-- Intentionally left empty. This is meant to be overridden by custom configuration if required. -->\n\n" +
              "</beans>", file, Charsets.UTF_8);
    } catch(IOException e) {
      throw new RuntimeException("Error while creating missing config file " + filePath);
    }
  }

  public void setFilePath(String filePath) {
    this.filePath = filePath;
  }
}
