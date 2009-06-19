package org.obiba.opal.mart.reader.sesame;

import org.openrdf.query.BindingSet;

public interface BindingSetMapper<T> {

	T mapBindingSet(BindingSet bindingSet, int setNum);

}
