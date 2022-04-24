/*
 * Copyright (c) 2022 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.project.admin;

import com.google.common.base.Strings;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectionPresenter;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectorPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.opal.RestoreCommandOptionsDto;

public class ProjectRestoreModalPresenter extends ModalPresenterWidget<ProjectRestoreModalPresenter.Display>
    implements ProjectRestoreModalUiHandlers {

  private final FileSelectionPresenter fileSelectionPresenter;

  private String projectName;

  @Inject
  public ProjectRestoreModalPresenter(EventBus eventBus, Display display, FileSelectionPresenter fileSelectionPresenter) {
    super(eventBus, display);
    this.fileSelectionPresenter = fileSelectionPresenter;
    getView().setUiHandlers(this);
  }

  @Override
  protected void onBind() {
    fileSelectionPresenter.bind();
    fileSelectionPresenter.setFileSelectionType(FileSelectorPresenter.FileSelectionType.FILE_OR_FOLDER);
    fileSelectionPresenter.setFileFilter("\\.zip$");
    getView().setFileSelectorWidgetDisplay(fileSelectionPresenter.getView());
  }

  @Override
  public void onRestore(String password, boolean override) {
    String path = fileSelectionPresenter.getSelectedFile();
    if (Strings.isNullOrEmpty(path)) {
      getView().showError("ProjectRestoreFolderIsRequired");
      return;
    }

    RestoreCommandOptionsDto dto = RestoreCommandOptionsDto.create();
    dto.setArchive(path);
    if (!Strings.isNullOrEmpty(password))
      dto.setPassword(password);
    dto.setOverride(override);

    ResourceRequestBuilderFactory.newBuilder()
        .forResource(UriBuilders.PROJECT_COMMANDS_RESTORE.create().build(projectName))
        .post()
        .withResourceBody(RestoreCommandOptionsDto.stringify(dto))
        .withCallback(Response.SC_CREATED, new ResponseCodeCallback() {
          @Override
          public void onResponseCode(Request request, Response response) {
            getView().hide();
            fireEvent(NotificationEvent.newBuilder().info("ProjectRestoreTask").build());
          }
        })
        .send();
  }

  public void setProjectName(String name) {
    this.projectName = name;
  }

  public interface Display extends PopupView, HasUiHandlers<ProjectRestoreModalUiHandlers> {

    void hideDialog();

    void setFileSelectorWidgetDisplay(FileSelectionPresenter.Display display);

    void showError(String messageKey);

    void clearErrors();
  }

}
