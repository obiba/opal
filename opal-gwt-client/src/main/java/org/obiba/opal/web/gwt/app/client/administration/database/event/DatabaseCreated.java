package org.obiba.opal.web.gwt.app.client.administration.database.event;

import org.obiba.opal.web.model.client.opal.DatabaseDto;

import com.gwtplatform.dispatch.annotation.GenEvent;

/**
 * Will generate {@link DatabaseCreatedEvent} and {@link DatabaseCreatedEvent.DatabaseCreatedHandler}
 */
@GenEvent
public class DatabaseCreated {

  DatabaseDto dto;

}
