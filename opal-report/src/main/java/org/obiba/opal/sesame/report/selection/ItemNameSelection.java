package org.obiba.opal.sesame.report.selection;

import java.util.Set;

import org.obiba.opal.sesame.report.IDataItemSelection;
import org.openrdf.model.URI;

public class ItemNameSelection extends AbstractSparqlSelection implements IDataItemSelection {

  private String pattern;

  private boolean exclude;

  public Set<URI> getSelection() {
    return doSelectDataItem("select ?dataItem {?dataItem opal:name ?name FILTER ("+(exclude ? "!" : "") + "REGEX(?name, \"" + pattern + "\"))}");
  }
}
