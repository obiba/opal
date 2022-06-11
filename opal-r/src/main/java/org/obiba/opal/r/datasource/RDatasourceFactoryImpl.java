/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.r.datasource;

import com.google.common.base.Strings;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.obiba.magma.Datasource;
import org.obiba.opal.core.runtime.OpalFileSystemService;
import org.obiba.opal.r.DataReadROperation;
import org.obiba.opal.spi.r.datasource.AbstractRDatasourceFactory;
import org.obiba.opal.spi.r.datasource.magma.RDatasource;

import java.io.File;

/**
 * Factory of {@link RDatasource}, either based on a symbol to be found in a R session or on file
 * to be read into a symbol in a R session.
 */
public class RDatasourceFactoryImpl extends AbstractRDatasourceFactory {

  private OpalFileSystemService opalFileSystemService;

  private String symbol;

  private String entityType;

  private String idColumn;

  private String characterSet;

  private String locale;

  private boolean multilines;

  private String file;

  private String categoryFile;

  public void setOpalFileSystemService(OpalFileSystemService opalFileSystemService) {
    this.opalFileSystemService = opalFileSystemService;
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

  /**
   * SAS specifies categories (formats) outside of the main data file.
   *
   * @param categoryFile
   */
  public void setCategoryFile(String categoryFile) {
    this.categoryFile = categoryFile;
  }

  @Override
  protected Datasource internalCreate() {
    RDatasource ds;

    if (Strings.isNullOrEmpty(file))
      ds = new RDatasource(getName(), getRSessionHandler(), symbol, entityType, idColumn);
    else
      try {
        FileObject fileObj = resolveFileInFileSystem(file);
        if (!fileObj.exists() || !fileObj.isReadable())
          throw new IllegalArgumentException("File does not exist or cannot be read: " + file);

        FileObject catFileObj;
        if (Strings.isNullOrEmpty(categoryFile) && file.endsWith(".sas7bdat")) {
          // try to guess a category file
          catFileObj = resolveFileInFileSystem(file.replaceAll("\\.sas7bdat$", ".sas7bcat"));
        } else {
          catFileObj = resolveFileInFileSystem(categoryFile);
        }

        File file = opalFileSystemService.getFileSystem().getLocalFile(fileObj);
        File categoryFile =
            catFileObj != null && catFileObj.exists() && catFileObj.isReadable() ? opalFileSystemService.getFileSystem().getLocalFile(catFileObj) : null;

        // create tibble if file is provided
        if (file != null && file.exists()) {
          // copy file(s) to R session
          prepareFile(file);
          prepareFile(categoryFile);
          // read it into the symbol
          execute(new DataReadROperation(symbol, file.getName(), categoryFile != null ? categoryFile.getName() : null));
        }

        ds = new RDatasource(getName(), getRSessionHandler(), symbol, entityType, idColumn);
      } catch (FileSystemException e) {
        throw new IllegalArgumentException("Failed resolving file path: " + file);
      }

    ds.setLocale(locale);

    return ds;
  }

  FileObject resolveFileInFileSystem(String path) throws FileSystemException {
    if (Strings.isNullOrEmpty(path)) return null;
    return opalFileSystemService.getFileSystem().getRoot().resolveFile(path);
  }
}
