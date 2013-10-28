package org.obiba.opal.core.service;

import javax.annotation.Nonnull;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.object.db.OObjectDatabaseTx;
import com.orientechnologies.orient.server.OServer;

public interface OrientDbServerFactory {

  @Nonnull
  OServer getServer();

  @Nonnull
  OObjectDatabaseTx getObjectTx();

  @Nonnull
  ODatabaseDocumentTx getDocumentTx();

  void setUrl(@Nonnull String url);
}
