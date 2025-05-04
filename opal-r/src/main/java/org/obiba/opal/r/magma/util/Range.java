package org.obiba.opal.r.magma.util;

import java.util.List;

public interface Range {

  boolean hasRange();

  String getRangeMin();

  String getRangeMax();

  List<String> getMissingCats();
}
