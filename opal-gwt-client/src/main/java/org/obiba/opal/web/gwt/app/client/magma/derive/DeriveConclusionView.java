/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.derive;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.ui.DropdownSuggestBox;
import org.obiba.opal.web.gwt.app.client.ui.WizardStep;
import org.obiba.opal.web.gwt.app.client.ui.wizard.DefaultWizardStepController;
import org.obiba.opal.web.gwt.app.client.ui.wizard.Skippable;
import org.obiba.opal.web.model.client.magma.DatasourceDto;

import com.github.gwtbootstrap.client.ui.Alert;
import com.github.gwtbootstrap.client.ui.ControlGroup;
import com.github.gwtbootstrap.client.ui.base.AlertBase;
import com.github.gwtbootstrap.client.ui.constants.AlertType;
import com.github.gwtbootstrap.client.ui.constants.ControlGroupType;
import com.github.gwtbootstrap.client.ui.event.ClosedEvent;
import com.github.gwtbootstrap.client.ui.event.ClosedHandler;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;

/**
 *
 */
public class DeriveConclusionView extends ViewImpl implements DeriveConclusionPresenter.Display {

  interface Binder extends UiBinder<Widget, DeriveConclusionView> {}

  private final Translations translations;

  @UiField
  WizardStep conclusionStep;

  @UiField
  TextBox derivedNameBox;

  @UiField
  FlowPanel derivedNameInput;

  @UiField
  ListBox datasourceNameBox;

  @UiField
  DropdownSuggestBox viewNameBox;

  @UiField
  FlowPanel viewNameInput;

  @UiField
  ControlGroup viewGroup;

  @UiField
  ControlGroup nameGroup;

  @UiField
  FlowPanel alerts;

  private Map<String, List<String>> viewSuggestions;

  @Inject
  public DeriveConclusionView(Binder uiBinder, Translations translations) {
    this.translations = translations;
    initWidget(uiBinder.createAndBindUi(this));
    datasourceNameBox.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        viewNameBox.getSuggestOracle().clear();
        if(viewSuggestions.containsKey(getDatasourceName())) {
          for(String viewName : viewSuggestions.get(getDatasourceName())) {
            viewNameBox.getSuggestOracle().add(viewName);
          }
        }
      }
    });
  }

  @Override
  public DefaultWizardStepController.Builder getConclusionStepBuilder(final boolean shouldSkip) {
    return DefaultWizardStepController.Builder.create(conclusionStep, new Skippable() {
      @Override
      public boolean skip() {
        return shouldSkip;
      }
    }).title(translations.saveDerivedVariable());
  }

  @Override
  public void setDefaultDerivedName(String name) {
    derivedNameBox.setText(name);
  }

  @Override
  public void addViewSuggestion(DatasourceDto ds, String viewName) {
    viewSuggestions.get(ds.getName()).add(viewName);

    if(ds.getName().equals(getDatasourceName())) {
      viewNameBox.getSuggestOracle().add(viewName);
    }
  }

  @Override
  public void populateDatasources(JsArray<DatasourceDto> datasources) {
    viewSuggestions = new HashMap<String, List<String>>();

    datasourceNameBox.clear();
    for(DatasourceDto ds : JsArrays.toIterable(datasources)) {
      viewSuggestions.put(ds.getName(), new ArrayList<String>());
      datasourceNameBox.addItem(ds.getName());
    }
    datasourceNameBox.setSelectedIndex(0);

    viewNameBox.getSuggestOracle().clear();
  }

  @Override
  public HasText getDerivedName() {
    return derivedNameBox;
  }

  @Override
  public String getDatasourceName() {
    return datasourceNameBox.getItemText(datasourceNameBox.getSelectedIndex());
  }

  @Override
  public HasText getViewName() {
    return viewNameBox;
  }

  @Override
  public void clearErrors() {
    alerts.setVisible(false);
    alerts.clear();
  }

  @Override
  public void showError(@Nullable DeriveConclusionPresenter.Display.FormField formField, String message) {
    ControlGroup group = null;
    if(formField != null) {
      switch(formField) {
        case NAME:
          group = nameGroup;
          break;
        case VIEW_NAME:
          group = viewGroup;
          break;
      }
    }

    alerts.setVisible(true);
    Alert alert = new Alert(message, AlertType.ERROR, true);

    if(group != null) {
      group.setType(ControlGroupType.ERROR);
      final ControlGroup finalGroup = group;
      alert.addClosedHandler(new ClosedHandler<AlertBase>() {
        @Override
        public void onClosed(ClosedEvent<AlertBase> event) {
          finalGroup.setType(ControlGroupType.NONE);
        }
      });
    }
    alerts.add(alert);
  }

}
