package org.obiba.opal.web.gwt.app.client.administration.database.event;

import org.obiba.opal.web.model.client.database.DatabaseDto;

import com.gwtplatform.dispatch.annotation.GenEvent;

/**
 * Will generate {@link DatabaseUpdatedEvent} and {@link DatabaseUpdatedEvent.DatabaseUpdatedHandler}
 */
@GenEvent
public class DatabaseUpdated {

  DatabaseDto dto;

}
