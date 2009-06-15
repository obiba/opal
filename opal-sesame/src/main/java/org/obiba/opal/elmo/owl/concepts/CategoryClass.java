package org.obiba.opal.elmo.owl.concepts;

import org.obiba.opal.elmo.concepts.Opal;
import org.openrdf.elmo.annotations.rdf;

public interface CategoryClass extends DataItemClass {

  @rdf(Opal.NS + "code")
  public String getCode();

  public void setCode(String code);

}
