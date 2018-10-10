package org.obiba.opal.r.magma;

import com.google.common.collect.Sets;
import org.obiba.magma.NoSuchValueTableException;
import org.obiba.magma.ValueTable;
import org.obiba.magma.ValueTableWriter;
import org.obiba.magma.support.AbstractDatasource;
import org.obiba.opal.spi.r.datasource.RSessionHandler;
import org.springframework.transaction.support.TransactionTemplate;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Writes a file through R.
 */
public class RFileDatasource extends AbstractDatasource {

  private final RSessionHandler sessionHandler;

  private final TransactionTemplate txTemplate;

  private final List<File> outputFiles;

  private final Map<String, String> idColumnNames;

  // default ID column name
  private final String idColumnName;


  public RFileDatasource(String name, RSessionHandler sessionHandler, List<File> outputFiles, TransactionTemplate txTemplate,  String idColumnName, Map<String, String> idColumnNames) {
    super(name, "r");
    this.sessionHandler = sessionHandler;
    this.outputFiles = outputFiles;
    this.txTemplate = txTemplate;
    this.idColumnName = idColumnName;
    this.idColumnNames = idColumnNames;
  }

  @Override
  public ValueTableWriter createWriter(@NotNull String tableName, @NotNull String entityType) {
    File outputFile;
    if (outputFiles.size() == 1)
      outputFile = outputFiles.get(0);
    else
      outputFile = outputFiles.stream().filter(f -> f.getName().startsWith(tableName + ".")).findFirst().get();
    return new RFileValueTableWriter(tableName, entityType, outputFile, sessionHandler.getSession(), txTemplate, getIdColumnName(entityType));
  }

  @Override
  protected Set<String> getValueTableNames() {
    return Sets.newHashSet();
  }

  @Override
  protected ValueTable initialiseValueTable(String tableName) {
    // not supposed to be here
    throw new NoSuchValueTableException(tableName);
  }

  private String getIdColumnName(String entityType) {
    return idColumnNames.getOrDefault(entityType, idColumnName);
  }
}
