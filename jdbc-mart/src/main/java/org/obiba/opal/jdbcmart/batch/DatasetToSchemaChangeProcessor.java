package org.obiba.opal.jdbcmart.batch;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.change.InsertDataChange;

import org.obiba.opal.core.domain.data.DataPoint;
import org.obiba.opal.core.domain.data.Dataset;
import org.obiba.opal.elmo.concepts.DataItem;
import org.obiba.opal.sesame.report.Report;
import org.springframework.batch.item.ItemProcessor;

public class DatasetToSchemaChangeProcessor implements ItemProcessor<Dataset, Change> {

  private static final String COLUMN_NAME_PREFIX = "OPAL_";

  private static final String ENTITY_KEY_NAME = "entity_id";

  private static final String OCCURRENCE_COLUMN_NAME = "occurrence";

  private List<Report> reports;

  private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

  public DatasetToSchemaChangeProcessor() {
  }

  public void setReports(List<Report> reports) {
    this.reports = reports;
  }

  public Change process(Dataset dataset) throws Exception {
    CompositeChange composite = new CompositeChange();
    Map<String, DataPoint> points = mapify(dataset);
    for(Report report : reports) {
      composite.addChange(doCreateInsert(report, dataset, points));
    }
    return composite;
  }

  protected Change doCreateInsert(Report report, Dataset dataset, Map<String, DataPoint> points) throws ParseException {
    InsertDataChange idc = new InsertDataChange();
    idc.setTableName(report.getName());

    ColumnConfig cc = new ColumnConfig();
    cc.setName(ENTITY_KEY_NAME);
    cc.setValue(dataset.getEntity().getIdentifier());
    idc.addColumn(cc);

    for(DataItem dataItem : report.getDataItems()) {
      if(dataItem.getDataType() == null) {
        continue;
      }
      cc = new ColumnConfig();
      cc.setName(COLUMN_NAME_PREFIX + dataItem.getIdentifier());
      cc.setValue(null);
      idc.addColumn(cc);

      DataPoint point = points.get(dataItem.getIdentifier());
      if(point != null) {
        if(dataItem.getDataType().equals("DATE")) {
          cc.setValueDate(new java.sql.Date(sdf.parse(point.getValue()).getTime()));
        } else if(dataItem.getDataType().equals("BOOLEAN")) {
          cc.setValueBoolean(Boolean.valueOf(point.getValue()));
        } else {
          cc.setValue(point.getValue());
        }
      }
    }
    return idc;
  }

  protected Map<String, DataPoint> mapify(Dataset dataset) {
    Map<String, DataPoint> map = new HashMap<String, DataPoint>();
    for(DataPoint point : dataset.getDataPoints()) {
      map.put(point.getDataItem().getCode().toString(), point);
    }
    return map;
  }
}
