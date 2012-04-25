/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.navigator.view;

import net.customware.gwt.presenter.client.widget.WidgetDisplay;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrayDataProvider;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.navigator.presenter.VariablePresenter;
import org.obiba.opal.web.gwt.app.client.workbench.view.HorizontalTabLayout;
import org.obiba.opal.web.gwt.app.client.workbench.view.Table;
import org.obiba.opal.web.gwt.prettify.client.PrettyPrintLabel;
import org.obiba.opal.web.gwt.rest.client.authorization.HasAuthorization;
import org.obiba.opal.web.gwt.rest.client.authorization.MenuItemAuthorizer;
import org.obiba.opal.web.gwt.rest.client.authorization.TabAuthorizer;
import org.obiba.opal.web.model.client.magma.AttributeDto;
import org.obiba.opal.web.model.client.magma.CategoryDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.ViewImpl;

/**
 *
 */
public class VariableView extends ViewImpl implements VariablePresenter.Display {

  @UiTemplate("VariableView.ui.xml")
  interface VariableViewUiBinder extends UiBinder<Widget, VariableView> {
  }

  private static final Integer SCRIPT_TAB_INDEX = 2;

  private static final Integer SUMMARY_TAB_INDEX = 3;

  private static final Integer VALUES_TAB_INDEX = 4;

  private static final Integer PERMISSIONS_TAB_INDEX = 5;

  private static VariableViewUiBinder uiBinder = GWT.create(VariableViewUiBinder.class);

  private static Translations translations = GWT.create(Translations.class);

  private final Widget widget;

  @UiField
  FlowPanel toolbarPanel;

  private NavigatorMenuBar toolbar;

  @UiField
  Label variableName;

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
  HorizontalTabLayout tabs;

  @UiField
  Anchor categoryTableTitle;

  @UiField(provided = true)
  Table<CategoryDto> categoryTable;

  @UiField
  SimplePager categoryTablePager;

  JsArrayDataProvider<CategoryDto> categoryProvider = new JsArrayDataProvider<CategoryDto>();

  @UiField
  Anchor attributeTableTitle;

  @UiField(provided = true)
  AttributesTable attributeTable;

  @UiField
  SimplePager attributeTablePager;

  @UiField
  Panel permissions;

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

  private MenuBar deriveBar;

  private MenuItem categorizeItem;

  public VariableView() {
    categoryTable = new CategoriesTable();
    attributeTable = new AttributesTable();
    this.widget = uiBinder.createAndBindUi(this);
    toolbarPanel.add(toolbar = new NavigatorMenuBar());
    initCategoryTable();
    initAttributeTable();
  }

  @Override
  public void setInSlot(Object slot, Widget content) {
    HasWidgets panel = null;
    if(slot == Slots.Permissions) {
      panel = permissions;
    } else if(slot == Slots.Values) {
      panel = values;
    }
    if(panel != null) {
      panel.clear();
      if(content != null) {
        panel.add(content);
      }
    }
  }

  //
  // VariablePresenter.Display Methods
  //

  public void renderCategoryRows(JsArray<CategoryDto> rows) {
    categoryProvider.setArray(rows);
    int size = categoryProvider.getList().size();
    categoryTableTitle.setText(translations.categoriesLabel() + " (" + size + ")");
    categoryTablePager.firstPage();
    categoryTablePager.setVisible(size > NavigatorView.PAGE_SIZE);
    categoryProvider.refresh();
  }

  public void renderAttributeRows(final JsArray<AttributeDto> rows) {
    attributeProvider.setArray(rows);
    int size = attributeProvider.getList().size();
    attributeTableTitle.setText(translations.attributesLabel() + " (" + size + ")");
    attributeTablePager.firstPage();
    attributeTablePager.setVisible(size > NavigatorView.PAGE_SIZE);
    attributeTable.setupSort(attributeProvider);
    attributeProvider.refresh();
    renderVariableLabels(rows);
  }

  private void renderVariableLabels(final JsArray<AttributeDto> rows) {
    label.clear();
    for(AttributeDto attr : JsArrays.toIterable(rows)) {
      if(attr.getName().equals("label")) {
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
  public void setValuesTabCommand(final Command cmd) {
    tabs.addSelectionHandler(new SelectionHandler<Integer>() {

      @Override
      public void onSelection(SelectionEvent<Integer> event) {
        if(event.getSelectedItem() == VALUES_TAB_INDEX) {
          cmd.execute();
        }
      }
    });
  }

  @Override
  public void setSummaryTabCommand(final Command cmd) {
    tabs.addSelectionHandler(new SelectionHandler<Integer>() {

      @Override
      public void onSelection(SelectionEvent<Integer> event) {
        if(event.getSelectedItem() == SUMMARY_TAB_INDEX) {
          cmd.execute();
        }
      }
    });
  }

  @Override
  public boolean isSummaryTabSelected() {
    return tabs.getSelectedIndex() == SUMMARY_TAB_INDEX;
  }

  @Override
  public void setSummaryTabWidget(WidgetDisplay widget) {
    summary.add(widget.asWidget());
  }

  public Widget asWidget() {
    return widget;
  }

  //
  // Methods
  //

  @Override
  public void setVariableName(String name) {
    variableName.setText(name);
  }

  @Override
  public void setEntityType(String text) {
    entityType.setText(text);
  }

  @Override
  public void setReferencedEntityType(String text) {
    refEntityType.setText(text);
  }

  @Override
  public void setValueType(String text) {
    valueType.setText(text);
  }

  @Override
  public void setMimeType(String text) {
    mimeType.setText(text);
  }

  @Override
  public void setUnit(String text) {
    unit.setText(text);
  }

  @Override
  public void setRepeatable(boolean repeatable) {
    this.repeatable.setText(repeatable ? translations.yesLabel() : translations.noLabel());
  }

  @Override
  public void setOccurrenceGroup(String text) {
    occurrenceGroup.setText(text);
  }

  @Override
  public void setParentName(String name) {
    toolbar.setParentName(name);
  }

  @Override
  public void setNextName(String name) {
    toolbar.setNextName(name);
  }

  @Override
  public void setPreviousName(String name) {
    toolbar.setPreviousName(name);
  }

  @Override
  public void setParentCommand(Command cmd) {
    toolbar.setParentCommand(cmd);
  }

  @Override
  public void setNextCommand(Command cmd) {
    toolbar.setNextCommand(cmd);
  }

  @Override
  public void setPreviousCommand(Command cmd) {
    toolbar.setPreviousCommand(cmd);
  }

  @Override
  public void setEditCommand(Command cmd) {
    toolbar.setEditCommand(cmd);
  }

  @Override
  public void setDeriveCategorizeCommand(Command cmd) {
    withDeriveItem(categorizeItem = new MenuItem(translations.deriveCategorizeLabel(), cmd));
  }

  @Override
  public void setDeriveCustomCommand(Command cmd) {
    withDeriveItem(new MenuItem(translations.deriveCustomLabel(), cmd));
  }

  private void withDeriveItem(MenuItem item) {
    if(deriveBar == null) {
      toolbar.getToolsMenu().addItem(translations.deriveLabel(), deriveBar = new MenuBar(true));
    }
    deriveBar.addItem(item);
  }

  @Override
  public HasAuthorization getEditAuthorizer() {
    return new MenuItemAuthorizer(toolbar.getEditItem());
  }

  private void initCategoryTable() {
    categoryTableTitle.setText(translations.categoriesLabel());
    categoryTable.setPageSize(NavigatorView.PAGE_SIZE);
    categoryTablePager.setDisplay(categoryTable);
    categoryProvider.addDataDisplay(categoryTable);
  }

  private void initAttributeTable() {
    attributeTableTitle.setText(translations.attributesLabel());
    attributeTable.setPageSize(NavigatorView.PAGE_SIZE);
    attributeTablePager.setDisplay(attributeTable);
    attributeProvider.addDataDisplay(attributeTable);
  }

  @Override
  public void setDerivedVariable(boolean derived, String value) {
    tabs.setTabVisible(SCRIPT_TAB_INDEX, derived);
    notDerived.setVisible(!derived);
    noScript.setVisible(derived && value.length() == 0);
    script.setVisible(derived && value.length() > 0);
    script.setText(value);
    if(toolbar.getEditItem() != null) {
      toolbar.getEditItem().setVisible(derived);
    }
  }

  @Override
  public HasAuthorization getSummaryAuthorizer() {
    return new TabAuthorizer(tabs, SUMMARY_TAB_INDEX);
  }

  @Override
  public HasAuthorization getValuesAuthorizer() {
    return new TabAuthorizer(tabs, VALUES_TAB_INDEX);
  }

  @Override
  public HasAuthorization getPermissionsAuthorizer() {
    return new TabAuthorizer(tabs, PERMISSIONS_TAB_INDEX);
  }

  @Override
  public void setCategorizeMenuAvailable(boolean available) {
    if(categorizeItem != null) {
      categorizeItem.setEnabled(available);
    }
  }

}
