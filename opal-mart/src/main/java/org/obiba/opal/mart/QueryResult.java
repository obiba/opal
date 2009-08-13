package org.obiba.opal.mart;

import org.obiba.opal.elmo.concepts.DataItem;

public interface QueryResult {

	public String getEntityId();

	public boolean hasOccurrence();

	public int getOccurrence();

	public DataItem getDataItemClass();

	public Object getValue();

}
