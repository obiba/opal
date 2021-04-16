/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.analysis;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import java.util.List;
import javax.inject.Inject;
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
  protected void onBind() { }

  public void initialize(TableDto tableDto,
                         OpalAnalysisDto analysisDto,
                         List<String> existingNames,
                         List<PluginPackageDto> plugins) {

    table = tableDto;
    analysis = analysisDto;
    AnalysisPluginsRepository pluginsRepository = new AnalysisPluginsRepository(plugins);
    AnalysisPluginData analysisPluginData = pluginsRepository.findAnalysisPluginData(analysis);

    if (analysis != null && analysis.hasName() && analysisPluginData == null) {
      getView().showError(
        TranslationsUtils.replaceArguments(
          translations.userMessageMap().get("InvalidAnalysisPluginData"),
          analysis.getName())
      );
    } else {
      pluginsRepository.visitPlugins(getView().getTemplateVisitor());
      getView().initialize(tableDto, analysisDto, existingNames, analysisPluginData);
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

    void initialize(TableDto tableDto, OpalAnalysisDto analysisDto, List<String> existingNames, AnalysisPluginData analysisPluginData);

    PluginTemplateVisitor getTemplateVisitor();

    boolean validate();

    void showError(String message);

    void clearErrors();
  }
}