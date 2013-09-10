package org.obiba.opal.core.cfg;

import com.orientechnologies.orient.object.db.OObjectDatabaseTx;

public abstract class OrientDbTransactionCallbackWithoutResult implements OrientDbTransactionCallback<Void> {

  @Override
  public final Void doInTransaction(OObjectDatabaseTx db) {
    doInTransactionWithoutResult(db);
    return null;
  }

  protected abstract void doInTransactionWithoutResult(OObjectDatabaseTx db);

}
