package org.obiba.opal.web.gwt.app.client.analysis.event;

import com.gwtplatform.dispatch.annotation.GenEvent;
import com.gwtplatform.dispatch.annotation.Order;
import org.obiba.opal.web.model.client.opal.OpalAnalysisDto;

@GenEvent
public class RunAnalysisRequest {
  @Order(0)
  OpalAnalysisDto analysisDto;
}

