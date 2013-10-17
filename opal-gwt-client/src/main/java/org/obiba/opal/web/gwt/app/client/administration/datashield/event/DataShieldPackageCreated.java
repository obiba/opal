package org.obiba.opal.web.gwt.app.client.administration.datashield.event;

import org.obiba.opal.web.model.client.opal.r.RPackageDto;

import com.gwtplatform.dispatch.annotation.GenEvent;

@GenEvent
public class DataShieldPackageCreated {

  RPackageDto dto;

}
