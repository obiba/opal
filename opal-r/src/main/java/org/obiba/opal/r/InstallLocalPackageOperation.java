/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.r;

import org.obiba.opal.spi.r.AbstractROperation;

import java.io.File;

public class InstallLocalPackageOperation extends AbstractROperation {

  private final File archiveFile;

  public InstallLocalPackageOperation(File archiveFile) {
    this.archiveFile = archiveFile;
  }

  @Override
  protected void doWithConnection() {
    writeFile(archiveFile.getName(), archiveFile);
    eval(String.format("install.packages('%s', repos = NULL, type ='source')", archiveFile.getName()));
  }
}
