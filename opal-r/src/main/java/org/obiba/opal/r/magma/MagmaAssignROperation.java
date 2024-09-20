/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.r.magma;

import com.google.common.base.Strings;
import org.obiba.magma.ValueTable;
import org.obiba.opal.core.service.DataExportService;
import org.obiba.opal.core.service.IdentifiersTableService;
import org.obiba.opal.r.service.RCacheHelper;
import org.obiba.opal.spi.r.AbstractROperation;
import org.obiba.opal.spi.r.RRuntimeException;
import org.obiba.opal.spi.r.RServerConnection;
import org.obiba.opal.spi.r.RServerException;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Assign Magma values (from a table or a variable) to a R symbol.
 */
public class MagmaAssignROperation extends AbstractROperation {

  public enum RClass {
    DATA_FRAME, DATA_FRAME_NO_FACTORS, TIBBLE, TIBBLE_WITH_FACTORS, VECTOR
  }

  private static final String DEFAULT_ID_COLUMN = "_id";

  @NotNull
  private final IdentifiersTableService identifiersTableService;

  @NotNull
  private final DataExportService dataExportService;

  @NotNull
  private final String symbol;

  @NotNull
  private final String path;

  private final ValueTable valueTable;

  private final String variableFilter;

  private final boolean withMissings;

  private final String identifiersMapping;

  private final String idColumnName;

  private final RClass rClass;

  private final RCacheHelper rCacheHelper;

  public MagmaAssignROperation(@NotNull String symbol, @NotNull ValueTable valueTable, @NotNull DataExportService dataExportService, RCacheHelper rCacheHelper, String idColumnName) {
    this(symbol, valueTable, dataExportService, rCacheHelper, idColumnName, RClass.TIBBLE);
  }

  public MagmaAssignROperation(@NotNull String symbol, @NotNull ValueTable valueTable, @NotNull DataExportService dataExportService, RCacheHelper rCacheHelper, String idColumnName, RClass rClass) {
    this.symbol = symbol;
    this.path = "";
    this.valueTable = valueTable;
    this.variableFilter = "";
    this.withMissings = true;
    this.identifiersMapping = "";
    this.identifiersTableService = null;
    this.dataExportService = dataExportService;
    this.idColumnName = idColumnName;
    this.rClass = rClass;
    this.rCacheHelper = rCacheHelper;
  }

  public MagmaAssignROperation(@NotNull String symbol, @NotNull String path, String variableFilter,
                               boolean withMissings, String idColumnName, String identifiersMapping, RClass rClass,
                               @NotNull IdentifiersTableService identifiersTableService,
                               @NotNull DataExportService dataExportService,
                               @NotNull RCacheHelper rCacheHelper) {
    this.symbol = symbol;
    this.path = path;
    this.valueTable = null;
    this.variableFilter = variableFilter;
    this.withMissings = withMissings;
    this.identifiersMapping = identifiersMapping;
    this.idColumnName = idColumnName;
    this.identifiersTableService = identifiersTableService;
    this.dataExportService = dataExportService;
    this.rClass = path.contains(":") ? RClass.VECTOR : RClass.VECTOR.equals(rClass) ? RClass.DATA_FRAME : rClass;
    this.rCacheHelper = rCacheHelper;
  }

  @Override
  public void doWithConnection() {
    MagmaRConverter converter;
    switch (rClass) {
      case VECTOR:
      case DATA_FRAME:
        converter = new ValueTableDataFrameRConverter(this);
        break;
      case DATA_FRAME_NO_FACTORS:
        converter = new ValueTableDataFrameRConverter(this, false, false);
        break;
      case TIBBLE_WITH_FACTORS:
        converter = new ValueTableDataFrameRConverter(this, true);
        break;
      default:
        converter = new ValueTableTibbleRConverter(this);
    }

    converter.doAssign(symbol, path);
  }

  public RCacheHelper getRCacheHelper() {
    return rCacheHelper;
  }

  RServerConnection getRConnection() {
    return getConnection();
  }

  void doEval(String script) {
    eval(script, false);
  }

  void doReadFile(String fileName, File destination) {
    try {
      readFile(fileName, destination);
    } catch (RServerException e) {
      throw new RRuntimeException(e);
    }
  }

  void doReadFile(String fileName, OutputStream out) {
    readFile(fileName, out);
  }

  void doWriteFile(String fileName, InputStream in) {
    try {
      writeFile(fileName, in);
    } catch (RServerException e) {
      throw new RRuntimeException(e);
    }
  }

  IdentifiersTableService getIdentifiersTableService() {
    return identifiersTableService;
  }

  DataExportService getDataExportService() {
    return this.dataExportService;
  }

  String getSymbol() {
    return symbol;
  }

  boolean hasValueTable() {
    return valueTable != null;
  }

  ValueTable getValueTable() {
    return valueTable;
  }

  String getIdColumnName() {
    // we need one for tibble assignment and will be removed in the case of a data frame
    return withIdColumn() ? idColumnName : DEFAULT_ID_COLUMN;
  }

  boolean withIdColumn() {
    return !Strings.isNullOrEmpty(idColumnName);
  }

  boolean withMissings() {
    return withMissings;
  }

  String getIdentifiersMapping() {
    return identifiersMapping;
  }

  boolean hasVariableFilter() {
    return !Strings.isNullOrEmpty(variableFilter);
  }

  String getVariableFilter() {
    return variableFilter;
  }

  @Override
  public String toString() {
    return symbol + " <- opal[" + path + "]";
  }

}
