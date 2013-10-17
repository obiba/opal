package org.obiba.opal.web.gwt.app.client.administration.datashield.event;

import org.obiba.opal.web.model.client.datashield.DataShieldMethodDto;

import com.gwtplatform.dispatch.annotation.GenEvent;

@GenEvent
public class DataShieldMethodCreated {

  DataShieldMethodDto dto;

}
