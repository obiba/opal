/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.configureview.view;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrayDataProvider;
import org.obiba.opal.web.gwt.app.client.navigator.view.AttributesTable;
import org.obiba.opal.web.gwt.app.client.navigator.view.NavigatorView;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.presenter.AttributesPresenter;
import org.obiba.opal.web.model.client.magma.AttributeDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.ViewImpl;

/**
 *
 */
public class AttributesView extends ViewImpl implements AttributesPresenter.Display {

  @UiTemplate("AttributesView.ui.xml")
  interface MyUiBinder extends UiBinder<Widget, AttributesView> {

  }

  private static final MyUiBinder uiBinder = GWT.create(MyUiBinder.class);

  private static final Translations translations = GWT.create(Translations.class);

  private final Widget widget;

  @UiField(provided = true)
  AttributesTable attributeTable;

  @UiField
  SimplePager attributeTablePager;

  @UiField
  Button addAttributeButton;

  private final JsArrayDataProvider<AttributeDto> attributeProvider = new JsArrayDataProvider<AttributeDto>();

  private ActionHandler<AttributeDto> editAttributeActionHandler;

  private ActionHandler<AttributeDto> deleteAttributeActionHandler;

  public AttributesView() {
    attributeTable = new AttributesTable();
    widget = uiBinder.createAndBindUi(this);
    initAttributeTable();
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  private void initAttributeTable() {
    ActionsColumn<AttributeDto> actionsColumn = new ActionsColumn<AttributeDto>(ActionsColumn.EDIT_ACTION,
        ActionsColumn.DELETE_ACTION);
    actionsColumn.setActionHandler(new ActionHandler<AttributeDto>() {
      @Override
      public void doAction(AttributeDto attributeDto, String actionName) {
        if(ActionsColumn.EDIT_ACTION.equals(actionName)) {
          editAttributeActionHandler.doAction(attributeDto, actionName);
        } else if(ActionsColumn.DELETE_ACTION.equals(actionName)) {
          deleteAttributeActionHandler.doAction(attributeDto, actionName);
        }
      }
    });
    attributeTable.addColumn(actionsColumn, translations.actionsLabel());
    attributeTable.setPageSize(NavigatorView.PAGE_SIZE);

    attributeTablePager.setDisplay(attributeTable);
    attributeProvider.addDataDisplay(attributeTable);
  }

  @Override
  public void setEditAttributeActionHandler(ActionHandler<AttributeDto> editAttributeActionHandler) {
    this.editAttributeActionHandler = editAttributeActionHandler;
  }

  @Override
  public void setDeleteAttributeActionHandler(ActionHandler<AttributeDto> deleteAttributeActionHandler) {
    this.deleteAttributeActionHandler = deleteAttributeActionHandler;
  }

  @Override
  public void renderAttributeRows(JsArray<AttributeDto> attributes) {
    attributeProvider.setArray(attributes);
    attributeTablePager.firstPage();
    attributeTablePager.setVisible(attributeProvider.getList().size() > attributeTable.getPageSize());
    attributeTable.setupSort(attributeProvider);
    attributeProvider.refresh();
  }

  @Override
  public HandlerRegistration addAddAttributeHandler(ClickHandler addAttributeHandler) {
    return addAttributeButton.addClickHandler(addAttributeHandler);
  }

  @Override
  public void formEnable(boolean enabled) {
    addAttributeButton.setEnabled(enabled);
  }
}
