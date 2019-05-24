/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.importdata.view;

import com.google.common.base.Strings;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.magma.importdata.presenter.IdentifiersMappingSelectionStepPresenter;
import org.obiba.opal.web.gwt.app.client.ui.Chooser;
import org.obiba.opal.web.gwt.app.client.ui.NumericTextBox;
import org.obiba.opal.web.model.client.identifiers.IdentifiersMappingDto;

import com.github.gwtbootstrap.client.ui.CheckBox;
import com.github.gwtbootstrap.client.ui.RadioButton;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;
import org.obiba.opal.web.model.client.opal.ProjectDto;

public class IdentifiersMappingSelectionStepView extends ViewImpl implements IdentifiersMappingSelectionStepPresenter.Display {

  interface Binder extends UiBinder<Widget, IdentifiersMappingSelectionStepView> {}

  private final Translations translations;

  @UiField
  CheckBox incremental;

  @UiField
  NumericTextBox limit;

  @UiField
  Panel idMappingOptions;

  @UiField
  RadioButton mappingRequired;

  @UiField
  RadioButton ignoreUnmapped;

  @UiField
  RadioButton generateForUnmapped;

  @UiField
  Panel identifiersPanel;

  @UiField
  Chooser identifiers;

  @Inject
  public IdentifiersMappingSelectionStepView(Binder uiBinder, Translations translations) {
    initWidget(uiBinder.createAndBindUi(this));
    this.translations = translations;
  }

  @Override
  public void setIdentifiersMappings(JsArray<IdentifiersMappingDto> mappings) {
    identifiers.clear();
    identifiers.addItem(translations.opalDefaultIdentifiersLabel());
    for(int i = 0; i < mappings.length(); i++) {
      identifiers.addItem(mappings.get(i).getName());
    }
    identifiers.setSelectedIndex(0);
    identifiersPanel.setVisible(mappings.length() > 0);
  }

  @Override
  public void selectIdentifiersMapping(ProjectDto.IdentifiersMappingDto mapping) {
    if (Strings.isNullOrEmpty(mapping.getMapping())) {
      if (identifiers.getItemCount() > 0) identifiers.setSelectedIndex(0);
    } else {
      identifiers.setSelectedValue(mapping.getMapping());
      idMappingOptions.setVisible(identifiers.getSelectedIndex()>0);
    }
  }

  @Override
  public String getSelectedIdentifiersMapping() {
    return identifiers.getSelectedIndex() == 0 ? null : identifiers.getSelectedValue();
  }

  @Override
  public boolean isIncremental() {
    return incremental.getValue();
  }

  @Override
  public Integer getLimit() {
    if(!limit.isEnabled()) return null;

    Long value = limit.getNumberValue();
    return value == null ? null : value.intValue();
  }

  @Override
  public boolean allowIdentifierGeneration() {
    return identifiers.getSelectedIndex() > 0 && !mappingRequired.getValue() && generateForUnmapped.getValue();
  }

  @Override
  public boolean ignoreUnknownIdentifier() {
    return identifiers.getSelectedIndex() > 0 && !mappingRequired.getValue();
  }

  @UiHandler("limitCheck")
  public void onLimitCheck(ValueChangeEvent<Boolean> event) {
    limit.setEnabled(event.getValue());
  }

  @UiHandler("identifiers")
  public void onIdentifiersMappingChange(ChangeEvent event) {
    idMappingOptions.setVisible(identifiers.getSelectedIndex()>0);
    mappingRequired.setValue(true);
  }

}
