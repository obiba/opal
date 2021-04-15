/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.magma.datasource;

import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.FileUpload;
import com.google.common.collect.Lists;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import elemental.html.File;
import elemental.html.FileList;
import java.util.List;
import javax.inject.Inject;

import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;

public class RestoreViewsModalView extends ModalPopupViewWithUiHandlers<RestoreViewsUiHandlers> implements RestoreViewsModalPresenter.Display {

  @UiField
  FileUpload viewsFiles;

  @UiField
  Modal modal;

  @UiField
  CheckBox override;

  @Override
  public void hideDialog() {
    modal.hide();
  }

  @Override
  public boolean canOverride() {
    return override.getValue();
  }

  interface Binder extends UiBinder<Widget, RestoreViewsModalView> { }

  @Inject
  protected RestoreViewsModalView(EventBus eventBus, Binder uiBinder) {
    super(eventBus);

    initWidget(uiBinder.createAndBindUi(this));
    modal.setTitle("Restore Views");
    viewsFiles.getElement().setAttribute("multiple", "multiple");
  }

  @UiHandler("submitButton")
  public void submitButtonClick(ClickEvent event) {
    List<File> fileItems = Lists.newArrayList();
    FileList files = (FileList) viewsFiles.getElement().getPropertyObject("files");

    if (files.length() > 0) {
      for (int i = 0; i < files.length(); i++) {
        File file = files.item(i);
        if (file.getName().toLowerCase().endsWith(".json")) { // only json files
          fileItems.add(file);
        }
      }
    }

    getUiHandlers().onSubmitFiles(fileItems);
  }

  @UiHandler("cancelButton")
  public void cancelButtonClick(ClickEvent event) {
    getUiHandlers().cancel();
  }
}
