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

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrayDataProvider;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.magma.presenter.VariablePresenter;
import org.obiba.opal.web.gwt.app.client.magma.presenter.VariableUiHandlers;
import org.obiba.opal.web.gwt.app.client.support.TabPanelHelper;
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.gwt.prettify.client.PrettyPrintLabel;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.gwt.rest.client.authorization.TabPanelAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.WidgetAuthorizer;
import org.obiba.opal.web.model.client.magma.AttributeDto;
import org.obiba.opal.web.model.client.magma.CategoryDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.github.gwtbootstrap.client.ui.Button;
import com.github.gwtbootstrap.client.ui.NavLink;
import com.github.gwtbootstrap.client.ui.SimplePager;
import com.github.gwtbootstrap.client.ui.TabPanel;
import com.github.gwtbootstrap.client.ui.base.InlineLabel;
import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
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
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

/**
 *
 */
public class VariableView extends ViewWithUiHandlers<VariableUiHandlers> implements VariablePresenter.Display {

  interface Binder extends UiBinder<Widget, VariableView> {}

  private static final int CATEGORIES_TAB_INDEX = 0;

  private static final int ATTRIBUTES_TAB_INDEX = 1;

  private static final int SCRIPT_TAB_INDEX = 2;

  private static final int SUMMARY_TAB_INDEX = 3;

  private static final int VALUES_TAB_INDEX = 4;

  private static final Translations translations = GWT.create(Translations.class);

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
  FlowPanel label;

  @UiField
  TabPanel tabPanel;

  @UiField(provided = true)
  Table<CategoryDto> categoryTable;

  @UiField
  SimplePager categoryTablePager;

  JsArrayDataProvider<CategoryDto> categoryProvider = new JsArrayDataProvider<CategoryDto>();

  @UiField(provided = true)
  AttributesTable attributeTable;

  @UiField
  SimplePager attributeTablePager;

  JsArrayDataProvider<AttributeDto> attributeProvider = new JsArrayDataProvider<AttributeDto>();

  @UiField
  Panel summary;

  @UiField
  Panel values;

  @UiField
  PrettyPrintLabel script;

  @UiField
  InlineLabel noScript;

  @UiField
  InlineLabel notDerived;

  @UiField
  Button previous;

  @UiField
  Button next;

  @UiField
  Button edit;

  @UiField
  NavLink addToView;

  @UiField
  NavLink categorizeToAnother;

  @UiField
  NavLink categorizeToThis;

  @UiField
  NavLink deriveCustom;

  @Inject
  public VariableView(Binder uiBinder) {
    categoryTable = new CategoriesTable();
    attributeTable = new AttributesTable();
    initWidget(uiBinder.createAndBindUi(this));
    initCategoryTable();
    initAttributeTable();
    tabPanel.addShownHandler(new TabPanel.ShownEvent.Handler() {
      @Override
      public void onShow(TabPanel.ShownEvent event) {
        if(tabPanel.getSelectedTab() == SUMMARY_TAB_INDEX) {
          getUiHandlers().onShowSummary();
        }
        if(tabPanel.getSelectedTab() == VALUES_TAB_INDEX) {
          getUiHandlers().onShowValues();
        }
      }
    });
  }

  @Override
  public void setInSlot(Object slot, IsWidget content) {
    HasWidgets panel = null;
    if(slot == Slots.Values) {
      panel = values;
    }
    if(panel != null) {
      panel.clear();
      if(content != null) {
        panel.add(content.asWidget());
      }
    }
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

  @UiHandler("edit")
  void onEdit(ClickEvent event) {
    getUiHandlers().onEdit();
  }

  //
  // VariablePresenter.Display Methods
  //

  @Override
  public void renderCategoryRows(JsArray<CategoryDto> rows) {
    categoryProvider.setArray(rows);
    int size = categoryProvider.getList().size();
    //TabPanelHelper.setTabText(tabPanel, CATEGORIES_TAB_INDEX, translations.categoriesLabel() + " (" + size + ")");
    categoryTablePager.firstPage();
    categoryTablePager.setVisible(size > Table.DEFAULT_PAGESIZE);
    categoryProvider.refresh();
  }

  @Override
  public void renderAttributeRows(JsArray<AttributeDto> rows) {
    attributeProvider.setArray(rows);
    int size = attributeProvider.getList().size();
    //TabPanelHelper.setTabText(tabPanel, ATTRIBUTES_TAB_INDEX, translations.attributesLabel() + " (" + size + ")");
    attributeTablePager.firstPage();
    attributeTablePager.setVisible(size > Table.DEFAULT_PAGESIZE);
    attributeTable.setupSort(attributeProvider);
    attributeProvider.refresh();
    renderVariableLabels(rows);
  }

  private void renderVariableLabels(JsArray<AttributeDto> rows) {
    label.clear();
    for(AttributeDto attr : JsArrays.toIterable(rows)) {
      if("label".equals(attr.getName())) {
        renderVariableLabel(attr);
      }
    }
  }

  private void renderVariableLabel(AttributeDto attr) {
    if(attr.hasValue() && attr.getValue().trim().length() > 0) {
      FlowPanel item = new FlowPanel();
      if(attr.hasLocale() && attr.getLocale().trim().length() > 0) {
        InlineLabel lang = new InlineLabel(attr.getLocale());
        lang.setStyleName("label");
        item.add(lang);
      }
      InlineLabel value = new InlineLabel(attr.getValue());
      value.addStyleName("xsmall-indent");
      item.add(value);
      label.add(item);
    }
  }

  @Override
  public boolean isSummaryTabSelected() {
    return tabPanel.getSelectedTab() == SUMMARY_TAB_INDEX;
  }

  @Override
  public void setSummaryTabWidget(View widget) {
    summary.add(widget.asWidget());
  }

  //
  // Methods
  //

  @Override
  public void setVariable(VariableDto variable) {
    entityType.setText(variable.getEntityType());
    refEntityType.setText(variable.getReferencedEntityType());
    valueType.setText(variable.getValueType());
    mimeType.setText(variable.hasMimeType() ? variable.getMimeType() : "");
    unit.setText(variable.hasUnit() ? variable.getUnit() : "");
    repeatable.setText(variable.getIsRepeatable() ? translations.yesLabel() : translations.noLabel());
    occurrenceGroup.setText(variable.getIsRepeatable() ? variable.getOccurrenceGroup() : "");
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
  public HasAuthorization getEditAuthorizer() {
    return new WidgetAuthorizer(edit);
  }

  private void initCategoryTable() {
    TabPanelHelper.setTabText(tabPanel, CATEGORIES_TAB_INDEX, translations.categoriesLabel());
    categoryTable.setPageSize(Table.DEFAULT_PAGESIZE);
    categoryTablePager.setDisplay(categoryTable);
    categoryProvider.addDataDisplay(categoryTable);
  }

  private void initAttributeTable() {
    TabPanelHelper.setTabText(tabPanel, ATTRIBUTES_TAB_INDEX, translations.attributesLabel());
    attributeTable.setPageSize(Table.DEFAULT_PAGESIZE);
    attributeTablePager.setDisplay(attributeTable);
    attributeProvider.addDataDisplay(attributeTable);
  }

  @Override
  public void setDerivedVariable(boolean derived, String value) {
    TabPanelHelper.setTabVisible(tabPanel, SCRIPT_TAB_INDEX, derived);
    notDerived.setVisible(!derived);
    noScript.setVisible(derived && value.isEmpty());
    script.setVisible(derived && value.length() > 0);
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

}
