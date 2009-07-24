package org.obiba.opal.datasource.reader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.obiba.opal.core.domain.data.DataItem;
import org.obiba.opal.core.domain.data.Dataset;
import org.obiba.opal.core.domain.data.Entity;
import org.obiba.opal.datasource.EntityProvider;
import org.springframework.batch.item.file.LineCallbackHandler;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.batch.item.file.transform.LineTokenizer;

public class DatasetFieldSetMapper implements LineCallbackHandler, FieldSetMapper<Dataset> {

  private EntityProvider entityProvider;

  private String datasource;

  private Date extractionDate;

  /** Used to tokenize the header line */
  private LineTokenizer lineTokenizer;

  /** FieldSet that contains the header of each column. Used to obtain the name of a DataItem */
  private FieldSet fieldNames;

  public void setEntityProvider(EntityProvider entityProvider) {
    this.entityProvider = entityProvider;
  }

  public void setLineTokenizer(LineTokenizer lineTokenizer) {
    this.lineTokenizer = lineTokenizer;
  }

  public void setDatasource(String datasource) {
    this.datasource = datasource;
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
    Entity entity = entityProvider.fetchEntity(datasource, entityId);
    Dataset ds = new Dataset(entity, datasource, extractionDate);
    for(int i = 1; i < fieldSet.getFieldCount(); i++) {
      String dataItemValue = fieldSet.readString(i);
      ds.getDataItems().add(new DataItem(ds, getName(i), dataItemValue));
    }
    return ds;
  }
}
