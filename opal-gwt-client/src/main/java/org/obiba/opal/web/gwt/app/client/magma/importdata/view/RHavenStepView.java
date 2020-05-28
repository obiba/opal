/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.importdata.view;

import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.base.HasType;
import com.github.gwtbootstrap.client.ui.constants.ControlGroupType;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;
import org.obiba.opal.web.gwt.app.client.fs.presenter.FileSelectionPresenter.Display;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.magma.importdata.ImportConfig;
import org.obiba.opal.web.gwt.app.client.magma.importdata.presenter.RHavenStepPresenter;
import org.obiba.opal.web.gwt.app.client.support.LanguageLocale;
import org.obiba.opal.web.gwt.app.client.ui.CharacterSetView;
import org.obiba.opal.web.gwt.app.client.ui.DropdownSuggestBox;

public class RHavenStepView extends ViewImpl implements RHavenStepPresenter.Display {

  interface Binder extends UiBinder<Widget, RHavenStepView> {}

  private Display fileSelection;

  private Display additionalFileSelection;

  private final Translations translations;

  @UiField
  SimplePanel selectFilePanel;

  @UiField
  SimplePanel selectAdditionalFilePanel;

  @UiField
  TextBox idColumn;

  @UiField
  CharacterSetView charsetView;

  @UiField
  Label selectFileHelp;

  @UiField
  TextBox entityType;

  @UiField
  DropdownSuggestBox localeNameBox;

  @UiField
  ControlGroup selectFileGroup;

  @UiField
  ControlGroup selectAdditionalFileGroup;

  @UiField
  ControlGroup localeGroup;

  @Inject
  public RHavenStepView(Binder uiBinder, Translations translations) {
    this.translations = translations;
    initWidget(uiBinder.createAndBindUi(this));
    initializeLocales();
  }

  private void initializeLocales() {
    localeNameBox.getSuggestOracle().clear();

    for(String locale : LanguageLocale.getAllLocales()) {
      localeNameBox.getSuggestOracle().add(locale);
    }

    localeNameBox.setText(LanguageLocale.EN.getName());
  }

  @Override
  public void setImportFormat(ImportConfig.ImportFormat importFormat) {
    selectAdditionalFileGroup.setVisible(additionalFileSelection != null && ImportConfig.ImportFormat.RSAS.equals(importFormat));
    switch(importFormat) {
      case RSAS:
        selectFileHelp.setText(translations.rSASHelp());
        break;
      case RXPT:
        selectFileHelp.setText(translations.rXPTHelp());
        break;
      case RSPSS:
        selectFileHelp.setText(translations.rSPSSHelp());
        break;
      case RZSPSS:
        selectFileHelp.setText(translations.rZSPSSHelp());
        break;
      case RSTATA:
        selectFileHelp.setText(translations.rStataHelp());
        break;
        default:
          selectFileHelp.setText("");
    }
  }

  @Override
  public void setFileSelectorWidgetDisplays(Display display, Display additionalDisplay) {
    selectFilePanel.setWidget(display.asWidget());
    fileSelection = display;
    fileSelection.setFieldWidth("20em");

    if (additionalDisplay != null) {
      selectFilePanel.setWidget(additionalDisplay.asWidget());
      additionalFileSelection = additionalDisplay;
      additionalFileSelection.setFieldWidth("20em");
    }
  }

  @Override
  public String getSelectedFile() {
    return fileSelection.getFile();
  }

  @Override
  public String getSelectedCategoryFile() {
    return additionalFileSelection == null ? null : additionalFileSelection.getFile();
  }

  @Override
  public HasText getCharsetText() {
    return charsetView.getCharsetText();
  }

  @Override
  public void setDefaultCharset(String defaultCharset) {
    charsetView.setDefaultCharset(defaultCharset);
  }

  @Override
  public TextBox getIdColumn() {
    return idColumn;
  }

  @Override
  public HasText getEntityType() {
    return entityType;
  }

  @Override
  public String getLocale() {
    return localeNameBox.getText();
  }

  @Override
  public HasType<ControlGroupType> getGroupType(String id) {
    FormField field = FormField.valueOf(id);
    switch(field) {
      case FILE:
        return selectFileGroup;
      case LOCALE:
        return localeGroup;
    }

    return null;
  }
}

