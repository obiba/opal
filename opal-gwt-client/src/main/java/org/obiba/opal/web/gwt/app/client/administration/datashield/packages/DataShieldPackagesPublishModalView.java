/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.administration.datashield.packages;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.Paragraph;
import com.google.common.collect.Lists;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.EventBus;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.ui.Modal;
import org.obiba.opal.web.gwt.app.client.ui.ModalPopupViewWithUiHandlers;
import org.obiba.opal.web.model.client.datashield.DataShieldProfileDto;

import javax.inject.Inject;
import java.util.List;

public class DataShieldPackagesPublishModalView extends ModalPopupViewWithUiHandlers<DataShieldPackagesPublishModalUiHandlers>
    implements DataShieldPackagesPublishModalPresenter.Display {

  interface Binder extends UiBinder<Widget, DataShieldPackagesPublishModalView> {
  }

  private final Translations translations;

  private final TranslationMessages translationMessages;

  @UiField
  Modal dialog;

  @UiField
  Paragraph selectMessage;

  @UiField
  Panel packagesPanel;

  @UiField
  Button publishButton;

  @UiField
  Button cancelButton;

  private List<CheckBox> packageCheckboxes = Lists.newArrayList();

  @Inject
  public DataShieldPackagesPublishModalView(EventBus eventBus, Binder uiBinder, Translations translations, TranslationMessages translationMessages) {
    super(eventBus);
    this.translations = translations;
    this.translationMessages = translationMessages;
    initWidget(uiBinder.createAndBindUi(this));
  }

  @Override
  public void renderPackages(List<String> packages, DataShieldProfileDto profile) {
    selectMessage.setText(translationMessages.selectPackagesToInitProfile(profile.getName()));
    packageCheckboxes.clear();
    packagesPanel.clear();
    for (String pkg : packages) {
      CheckBox box = new CheckBox();
      box.setText(pkg);
      box.setValue(true);
      packageCheckboxes.add(box);
      packagesPanel.add(box);
    }
  }

  @Override
  public void hideDialog() {
    dialog.hide();
  }

  @Override
  public void setInProgress(boolean progress) {
    dialog.setBusy(progress);
    publishButton.setEnabled(!progress);
    cancelButton.setEnabled(!progress);
  }

  @Override
  public void onShow() {
    dialog.setTitle(translations.dataShieldProfileInitTitle());
  }

  @UiHandler("publishButton")
  public void onPublishButton(ClickEvent event) {
    List<String> names = Lists.newArrayList();
    for (CheckBox box : packageCheckboxes) {
      if (box.getValue())
        names.add(box.getText());
    }
    getUiHandlers().publishPackages(names);
  }

  @UiHandler("cancelButton")
  public void onCancelButton(ClickEvent event) {
    hide();
  }

}
