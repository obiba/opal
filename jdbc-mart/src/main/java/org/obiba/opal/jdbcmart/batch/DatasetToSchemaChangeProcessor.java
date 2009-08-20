package org.obiba.opal.jdbcmart.batch;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import liquibase.change.Change;
import liquibase.change.ColumnConfig;
import liquibase.change.InsertDataChange;

import org.obiba.opal.core.domain.data.DataPoint;
import org.obiba.opal.core.domain.data.Dataset;
import org.obiba.opal.elmo.concepts.DataItem;
import org.obiba.opal.sesame.report.Report;
import org.springframework.batch.item.ItemProcessor;

public class DatasetToSchemaChangeProcessor implements ItemProcessor<Dataset, Change> {

  private List<Report> reports;

  private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

  private String studyPrefix;

  public DatasetToSchemaChangeProcessor() {
  }

  public void setReports(List<Report> reports) {
    this.reports = reports;
  }

  public void setStudyPrefix(String studyPrefix) {
    this.studyPrefix = (studyPrefix != null) ? studyPrefix : "";
  }

  public Change process(Dataset dataset) throws Exception {
    CompositeChange composite = new CompositeChange();
    for(Report report : reports) {
      addChanges(composite, report, dataset);
    }
    return composite;
  }

  protected void addChanges(CompositeChange compositeChange, Report report, Dataset dataset) throws ParseException {
    if(!report.hasOccurrence()) {
      Map<String, DataPoint> points = mapify(dataset, null);
      compositeChange.addChange(doCreateInsert(report, dataset, points, null));
    } else {
      Set<Integer> occurrences = new HashSet<Integer>();
      for(DataPoint dataPoint : dataset.getDataPoints()) {
        if(dataPoint.getOccurrence() != null) {
          occurrences.add(dataPoint.getOccurrence());
        }
      }

      for(Integer occurrence : occurrences) {
        Map<String, DataPoint> points = mapify(dataset, occurrence);
        for(DataItem item : report.getDataItems()) {
          if(points.containsKey(item.getIdentifier())) {
            compositeChange.addChange(doCreateInsert(report, dataset, points, occurrence));
            break;
          }
        }
      }
    }
  }

  protected Change doCreateInsert(Report report, Dataset dataset, Map<String, DataPoint> points, Integer occurrence) throws ParseException {
    InsertDataChange idc = new InsertDataChange();
    idc.setTableName(report.getName());

    addEntityKeyValue(idc, dataset.getEntity().getIdentifier());

    if(occurrence != null) {
      addOccurrenceValue(idc, occurrence);
    }

    for(DataItem dataItem : report.getDataItems()) {
      if(dataItem.getDataType() == null) {
        continue;
      }

      addDataPointValue(idc, dataItem, points.get(dataItem.getIdentifier()));
    }
    return idc;
  }

  protected Map<String, DataPoint> mapify(Dataset dataset, Integer occurrence) {
    Map<String, DataPoint> map = new HashMap<String, DataPoint>();
    for(DataPoint point : dataset.getDataPoints()) {
      if(occurrence == null || (point.getOccurrence() != null && point.getOccurrence().equals(occurrence))) {
        map.put(point.getDataItem().getCode().toString(), point);
      }
    }
    return map;
  }

  private void addEntityKeyValue(InsertDataChange idc, String entityId) {
    ColumnConfig cc = new ColumnConfig();
    cc.setName(SchemaChangeConstants.ENTITY_KEY_NAME);
    cc.setValue(entityId);
    idc.addColumn(cc);
  }

  private void addOccurrenceValue(InsertDataChange idc, Integer occurrence) {
    ColumnConfig oc = new ColumnConfig();
    oc.setName(SchemaChangeConstants.OCCURRENCE_COLUMN_NAME);
    oc.setValueNumeric(occurrence);
    idc.addColumn(oc);
  }

  private void addDataPointValue(InsertDataChange idc, DataItem dataItem, DataPoint dataPoint) throws ParseException {
    ColumnConfig cc = new ColumnConfig();
    cc.setName(studyPrefix + dataItem.getIdentifier());
    cc.setValue(null);
    idc.addColumn(cc);

    if(dataPoint != null) {
      if(dataItem.getDataType().equals("DATE")) {
        cc.setValueDate(new java.sql.Timestamp(sdf.parse(dataPoint.getValue()).getTime()));
      } else if(dataItem.getDataType().equals("BOOLEAN")) {
        cc.setValueBoolean(Boolean.valueOf(dataPoint.getValue()));
      } else {
        cc.setValue(dataPoint.getValue());
      }
    }
  }
}
