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
import com.google.common.collect.Sets;
import org.obiba.magma.Datasource;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.support.AbstractDatasource;
import org.obiba.magma.support.StaticValueTable;
import org.obiba.opal.r.AbstractROperation;
import org.obiba.opal.r.DataReadROperation;
import org.obiba.opal.r.FileWriteROperation;
import org.obiba.opal.r.service.OpalRSession;
import org.springframework.transaction.support.TransactionTemplate;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.util.List;
import java.util.Set;

/**
 * A datasource based on tibble data frames identified by symbols in a R session. If a file path is provided (in opal
 * file system) it will be copied to the R session and the R haven package will be used to reate a tibble from it.
 */
public class RDatasource extends AbstractDatasource {

  private static final String DEFAULT_ENTITY_TYPE = "Participant";

  private static final String DEFAULT_ID_COLUMN = "entity_id";

  private final OpalRSession rSession;

  private TransactionTemplate txTemplate;

  private final File file;

  private List<File> outputFiles;

  private final File categoryFile;

  private final String symbol;

  private final String entityType;

  private final String idColumn;

  private String locale;

  /**
   * Empty datasource with files to be written to.
   *  @param name
   * @param rSession
   * @param txTemplate
   */
  public RDatasource(@NotNull String name, OpalRSession rSession, List<File> files, TransactionTemplate txTemplate) {
    this(name, rSession, null, null, null, null, null);
    this.outputFiles = files;
    this.txTemplate = txTemplate;
  }

  /**
   * Datasource based on a tibble named by the symbol.
   *
   * @param name
   * @param rSession
   * @param symbol
   * @param entityType
   * @param idColumn
   */
  public RDatasource(@NotNull String name, OpalRSession rSession, String symbol, String entityType, String idColumn) {
    this(name, rSession, null, null, symbol, entityType, idColumn);
  }

  /**
   * Datasource based on the provided data file(s) from which a tibble will be created.
   *
   * @param name
   * @param rSession
   * @param file
   * @param categoryFile
   * @param symbol
   * @param entityType
   * @param idColumn
   */
  public RDatasource(@NotNull String name, OpalRSession rSession, File file, File categoryFile, String symbol, String entityType, String idColumn) {
    super(name, "r");
    this.rSession = rSession;
    this.file = file;
    this.categoryFile = categoryFile;
    this.symbol = symbol;
    this.entityType = Strings.isNullOrEmpty(entityType) ? DEFAULT_ENTITY_TYPE : entityType;
    this.idColumn = idColumn;
    this.txTemplate = null;
  }

  @Override
  protected void onInitialise() {
    getRSession().execute(new AbstractROperation() {
      @Override
      protected void doWithConnection() {
        ensurePackage("tibble");
        eval("library(tibble)", false);
      }
    });
    // create tibble if file is provided
    if (file != null && file.exists()) {
      // copy file(s) to R session
      getRSession().execute(new FileWriteROperation(file.getName(), file));
      if (hasCategoryFile())
        getRSession().execute(new FileWriteROperation(categoryFile.getName(), categoryFile));
      // read it into the symbol
      getRSession().execute(new DataReadROperation(symbol, file.getName(), hasCategoryFile() ? categoryFile.getName() : null));
    }
  }

  OpalRSession getRSession() {
    return rSession;
  }

  @Override
  protected Set<String> getValueTableNames() {
    return Strings.isNullOrEmpty(symbol) ? Sets.newHashSet() : Sets.newHashSet(symbol.replaceAll(" ", "_"));
  }

  @Override
  protected ValueTable initialiseValueTable(String tableName) {
    return new RValueTable(this, tableName, symbol, entityType, idColumn);
  }

  @Override
  public ValueTableWriter createWriter(@NotNull String tableName, @NotNull String entityType) {
    File outputFile;
    if (outputFiles.size() == 1)
      outputFile = outputFiles.get(0);
    else
      outputFile = outputFiles.stream().filter(f -> f.getName().startsWith(tableName + ".")).findFirst().get();
    return new RValueTableWriter(tableName, entityType, outputFile, getRSession(), txTemplate);
  }

  private boolean hasCategoryFile() {
    return categoryFile != null;
  }

  public void setLocale(String locale) {
    this.locale = locale;
  }

  public String getLocale() {
    return Strings.isNullOrEmpty(locale) ? "en" : locale;
  }

}
