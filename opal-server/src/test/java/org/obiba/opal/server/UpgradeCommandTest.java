/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.server;

import org.junit.Test;

import java.io.File;
import java.nio.file.Files;

public class UpgradeCommandTest {

  @Test
  public void test_new_installation_passes() throws Exception {
    UpgradeCommand.checkConfigurationMigrated(Files.createTempDirectory("opal").resolve("opal-config.xml").toFile());
  }

  @Test
  public void test_6_x_passes() throws Exception {
    UpgradeCommand.checkConfigurationMigrated(config(6));
  }

  @Test(expected = IllegalStateException.class)
  public void test_5_x_stops() throws Exception {
    UpgradeCommand.checkConfigurationMigrated(config(5));
  }

  private File config(int major) throws Exception {
    File file = Files.createTempFile("opal-config", ".xml").toFile();
    Files.writeString(file.toPath(), "<org.obiba.opal.core.cfg.OpalConfiguration><version><major>" + major +
        "</major><minor>2</minor><micro>0</micro></version></org.obiba.opal.core.cfg.OpalConfiguration>");
    return file;
  }
}
