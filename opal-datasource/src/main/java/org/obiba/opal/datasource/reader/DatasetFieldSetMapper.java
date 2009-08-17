package org.obiba.opal.datasource.reader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.obiba.opal.core.domain.data.DataPoint;
import org.obiba.opal.core.domain.data.Dataset;
import org.obiba.opal.core.domain.data.Entity;
import org.obiba.opal.datasource.DatasourceService;
import org.springframework.batch.item.file.LineCallbackHandler;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.batch.item.file.transform.LineTokenizer;

public class DatasetFieldSetMapper implements LineCallbackHandler, FieldSetMapper<Dataset> {

  private DatasourceService datasourceService;

  private String catalogueName;

  private Date extractionDate;

  /** Used to tokenize the header line */
  private LineTokenizer lineTokenizer;

  /** FieldSet that contains the header of each column. Used to obtain the name of a DataPoint */
  private FieldSet fieldNames;

  public void setCatalogueName(String catalogueName) {
    this.catalogueName = catalogueName;
  }

  public void setDatasourceService(DatasourceService datasourceService) {
    this.datasourceService = datasourceService;
  }

  public void setLineTokenizer(LineTokenizer lineTokenizer) {
    this.lineTokenizer = lineTokenizer;
  }

  public void setExtractionDate(String extractionDate) {
    try {
      this.extractionDate = new SimpleDateFormat("yyyy-MM-dd").parse(extractionDate);
    } catch(ParseException e) {
      throw new IllegalArgumentException(e);
    }
  }

  public void handleLine(String line) {
    if(fieldNames == null) {
      fieldNames = lineTokenizer.tokenize(line);
    }
  }

  protected String getName(int index) {
    if(fieldNames != null) {
      return fieldNames.readString(index);
    }
    throw new IllegalStateException("fieldNames not set. Make sure the same instance is used for the skippedLinesCallback property of a FlarFileItemReader");
  }

  public Dataset mapFieldSet(FieldSet fieldSet) {
    String entityId = fieldSet.readString(0);
    Entity entity = datasourceService.fetchEntity(entityId);
    Dataset ds = new Dataset(entity, datasourceService.loadCatalogue(catalogueName), extractionDate);
    for(int i = 1; i < fieldSet.getFieldCount(); i++) {
      String dataPointValue = fieldSet.readString(i);
      // TODO: Lookup DataIem
      ds.getDataPoints().add(new DataPoint(ds, null, dataPointValue));
    }
    return ds;
  }
}
