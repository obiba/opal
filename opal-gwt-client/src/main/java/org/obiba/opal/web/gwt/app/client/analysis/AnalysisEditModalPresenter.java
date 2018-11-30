package org.obiba.opal.web.gwt.app.client.analysis;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import org.obiba.opal.web.gwt.app.client.analysis.service.OpalAnalysisPluginData;
import org.obiba.opal.web.gwt.app.client.analysis.service.PluginService;
import org.obiba.opal.web.gwt.app.client.analysis.service.PluginVisitor;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.model.client.opal.OpalAnalysisDto;

import javax.inject.Inject;

public class AnalysisEditModalPresenter extends ModalPresenterWidget<AnalysisEditModalPresenter.Display>
  implements AnalysisEditModalUiHandlers {


  private final Translations translations;

  private PluginService pluginService;

  @Inject
  public AnalysisEditModalPresenter(AnalysisEditModalPresenter.Display display,
                                    EventBus eventBus,
                                    Translations translations,
                                    PluginService service) {
    super(eventBus, display);
    getView().setUiHandlers(this);
    this.translations = translations;
    pluginService = service;
  }

  @Override
  protected void onBind() {
  }

  void setAnalysis(OpalAnalysisDto analysis) {
    getView().initialize(analysis, pluginService.getAnalysisPluginData(analysis));
  }

  public void initialize(OpalAnalysisDto analysis) {
    pluginService.travers(getView());
    getView().initialize(analysis, pluginService.getAnalysisPluginData(analysis));
  }


  public interface Display extends PopupView, HasUiHandlers<AnalysisEditModalUiHandlers>, PluginVisitor {

    void hideDialog();

    void initialize(OpalAnalysisDto dto, OpalAnalysisPluginData pluginData);

  }

}
