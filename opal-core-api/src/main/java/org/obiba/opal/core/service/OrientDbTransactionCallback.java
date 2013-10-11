package org.obiba.opal.core.service;

import com.orientechnologies.orient.object.db.OObjectDatabaseTx;

public interface OrientDbTransactionCallback<T> {

  T doInTransaction(OObjectDatabaseTx db);

}
