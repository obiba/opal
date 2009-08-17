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
    return doSelectDataItem("select ?dataItem {_:parent opal:hasChild ?dataItem ; opal:path ?path ; a opal:DataItem}");
  }

}
