package org.obiba.opal.sesame.report.selection;

import java.util.Set;

import org.obiba.opal.sesame.report.IDataItemSelection;
import org.openrdf.model.URI;

public class ChildrenItemSelection extends AbstractSparqlSelection implements IDataItemSelection {

  private String path;

  public void prepare() {
    setBinding("path", getValueFactory().createLiteral(path));
  }

  public Set<URI> getSelection() {
    prepare();
    return doSelectDataItem("select ?dataItem {?dataItem a opal:DataItem ; opal:hasParent [ opal:path ?path ]}");
  }

}
