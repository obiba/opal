/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.project.view;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.project.presenter.ProjectAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.project.presenter.ProjectEditionUiHandlers;
import org.obiba.opal.web.gwt.app.client.ui.EditorPanel;
import org.obiba.opal.web.model.client.opal.DatabaseDto;
import org.obiba.opal.web.model.client.opal.ProjectDto;

import com.github.gwtbootstrap.client.ui.Paragraph;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class ProjectAdministrationView extends ViewWithUiHandlers<ProjectEditionUiHandlers>
    implements ProjectAdministrationPresenter.Display {

  interface Binder extends UiBinder<Widget, ProjectAdministrationView> {}

  @UiField
  Paragraph database;

  @UiField
  ListBox databases;

  @UiField
  EditorPanel storageEditor;

  private ProjectDto project;

  @Inject
  public ProjectAdministrationView(Binder uiBinder, Translations translations) {
    initWidget(uiBinder.createAndBindUi(this));
    for(int i = 0; i < databases.getItemCount(); i++) {
      databases.setItemText(i, translations.datasourceTypeMap().get(databases.getValue(i)));
    }
    storageEditor.setHandler(new StorageEditorHandler());
  }

  @Override
  public void setProject(ProjectDto project) {
    this.project = project;
    database.setText(project.getDatabase());
    storageEditor.showEditor(false);
  }

  @Override
  public ProjectDto getProject() {
    return project;
  }

  @Override
  public void setAvailableDatabases(JsArray<DatabaseDto> dtos) {
    for(DatabaseDto dto : JsArrays.toIterable(dtos)) {
      databases.addItem(dto.getName());
    }
  }

  @UiHandler("deleteProject")
  void onDeleteProject(ClickEvent event) {
    getUiHandlers().delete();
  }

  private class StorageEditorHandler implements EditorPanel.Handler {

    @Override
    public void onEdit() {
      for(int i = 0; i < databases.getItemCount(); i++) {
        if(databases.getValue(i).equals(project.getDatabase())) {
          databases.setSelectedIndex(i);
          break;
        }
      }
    }

    @Override
    public void onSave() {
      getUiHandlers().saveStorage(databases.getValue(databases.getSelectedIndex()));
    }

    @Override
    public void onCancel() {
      // TODO
    }

    @Override
    public void onHistory() {
      // not applicable
    }
  }
}
