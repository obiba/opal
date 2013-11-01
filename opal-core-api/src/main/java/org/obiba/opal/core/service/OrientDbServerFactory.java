package org.obiba.opal.core.service;

import javax.validation.constraints.NotNull;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.server.OServer;

public interface OrientDbServerFactory {

  @NotNull
  OServer getServer();

  @NotNull
  ODatabaseDocumentTx getDocumentTx();

  void setUrl(@NotNull String url);
}
