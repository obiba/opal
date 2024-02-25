/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.spi.r.datasource.magma;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.obiba.magma.ValueTable;
import org.obiba.magma.support.AbstractDatasource;
import org.obiba.opal.spi.r.AbstractROperation;
import org.obiba.opal.spi.r.ROperationTemplate;
import org.obiba.opal.spi.r.datasource.RSessionHandler;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A datasource based on tibble data frames identified by symbols in a R session.
 */
public class RDatasource extends AbstractDatasource {

  private static final String DEFAULT_ENTITY_TYPE = "Participant";

  public static final String DEFAULT_ID_COLUMN_NAME = "id";

  private final RSessionHandler rSessionHandler;

  private List<File> outputFiles;

  private final Map<String, String> tableSymbolNames = Maps.newHashMap();

  private final String entityType;

  private final String idColumnName;

  private String locale;

  /**
   * Datasource based on a tibble named by the symbol.
   *
   * @param name
   * @param rSessionHandler
   * @param symbol
   * @param entityType
   * @param idColumnName
   */
  public RDatasource(@NotNull String name, RSessionHandler rSessionHandler, String symbol, String entityType, String idColumnName) {
    this(name, rSessionHandler, Lists.newArrayList(symbol), entityType, idColumnName);
  }

  public RDatasource(@NotNull String name, RSessionHandler rSessionHandler, List<String> symbols, String entityType, String idColumnName) {
    super(name, "r");
    this.rSessionHandler = rSessionHandler;
    if (symbols != null && !symbols.isEmpty())
      symbols.forEach(symbol -> tableSymbolNames.put(symbol, symbol));
    this.entityType = Strings.isNullOrEmpty(entityType) ? DEFAULT_ENTITY_TYPE : entityType;
    this.idColumnName = idColumnName;
  }

  @Override
  protected void onInitialise() {
    getRSession().execute(new AbstractROperation() {
      @Override
      protected void doWithConnection() {
        ensurePackage("tibble");
        ensurePackage("dplyr");
      }
    });
  }

  ROperationTemplate getRSession() {
    return rSessionHandler.getSession();
  }

  @Override
  protected Set<String> getValueTableNames() {
    return tableSymbolNames.isEmpty() ? Sets.newHashSet() : tableSymbolNames.keySet();
  }

  @Override
  protected ValueTable initialiseValueTable(String tableName) {
    return new RValueTable(this, tableName, tableSymbolNames.get(tableName), entityType, idColumnName);
  }

  @Override
  protected void onDispose() {
    super.onDispose();
    rSessionHandler.onDispose();
  }

  public void setLocale(String locale) {
    this.locale = locale;
  }

  public String getLocale() {
    return Strings.isNullOrEmpty(locale) ? "en" : locale;
  }

}
