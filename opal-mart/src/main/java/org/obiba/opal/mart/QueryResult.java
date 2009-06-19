package org.obiba.opal.mart;

import org.obiba.opal.elmo.owl.concepts.DataItemClass;

public interface QueryResult {

	public String getEntityId();

	public boolean hasOccurrence();

	public int getOccurrence();

	public DataItemClass getDataItemClass();

	public Object getValue();

}
