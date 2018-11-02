package org.obiba.opal.r.magma;

import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.support.StaticDatasource;
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


  public RFileDatasource(String name, RSessionHandler sessionHandler, List<File> outputFiles, TransactionTemplate txTemplate,  String idColumnName, Map<String, String> idColumnNames) {
    super(name);
    this.sessionHandler = sessionHandler;
    this.outputFiles = outputFiles;
    this.txTemplate = txTemplate;
    this.idColumnName = idColumnName;
    this.idColumnNames = idColumnNames;
  }

  @Override
  @NotNull
  public ValueTableWriter createWriter(@NotNull String tableName, @NotNull String entityType) {
    ValueTableWriter staticValueTableWriter = super.createWriter(tableName, entityType);
    return new RSymbolValueTableWriter(this, staticValueTableWriter, tableName, new RFileSymbolWriter(sessionHandler, outputFiles),
        sessionHandler, txTemplate, getIdColumnName(entityType));
  }

  private String getIdColumnName(String entityType) {
    return idColumnNames.getOrDefault(entityType, idColumnName);
  }
}
