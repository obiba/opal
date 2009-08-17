package org.obiba.opal.sesame.report.selection;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.obiba.opal.sesame.report.IDataItemSelection;
import org.openrdf.model.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntersectingSelection implements IDataItemSelection {

  private static final Logger log = LoggerFactory.getLogger(IntersectingSelection.class);

  private List<IDataItemSelection> selections;

  public Set<URI> getSelection() {
    Set<URI> selection = new HashSet<URI>();
    boolean first = true;
    for(IDataItemSelection s : selections) {
      if(first) {
        selection.addAll(s.getSelection());
        log.debug("Initial size: {}" + selection.size());
      } else {
        selection.retainAll(s.getSelection());
        log.debug("Intersection size: {}" + selection.size());
      }
      first = false;
    }
    return selection;
  }

}
