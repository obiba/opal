package org.obiba.opal.sesame.report.selection;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.obiba.opal.sesame.report.IDataItemSelection;
import org.openrdf.model.URI;

public class UnionSelection implements IDataItemSelection {

  private List<IDataItemSelection> selections;

  public Set<URI> getSelection() {
    Set<URI> selection = new HashSet<URI>();
    for(IDataItemSelection s : selections) {
      selection.addAll(s.getSelection());
    }
    return selection;
  }

}
