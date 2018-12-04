package org.obiba.opal.web.gwt.app.client.analysis;

import com.google.gwt.core.shared.GWT;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import org.obiba.opal.web.gwt.app.client.analysis.event.RunAnalysisRequestEvent;
import org.obiba.opal.web.gwt.app.client.analysis.support.AnalysisPluginData;
import org.obiba.opal.web.gwt.app.client.analysis.support.AnalysisPluginsRepository;
import org.obiba.opal.web.gwt.app.client.analysis.support.PluginTemplateVisitor;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationsUtils;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.opal.OpalAnalysisDto;
import org.obiba.opal.web.model.client.opal.PluginPackageDto;

import javax.inject.Inject;
import java.util.List;

public class AnalysisModalPresenter extends ModalPresenterWidget<AnalysisModalPresenter.Display>
  implements AnalysisModalUiHandlers  {

  private final Translations translations;
  private TableDto table;
  private OpalAnalysisDto analysis;

  @Inject
  public AnalysisModalPresenter(EventBus eventBus,
                                Display view,
                                Translations translations) {
    super(eventBus, view);
    getView().setUiHandlers(this);
    this.translations = translations;
  }


  @Override
  protected void onBind() {
    GWT.log("AnalysisModalPresenter.onBind()");
  }

  public void initialize(TableDto tableDto, OpalAnalysisDto analysisDto, List<PluginPackageDto> plugins) {
    table = tableDto;
    analysis = analysisDto;
    AnalysisPluginsRepository pluginsRepository = new AnalysisPluginsRepository(plugins);
    AnalysisPluginData analysisPluginData = pluginsRepository.findAnalysisPluginData(analysis);

    if (analysis != null && analysisPluginData == null) {
      getView().showError(
        TranslationsUtils.replaceArguments(
          translations.userMessageMap().get("InvalidAnalysisPluginData"),
          analysis.getName())
      );
    } else {
      pluginsRepository.visitPlugins(getView().getTemplateVisitor());
      getView().initialize(tableDto, analysisDto, analysisPluginData);
    }
  }

  @Override
  public void run(OpalAnalysisDto analysisDto) {
    Display view = getView();
    view.clearErrors();

    if (view.validate()) {
      fireEvent(new RunAnalysisRequestEvent(analysisDto));
      view.hideDialog();
    }
  }

  public interface Display extends PopupView, HasUiHandlers<AnalysisModalUiHandlers> {

    void hideDialog();

    void initialize(TableDto tableDto, OpalAnalysisDto analysisDto, AnalysisPluginData analysisPluginData);

    PluginTemplateVisitor getTemplateVisitor();

    boolean validate();

    void showError(String message);

    void clearErrors();
  }
}