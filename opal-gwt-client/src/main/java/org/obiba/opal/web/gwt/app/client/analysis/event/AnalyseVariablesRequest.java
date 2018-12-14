package org.obiba.opal.web.gwt.app.client.analysis.event;

import com.gwtplatform.dispatch.annotation.GenEvent;
import com.gwtplatform.dispatch.annotation.Order;
import org.obiba.opal.web.model.client.magma.VariableDto;

import java.util.List;

@GenEvent
public class AnalyseVariablesRequest {
  @Order(0)
  List<VariableDto> variables;
}

