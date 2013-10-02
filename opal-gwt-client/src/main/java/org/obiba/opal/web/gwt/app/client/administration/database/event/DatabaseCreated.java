package org.obiba.opal.web.gwt.app.client.administration.database.event;

import org.obiba.opal.web.model.client.opal.DatabaseDto;

import com.gwtplatform.dispatch.annotation.GenEvent;

@GenEvent
public class DatabaseCreated {

  DatabaseDto dto;

}
