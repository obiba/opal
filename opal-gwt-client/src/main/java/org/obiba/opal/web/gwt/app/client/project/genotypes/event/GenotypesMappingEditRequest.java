package org.obiba.opal.web.gwt.app.client.project.genotypes.event;

import com.gwtplatform.dispatch.annotation.GenEvent;
import com.gwtplatform.dispatch.annotation.Order;
import org.obiba.opal.web.model.client.opal.GenotypesMappingDto;

@GenEvent
public class GenotypesMappingEditRequest {
  @Order(0)
  GenotypesMappingDto genotypesMapping;
}
