package org.obiba.opal.web.gwt.app.client.analysis;

import org.obiba.opal.web.gwt.app.client.inject.AbstractOpalModule;

public class AnalysisModule extends AbstractOpalModule {

  @Override
  protected void configure() {
    bindPresenterWidget(AnalysesPresenter.class, AnalysesPresenter.Display.class, AnalysesView.class);
    bindPresenterWidget(AnalysisModalPresenter.class, AnalysisModalPresenter.Display.class, AnalysisModalView.class);
  }
}
