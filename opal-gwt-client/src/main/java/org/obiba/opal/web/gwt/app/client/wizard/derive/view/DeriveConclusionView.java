/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.derive.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.wizard.DefaultWizardStepController;
import org.obiba.opal.web.gwt.app.client.wizard.Skippable;
import org.obiba.opal.web.gwt.app.client.wizard.derive.presenter.DeriveConclusionPresenter;
import org.obiba.opal.web.gwt.app.client.workbench.view.DropdownSuggestBox;
import org.obiba.opal.web.gwt.app.client.workbench.view.WizardStep;
import org.obiba.opal.web.model.client.magma.DatasourceDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.ViewImpl;

/**
 *
 */
public class DeriveConclusionView extends ViewImpl implements DeriveConclusionPresenter.Display {

  @UiTemplate("DeriveConclusionView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, DeriveConclusionView> {
  }

  private static final ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private static final Translations translations = GWT.create(Translations.class);

  @UiField
  WizardStep conclusionStep;

  @UiField
  CheckBox openEditor;

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

  private Map<String, List<String>> viewSuggestions;

  private final Widget widget;

  public DeriveConclusionView() {
    widget = uiBinder.createAndBindUi(this);
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
    return DefaultWizardStepController.Builder.create(conclusionStep, null, new Skippable() {
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
  public String getDerivedName() {
    return derivedNameBox.getText();
  }

  @Override
  public String getDatasourceName() {
    return datasourceNameBox.getItemText(datasourceNameBox.getSelectedIndex());
  }

  @Override
  public String getViewName() {
    return viewNameBox.getText();
  }

  @Override
  public boolean isOpenEditorSelected() {
    return openEditor.getValue();
  }

  @Override
  public void setDerivedNameError(boolean error) {
    if(error) {
      derivedNameInput.addStyleName("error");
    } else {
      derivedNameInput.removeStyleName("error");
    }
  }

  @Override
  public void setViewNameError(boolean error) {
    if(error) {
      viewNameInput.addStyleName("error");
    } else {
      viewNameInput.removeStyleName("error");
    }
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

}
