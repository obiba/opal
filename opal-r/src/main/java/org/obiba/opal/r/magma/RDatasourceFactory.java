/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.r.magma;

import com.google.common.base.Strings;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.obiba.magma.AbstractDatasourceFactory;
import org.obiba.magma.Datasource;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.r.service.OpalRSession;
import org.obiba.opal.r.service.OpalRSessionManager;

/**
 * Factory of {@link RDatasource}, either based on a symbol to be found in a R session or on file
 * to be read into a symbol in a R session.
 */
public class RDatasourceFactory extends AbstractDatasourceFactory {

  private OpalRSessionManager opalRSessionManager;

  private OpalRuntime opalRuntime;

  private String rSessionId;

  private String symbol;

  private String entityType;

  private String idColumn;

  private String characterSet;

  private String locale;

  private boolean multilines;

  private String file;

  public void setOpalRSessionManager(OpalRSessionManager opalRSessionManager) {
    this.opalRSessionManager = opalRSessionManager;
  }

  public void setOpalRuntime(OpalRuntime opalRuntime) {
    this.opalRuntime = opalRuntime;
  }

  public void setRSessionId(String rSessionId) {
    this.rSessionId = rSessionId;
  }

  public void setSymbol(String symbol) {
    this.symbol = symbol;
  }

  public void setEntityType(String entityType) {
    this.entityType = entityType;
  }

  public void setIdColumn(String idColumn) {
    this.idColumn = idColumn;
  }

  public void setCharacterSet(String characterSet) {
    this.characterSet = characterSet;
  }

  public void setLocale(String locale) {
    this.locale = locale;
  }

  public void setMultilines(boolean multilines) {
    this.multilines = multilines;
  }

  public void setFile(String file) {
    this.file = file;
  }

  @Override
  protected Datasource internalCreate() {
    final OpalRSession rSession = Strings.isNullOrEmpty(rSessionId) ?
        opalRSessionManager.newSubjectRSession() : opalRSessionManager.getSubjectRSession(rSessionId);

    if(Strings.isNullOrEmpty(rSessionId)) rSession.setExecutionContext("Import");

    if (Strings.isNullOrEmpty(file)) return new RDatasource(getName(), rSession, symbol, entityType, idColumn);

    try {
      FileObject fileObj = resolveFileInFileSystem(file);
      if (!fileObj.exists() || !fileObj.isReadable()) throw new IllegalArgumentException("File does not exist or cannot be read: " + file);
      return new RDatasource(getName(), rSession, opalRuntime.getFileSystem().getLocalFile(fileObj), symbol, entityType, idColumn) {
        @Override
        protected void onDispose() {
          opalRSessionManager.removeRSession(rSession.getId());
        }
      };
    } catch (FileSystemException e) {
      throw new IllegalArgumentException("Failed resolving file path: " + file);
    }
  }

  FileObject resolveFileInFileSystem(String path) throws FileSystemException {
    return opalRuntime.getFileSystem().getRoot().resolveFile(path);
  }

}
