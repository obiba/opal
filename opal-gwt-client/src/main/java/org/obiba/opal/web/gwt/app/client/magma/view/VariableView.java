/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.magma.view;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrayDataProvider;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.magma.presenter.VariablePresenter;
import org.obiba.opal.web.gwt.app.client.magma.presenter.VariableUiHandlers;
import org.obiba.opal.web.gwt.app.client.magma.variable.view.NamespacedAttributesTable;
import org.obiba.opal.web.gwt.app.client.support.TabPanelHelper;
import org.obiba.opal.web.gwt.app.client.ui.OpalSimplePager;
import org.obiba.opal.web.gwt.app.client.ui.TabDeckPanel;
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionHandler;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.gwt.rest.client.authorization.TabPanelAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.WidgetAuthorizer;
import org.obiba.opal.web.model.client.magma.AttributeDto;
import org.obiba.opal.web.model.client.magma.CategoryDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.CodeBlock;
import com.github.gwtbootstrap.client.ui.DropdownButton;
import com.github.gwtbootstrap.client.ui.NavLink;
import com.github.gwtbootstrap.client.ui.TabPanel;
import com.github.gwtbootstrap.client.ui.TextBox;
import com.github.gwtbootstrap.client.ui.base.IconAnchor;
import com.google.common.base.Strings;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

import static org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn.EDIT_ACTION;
import static org.obiba.opal.web.gwt.app.client.ui.celltable.ActionsColumn.REMOVE_ACTION;

/**
 *
 */
public class VariableView extends ViewWithUiHandlers<VariableUiHandlers> implements VariablePresenter.Display {

  private TranslationMessages translationMessages;

  interface Binder extends UiBinder<Widget, VariableView> {}

  private static final int DICTIONARY_TAB_INDEX = 0;

  private static final int SCRIPT_TAB_INDEX = 1;

  private static final int SUMMARY_TAB_INDEX = 2;

  private static final int VALUES_TAB_INDEX = 3;

  private static final int PERMISSIONS_TAB_INDEX = 4;

  @UiField
  Label name;

  @UiField
  Label entityType;

  @UiField
  Label refEntityType;

  @UiField
  Label valueType;

  @UiField
  Label mimeType;

  @UiField
  Label unit;

  @UiField
  Label repeatable;

  @UiField
  Label occurrenceGroup;

  @UiField
  FlowPanel historyPanel;

  @UiField
  FlowPanel scriptHeaderPanel;

  @UiField
  TabDeckPanel scriptNavPanel;

  @UiField
  NavLink backToScript;

  @UiField
  Panel scriptControls;

  @UiField
  TabPanel tabPanel;

  @UiField
  CategoriesTable categoryTable;

  @UiField
  OpalSimplePager categoryTablePager;

  JsArrayDataProvider<CategoryDto> categoryProvider = new JsArrayDataProvider<CategoryDto>();

  @UiField
  FlowPanel attributesPanel;

  @UiField
  Panel summaryPanel;

  @UiField
  SimplePanel values;

  @UiField
  CodeBlock script;

  @UiField
  Button previous;

  @UiField
  Button next;

  @UiField
  Button remove;

  @UiField
  Button editScript;

  @UiField
  Button historyScript;

  @UiField
  SimplePanel scriptEditor;

  @UiField
  NavLink addToView;

  @UiField
  NavLink categorizeToAnother;

  @UiField
  NavLink categorizeToThis;

  @UiField
  NavLink deriveCustom;

  @UiField
  Button editCategories;

  @UiField
  DropdownButton addAttributeButton;

  @UiField
  IconAnchor editProperties;

  @UiField
  TextBox comment;

  @UiField
  Panel permissionsPanel;

  @UiField
  DropdownButton deriveBtn;

  private final Translations translations;

  @Inject
  public VariableView(Binder uiBinder, Translations translations, TranslationMessages translationMessages) {
    this.translations = translations;
    this.translationMessages = translationMessages;

    initWidget(uiBinder.createAndBindUi(this));

    deriveBtn.setText(translations.derive());
    addAttributeButton.setText(translations.addAttribute());
    initCategoryTable();
    scriptNavPanel.showWidget(0);
  }

  @UiHandler("tabPanel")
  void onShown(TabPanel.ShownEvent shownEvent) {
    if(shownEvent.getTarget() == null) return;

    switch(tabPanel.getSelectedTab()) {
      case SUMMARY_TAB_INDEX:
        getUiHandlers().onShowSummary();
        break;
      case VALUES_TAB_INDEX:
        getUiHandlers().onShowValues();
        break;
    }
  }

  @SuppressWarnings("PMD.NcssMethodCount")
  @Override
  public void setInSlot(Object slot, IsWidget content) {
    HasWidgets panel = null;
    switch((Slots) slot) {
      case Values:
        panel = values;
        break;
      case ScriptEditor:
        panel = scriptEditor;
        break;
      case History:
        panel = historyPanel;
        break;
      case Permissions:
        panel = permissionsPanel;
        break;
      case Summary:
        panel = summaryPanel;
        break;
    }
    if(panel != null) {
      panel.clear();
      if(content != null) {
        panel.add(content.asWidget());
      }
    }
  }

  @Override
  public void goToEditScript() {
    backToScript.setVisible(true);
    scriptControls.setVisible(false);
    scriptNavPanel.showWidget(1);
  }

  @Override
  public void backToViewScript() {
    scriptNavPanel.showWidget(0);
    updateScriptNavPanel(0);
  }

  @UiHandler("backToScript")
  public void onBackToScript(ClickEvent event) {
    backToViewScript();
  }

  @UiHandler("editScript")
  public void onEditScript(ClickEvent event) {
    scriptNavPanel.showWidget(1);
    updateScriptNavPanel(1);
  }

  @UiHandler("saveScript")
  public void onSaveScript(ClickEvent event) {
    getUiHandlers().onSaveScript();
  }

  @UiHandler("cancelEditScript")
  public void onCancelEditScript(ClickEvent event) {
    backToViewScript();
  }

  @UiHandler("historyScript")
  public void onHistoryScript(ClickEvent event) {
    scriptNavPanel.showWidget(2);
    updateScriptNavPanel(2);
  }

  @UiHandler("addToView")
  void onAddToView(ClickEvent event) {
    getUiHandlers().onAddToView();
  }

  @UiHandler("categorizeToAnother")
  void onCategorizeToAnother(ClickEvent event) {
    getUiHandlers().onCategorizeToAnother();
  }

  @UiHandler("categorizeToThis")
  void onCategorizeToThis(ClickEvent event) {
    getUiHandlers().onCategorizeToThis();
  }

  @UiHandler("deriveCustom")
  void onDeriveCustom(ClickEvent event) {
    getUiHandlers().onDeriveCustom();
  }

  @UiHandler("next")
  void onNext(ClickEvent event) {
    getUiHandlers().onNextVariable();
  }

  @UiHandler("previous")
  void onPrevious(ClickEvent event) {
    getUiHandlers().onPreviousVariable();
  }

  @UiHandler("editCategories")
  void onEditCategories(ClickEvent event) {
    getUiHandlers().onEditCategories();
  }

  @UiHandler("addAttribute")
  void onAddAttribute(ClickEvent event) {
    getUiHandlers().onAddAttribute();
  }

  @UiHandler("addTaxonomy")
  void onAddTaxonomy(ClickEvent event) {
    getUiHandlers().onAddTaxonomy();
  }

  @UiHandler("editProperties")
  void onEditProperties(ClickEvent event) {
    getUiHandlers().onEditProperties();
  }

  @UiHandler("remove")
  void onRemove(ClickEvent event) {
    getUiHandlers().onRemove();
  }

  //
  // VariablePresenter.Display Methods
  //

  @Override
  public void renderCategoryRows(JsArray<CategoryDto> rows) {
    categoryProvider.setArray(rows);
    categoryTablePager.firstPage();
    categoryTablePager.setPagerVisible(categoryProvider.getList().size() > Table.DEFAULT_PAGESIZE);
    categoryProvider.refresh();
  }

  @Override
  public String getComment() {
    String commentText = comment.getText();
    String placeholder = comment.getPlaceholder();
    return Strings.isNullOrEmpty(commentText) ? placeholder : commentText;
  }

  @Override
  public boolean isSummaryTabSelected() {
    return tabPanel.getSelectedTab() == SUMMARY_TAB_INDEX;
  }

  @Override
  public boolean isValuesTabSelected() {
    return tabPanel.getSelectedTab() == VALUES_TAB_INDEX;
  }

  @Override
  public void setVariable(VariableDto variable) {
    name.setText(variable.getName());
    entityType.setText(variable.getEntityType());
    refEntityType.setText(variable.getReferencedEntityType());
    valueType.setText(variable.getValueType());
    mimeType.setText(variable.hasMimeType() ? variable.getMimeType() : "");
    unit.setText(variable.hasUnit() ? variable.getUnit() : "");
    repeatable.setText(variable.getIsRepeatable() ? translations.yesLabel() : translations.noLabel());
    occurrenceGroup.setText(variable.getIsRepeatable() ? variable.getOccurrenceGroup() : "");

    updateComment(variable.getName());
    updateScriptNavPanel(scriptNavPanel.getVisibleWidget());
  }

  @Override
  public HasAuthorization getVariableAttributesAuthorizer(VariableDto variableDto) {
    return new NamespacedAttributesTableAuthorization(variableDto);
  }

  //
  // Methods
  //

  private void updateComment(String variableName) {
    comment.setPlaceholder(translations.scriptUpdateDefaultPrefixLabel() + " " + variableName);
  }

  private void updateScriptNavPanel(int selectedIndex) {
    switch(ScriptNavPanels.values()[selectedIndex]) {
      case VIEW:
        backToScript.setVisible(false);
        scriptControls.setVisible(true);
        break;
      case EDIT:
        backToScript.setVisible(true);
        scriptControls.setVisible(false);
        getUiHandlers().onEditScript();
        break;
      case HISTORY:
        backToScript.setVisible(true);
        scriptControls.setVisible(false);
        getUiHandlers().onHistory();
        break;
    }
  }

  @Override
  public void setNextName(String name) {
    next.setTitle(name);
    next.setEnabled(!Strings.isNullOrEmpty(name));
  }

  @Override
  public void setPreviousName(String name) {
    previous.setTitle(name);
    previous.setEnabled(!Strings.isNullOrEmpty(name));
  }

  @Override
  public void setDeriveFromMenuVisibility(boolean visible) {
    categorizeToThis.setVisible(visible);
  }

  @Override
  public void resetTabs() {
    TabPanelHelper.setTabActive(tabPanel, tabPanel.getSelectedTab(), false);
    TabPanelHelper.setTabActive(tabPanel, DICTIONARY_TAB_INDEX, true);
  }

  @Override
  public HasAuthorization getEditAuthorizer() {
    return new WidgetAuthorizer(remove, scriptHeaderPanel, editProperties, editCategories, addAttributeButton);
  }

  @Override
  public HasAuthorization getPermissionsAuthorizer() {
    return new TabPanelAuthorizer(tabPanel, PERMISSIONS_TAB_INDEX);
  }

  private void initCategoryTable() {
    categoryTable.setPageSize(Table.DEFAULT_PAGESIZE);
    categoryTablePager.setDisplay(categoryTable);
    categoryProvider.addDataDisplay(categoryTable);
  }

  @Override
  public void setDerivedVariable(boolean derived, String value) {
    TabPanelHelper.setTabVisible(tabPanel, SCRIPT_TAB_INDEX, derived);
    comment.setText("");
    scriptHeaderPanel.setVisible(derived);
    script.setVisible(derived && !value.isEmpty());
    script.setText(value);
  }

  @Override
  public HasAuthorization getSummaryAuthorizer() {
    return new TabPanelAuthorizer(tabPanel, SUMMARY_TAB_INDEX);
  }

  @Override
  public HasAuthorization getValuesAuthorizer() {
    return new TabPanelAuthorizer(tabPanel, VALUES_TAB_INDEX);
  }

  @Override
  public void setCategorizeMenuAvailable(boolean available) {
    categorizeToAnother.setDisabled(!available);
    categorizeToThis.setDisabled(!available);
    deriveCustom.setDisabled(!available);
  }

  private enum ScriptNavPanels {
    VIEW,
    EDIT,
    HISTORY
  }

  private class NamespacedAttributesTableAuthorization implements HasAuthorization {
    private VariableDto variableDto;

    private List<NamespacedAttributesTable> attributesTables = new ArrayList<NamespacedAttributesTable>();

    private NamespacedAttributesTableAuthorization(VariableDto variableDto) {

      this.variableDto = variableDto;
    }

    @Override
    public void beforeAuthorization() {
      attributesPanel.clear();

      if (variableDto.getAttributesArray() == null || variableDto.getAttributesArray().length() == 0) {
        attributesPanel.setStyleName("xxlarge-bottom-margin");
      } else {
        attributesPanel.removeStyleName("xxlarge-bottom-margin");
      }

      List<String> namespaces = new ArrayList<String>();
      JsArray<AttributeDto> attributesArray = JsArrays.toSafeArray(variableDto.getAttributesArray());
      for(int i = 0; i < attributesArray.length(); i++) {
        String namespace = attributesArray.get(i).getNamespace();
        if(!namespaces.contains(namespace)) {
          namespaces.add(namespace);
        }
      }

      Collections.sort(namespaces);
      for(String namespace : namespaces) {
        NamespacedAttributesTable child = new NamespacedAttributesTable(attributesArray, namespace,
            translationMessages);
        child.setUiHandlers(getUiHandlers());
        attributesTables.add(child);
      }
    }

    @Override
    public void authorized() {
      for(NamespacedAttributesTable attributesTable : attributesTables) {
        attributesTable.addEditableColumns();
        attributesTable.getActions().setActionHandler(new ActionHandler<JsArray<AttributeDto>>() {
          @Override
          public void doAction(JsArray<AttributeDto> object, String actionName) {
            ArrayList<JsArray<AttributeDto>> selectedItems = new ArrayList<JsArray<AttributeDto>>();
            selectedItems.add(object);

            if(actionName.equalsIgnoreCase(REMOVE_ACTION)) {
              getUiHandlers().onDeleteAttribute(selectedItems);
            } else if(actionName.equalsIgnoreCase(EDIT_ACTION)) {
              getUiHandlers().onEditAttributes(selectedItems);
            }
          }
        });
        attributesPanel.add(attributesTable);
      }
    }

    @Override
    public void unauthorized() {
      for(NamespacedAttributesTable attributesTable : attributesTables) {
        attributesPanel.add(attributesTable);
      }
    }
  }
}
