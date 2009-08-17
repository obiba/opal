package org.obiba.opal.sesame.report.selection;

import java.util.Set;

import org.obiba.opal.elmo.concepts.DataEntryForm;
import org.obiba.opal.sesame.report.IDataItemSelection;
import org.openrdf.model.URI;

public class DataEntryFormItemSelection extends AbstractSparqlSelection implements IDataItemSelection {

  private String name;

  public void prepare() {
    setBinding("def", getResourceFactory().findResource(DataEntryForm.URI, name));
  }

  public Set<URI> getSelection() {
    prepare();
    return doSelectDataItem("select ?dataItem {?dataItem opal:dataEntryForm ?def}");
  }

}
