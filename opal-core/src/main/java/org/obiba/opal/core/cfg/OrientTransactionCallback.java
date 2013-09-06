package org.obiba.opal.core.cfg;

public interface OrientTransactionCallback<T> {

  T doInTransaction();

}
