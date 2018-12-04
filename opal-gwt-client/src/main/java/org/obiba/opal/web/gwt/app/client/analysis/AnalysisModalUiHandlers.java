package org.obiba.opal.web.gwt.app.client.analysis;

import org.obiba.opal.web.gwt.app.client.ui.ModalUiHandlers;
import org.obiba.opal.web.model.client.opal.OpalAnalysisDto;

public interface AnalysisModalUiHandlers extends ModalUiHandlers {
  void run(OpalAnalysisDto analysisDto);
}
