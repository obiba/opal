/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.project.admin;

import com.google.common.base.Strings;
import com.google.gwt.core.client.JsonUtils;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import org.obiba.opal.web.gwt.app.client.administration.taxonomies.event.TaxonomyImportedEvent;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectorPresenter;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationsUtils;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.magma.copy.DataCopyPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilder;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.opal.BackupCommandOptionsDto;
import org.obiba.opal.web.model.client.opal.CopyCommandOptionsDto;
import org.obiba.opal.web.model.client.ws.ClientErrorDto;

public class ProjectBackupModalPresenter extends ModalPresenterWidget<ProjectBackupModalPresenter.Display>
    implements ProjectBackupModalUiHandlers {

  private final FileSelectionPresenter fileSelectionPresenter;

  private final Translations translations;

  private String projectName;

  @Inject
  public ProjectBackupModalPresenter(EventBus eventBus, Display display, FileSelectionPresenter fileSelectionPresenter, Translations translations) {
    super(eventBus, display);
    this.fileSelectionPresenter = fileSelectionPresenter;
    getView().setUiHandlers(this);
    this.translations = translations;
  }

  @Override
  protected void onBind() {
    fileSelectionPresenter.bind();
    fileSelectionPresenter.setFileSelectionType(FileSelectorPresenter.FileSelectionType.FOLDER);
    getView().setFileSelectorWidgetDisplay(fileSelectionPresenter.getView());
  }

  @Override
  public void onBackup(boolean viewsAsTables) {
    String path = fileSelectionPresenter.getSelectedFile();
    if (Strings.isNullOrEmpty(path)) {
      getView().showError("ProjectBackupFolderIsRequired");
      return;
    }

    BackupCommandOptionsDto dto = BackupCommandOptionsDto.create();
    dto.setArchive(path);
    dto.setViewsAsTables(viewsAsTables);
    dto.setOverride(true);

    ResourceRequestBuilderFactory.newBuilder()
        .forResource(UriBuilders.PROJECT_COMMANDS_BACKUP.create().build(projectName))
        .post()
        .withResourceBody(BackupCommandOptionsDto.stringify(dto))
        .withCallback(Response.SC_CREATED, new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            getView().hide();
          }
        })
        .send();
  }

  public void setProjectName(String name) {
    this.projectName = name;
  }

  private static class TaxonomyResourceErrorHandler implements ResponseCodeCallback {

    private final Translations translations;

    private final Display view;

    private final String defaultMessage;

    TaxonomyResourceErrorHandler(Display view, Translations translations, String defaultMessage) {
      this.translations = translations;
      this.view = view;
      this.defaultMessage = defaultMessage;
    }

    @Override
    public void onResponseCode(Request request, Response response) {
      String message = response.getText();
      ClientErrorDto errorDto = Strings.isNullOrEmpty(message) ? null : (ClientErrorDto) JsonUtils.unsafeEval(message);

      if (errorDto != null) {
        view.showError(TranslationsUtils.replaceArguments(
            translations.userMessageMap().get(errorDto.getStatus()),
            JsArrays.toList(errorDto.getArgumentsArray())));
      } else if (!Strings.isNullOrEmpty(message)) {
        view.showError(message);
      } else {
        view.showError(defaultMessage);
      }
    }
  }

  public interface Display extends PopupView, HasUiHandlers<ProjectBackupModalUiHandlers> {

    void hideDialog();

    void setFileSelectorWidgetDisplay(FileSelectionPresenter.Display display);

    void showError(String messageKey);

    void clearErrors();
  }

}
