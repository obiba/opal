/*
 * Copyright (c) 2018 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.r.magma;

import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.support.StaticDatasource;
import org.obiba.opal.r.magma.util.RCopyBufferSizeProvider;
import org.obiba.opal.spi.r.datasource.RSessionHandler;
import org.springframework.transaction.support.TransactionTemplate;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Writes a file through R using the haven package.
 */
public class RFileDatasource extends StaticDatasource {

  private final RSessionHandler sessionHandler;

  private final TransactionTemplate txTemplate;

  private final List<File> outputFiles;

  private final Map<String, String> idColumnNames;

  // default ID column name
  private final String idColumnName;

  private final RCopyBufferSizeProvider memoryRatioProvider;


  public RFileDatasource(String name, RSessionHandler sessionHandler, List<File> outputFiles, TransactionTemplate txTemplate,  String idColumnName, Map<String, String> idColumnNames, RCopyBufferSizeProvider memoryRatioProvider) {
    super(name);
    this.sessionHandler = sessionHandler;
    this.outputFiles = outputFiles;
    this.txTemplate = txTemplate;
    this.idColumnName = idColumnName;
    this.idColumnNames = idColumnNames;
    this.memoryRatioProvider = memoryRatioProvider;
  }

  @Override
  @NotNull
  public ValueTableWriter createWriter(@NotNull String tableName, @NotNull String entityType) {
    ValueTableWriter staticValueTableWriter = super.createWriter(tableName, entityType);
    return new RSymbolValueTableWriter(this, staticValueTableWriter, tableName, new RFileSymbolWriter(sessionHandler, outputFiles),
        sessionHandler, txTemplate, getIdColumnName(entityType), memoryRatioProvider);
  }

  private String getIdColumnName(String entityType) {
    return idColumnNames.getOrDefault(entityType, idColumnName);
  }
}
