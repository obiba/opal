/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.datasource.onyx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.obiba.onyx.engine.variable.IVariablePathNamingStrategy;
import org.obiba.onyx.engine.variable.Variable;
import org.obiba.onyx.engine.variable.VariableData;
import org.obiba.onyx.engine.variable.VariableDataSet;
import org.obiba.onyx.engine.variable.util.VariableFinder;
import org.obiba.onyx.util.data.Data;
import org.obiba.opal.core.domain.data.DataPoint;
import org.obiba.opal.core.domain.data.Dataset;
import org.obiba.opal.core.domain.data.Entity;
import org.obiba.opal.core.domain.metadata.Catalogue;
import org.obiba.opal.datasource.DatasourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

/**
 * 
 */
public class OnyxDatasetReader extends AbstractOnyxReader<Dataset> implements ItemStreamReader<Dataset> {

  private static final Logger log = LoggerFactory.getLogger(OnyxDatasetReader.class);

  private String catalogueName;

  private DatasourceService datasourceService;

  private Iterator<String> entryIterator;

  private Catalogue catalogue;

  private IVariablePathNamingStrategy variablePathNamingStrategy;

  public void setDatasourceService(DatasourceService datasourceService) {
    this.datasourceService = datasourceService;
  }

  public void setCatalogueName(String catalogueName) {
    this.catalogueName = catalogueName;
  }

  public void setVariablePathNamingStrategy(IVariablePathNamingStrategy variablePathNamingStrategy) {
    this.variablePathNamingStrategy = variablePathNamingStrategy;
  }

  @Override
  protected void doOpen(ExecutionContext executionContext) throws ItemStreamException {
    this.entryIterator = getDataInputStrategy().listEntries().iterator();
  }

  public Dataset read() throws Exception, UnexpectedInputException, ParseException {
    if(this.catalogue == null) {
      this.catalogue = datasourceService.loadCatalogue(catalogueName);
    }
    if(entryIterator.hasNext()) {
      String entryName = entryIterator.next();
      while(isParticipantEntry(entryName) == false && entryIterator.hasNext()) {
        entryName = entryIterator.next();
      }
      if(isParticipantEntry(entryName)) {
        log.info("Processing entry {}", entryName);
        VariableDataSet variableDataSetRoot = readVariableDataset(entryName);

        Entity entity = datasourceService.fetchEntity(entryName.replace(".xml", ""));

        Dataset dataset = new Dataset(entity, catalogue, variableDataSetRoot.getExportDate());

        for(VariableData vd : variableDataSetRoot.getVariableDatas()) {
          handleVariableData(dataset, vd, null);
        }
        return dataset;
      }
    }
    return null;
  }

  /**
   * Given a <code>Dataset</code> and <code>VariableData</code>, recursively adds the necessary
   * <code>DataPoint</code>s to the <code>Dataset</code>
   * 
   * @param dataset dataset
   * @param variableData variableData
   * @param parentDatas datas of variableData's parent (<code>null</code> if none)
   */
  private void handleVariableData(Dataset dataset, VariableData variableData, List<Integer> parentOccurrenceIds) {
    List<Data> datas = variableData.getDatas();
    List<Integer> occurrenceIds = null;

    // Look up variableData's variable.
    VariableFinder variableFinder = VariableFinder.getInstance(variableRoot, variablePathNamingStrategy);
    Variable variable = variableFinder.findVariable(variableData.getVariablePath());

    if(!variable.isMultiple() && !variable.isRepeatable()) {
      // Variable is NON-MULTIPLE and NON-REPEATABLE. Add a DataPoint containing the data value.
      Data data = datas.get(0);
      DataPoint dataPoint = new DataPoint(dataset, variableData.getVariablePath(), data.getValueAsString());
      dataset.getDataPoints().add(dataPoint);
    } else if(variable.isCategorial() && variable.isMultiple()) {
      // Variable is CATEGORIAL and MULTIPLE. Add a DataPoint with a comma-separated list of data values.
      DataPoint dataPoint = null;
      String values = toCommaSeparatedValues(datas);

      if(variable.isRepeatable()) {
        // Determine the occurrence id and use this to create the DataPoint.
        int occurrenceId = getOccurrenceId(variableData.getVariablePath());
        int normalizedOccurrenceId = getNormalizedOccurrenceId(parentOccurrenceIds, occurrenceId);
        dataPoint = new DataPoint(dataset, variableData.getVariablePath(), values, normalizedOccurrenceId);
      } else {
        dataPoint = new DataPoint(dataset, variableData.getVariablePath(), values);
      }
      dataset.getDataPoints().add(dataPoint);
    } else if(!variable.isCategorial() && variable.isMultiple() && !variable.isRepeatable()) {
      // Variable is NON-CATEGORIAL, MULTIPLE and NON-REPEATABLE. This must mean that it has
      // REPEATABLE children. Its data consists of a list of occurrence ids. Convert to a list
      // of Integers and pass it along to the children-handling code.
      occurrenceIds = new ArrayList<Integer>();
      for(Data data : datas) {
        occurrenceIds.add(Integer.valueOf(data.getValueAsString()));
      }
    } else if(!variable.isMultiple() && variable.isRepeatable()) {
      // Variable is NON-MULTIPLE and REPEATABLE. Determine its occurrence id and add a DataPoint.
      int occurrenceId = getOccurrenceId(variableData.getVariablePath());
      int normalizedOccurrenceId = getNormalizedOccurrenceId(parentOccurrenceIds, occurrenceId);

      DataPoint dataPoint = new DataPoint(dataset, variableData.getVariablePath(), datas.get(0).getValueAsString(), normalizedOccurrenceId);
      dataset.getDataPoints().add(dataPoint);
    } else if(!variable.isCategorial() && variable.isMultiple() && variable.isRepeatable()) {
      // TODO: Is this case even possible? This would be a REPEATABLE variable with REPEATABLE children.
      // Does it have any data of its own? Or is the data simply a list of occurrence ids? Both? How is
      // one distinguished from the other? Do nothing for now.
    }

    // Recurse on variableData children.
    for(VariableData child : variableData.getVariableDatas()) {
      handleVariableData(dataset, child, occurrenceIds);
    }
  }

  private Integer getOccurrenceId(String variablePath) {
    Map<String, String> parameters = variablePathNamingStrategy.getParameters(variablePath);
    if(parameters != null && parameters.size() > 0) {
      String parentName = parameters.keySet().iterator().next();
      List<String> parts = variablePathNamingStrategy.getNormalizedNames(variablePath);
      parts.remove(parts.size() - 1);
      Collections.reverse(parts);
      for(Iterator<String> iterator = parts.iterator(); iterator.hasNext();) {
        String string = iterator.next();
        if(string.equals(parentName) == false) {
          iterator.remove();
        } else {
          break;
        }
      }
      Collections.reverse(parts);

      StringBuilder parentPath = new StringBuilder();
      for(String part : parts) {
        if(parentPath.length() > 0) {
          parentPath.append(variablePathNamingStrategy.getPathSeparator());
        }
        parentPath.append(part);
      }

      String occurrenceId = parameters.values().iterator().next();
      return Integer.valueOf(occurrenceId);
    }
    return null;
  }

  private Integer getNormalizedOccurrenceId(List<Integer> parentOccurrenceIds, Integer occurrenceId) {
    for(Integer aParentOccurrenceId : parentOccurrenceIds) {
      if(aParentOccurrenceId.equals(occurrenceId)) {
        return aParentOccurrenceId;
      }
    }

    return null;
  }

  private String toCommaSeparatedValues(List<Data> datas) {
    StringBuffer values = new StringBuffer();
    for(int i = 0; i < datas.size(); i++) {
      values.append(datas.get(0).getValueAsString());
      if(i < datas.size() - 1) {
        values.append(", ");
      }
    }

    return values.toString();
  }
}
