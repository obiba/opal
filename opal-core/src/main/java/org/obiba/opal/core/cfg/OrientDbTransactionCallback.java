package org.obiba.opal.core.cfg;

import com.orientechnologies.orient.object.db.OObjectDatabaseTx;

public interface OrientDbTransactionCallback<T> {

  T doInTransaction(OObjectDatabaseTx db);

}
