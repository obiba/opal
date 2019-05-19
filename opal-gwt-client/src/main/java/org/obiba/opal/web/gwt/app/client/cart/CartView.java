/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.cart;

import com.github.gwtbootstrap.client.ui.*;
import com.github.gwtbootstrap.client.ui.base.IconAnchor;
import com.google.common.collect.Lists;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import org.obiba.opal.web.gwt.app.client.cart.service.CartVariableItem;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.support.FilterHelper;
import org.obiba.opal.web.gwt.app.client.ui.OpalSimplePager;
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.gwt.app.client.ui.TextBoxClearable;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionHandler;
import org.obiba.opal.web.gwt.app.client.ui.celltable.CheckboxColumn;

import java.util.List;

public class CartView extends ViewWithUiHandlers<CartUiHandlers> implements CartPresenter.Display {

  interface Binder extends UiBinder<Widget, CartView> {
  }

  @UiField
  Button addToViewAll;

  @UiField
  Button searchEntitiesAll;

  @UiField
  NavLink applyAnnotationAll;

  @UiField
  NavLink deleteAnnotationAll;

  @UiField
  TextBoxClearable filter;

  @UiField
  OpalSimplePager variableCartPager;

  @UiField
  Alert selectItemTipsAlert;

  @UiField
  Alert selectAllItemsAlert;

  @UiField
  Label selectAllStatus;

  @UiField
  IconAnchor selectAllAnchor;

  @UiField
  IconAnchor clearSelectionAnchor;

  @UiField
  CartVariableTable variableCartTable;

  @UiField
  DropdownButton editAttributeAll;

  @UiField
  Dropdown editAttribute;

  private final PlaceManager placeManager;

  private final TranslationMessages translationMessages;

  private ListDataProvider<CartVariableItem> variableCartProvider;

  private List<CartVariableItem> originalVariables;

  @Inject
  public CartView(CartView.Binder uiBinder, Translations translations, PlaceManager placeManager, TranslationMessages translationMessages) {
    initWidget(uiBinder.createAndBindUi(this));
    this.placeManager = placeManager;
    this.translationMessages = translationMessages;
    filter.getTextBox().setPlaceholder(translations.filterVariables());
    editAttributeAll.setText(translations.editAnnotations());
    editAttribute.setText(translations.editAnnotations());
  }

  @UiHandler("clearVariables")
  void onClearVariables(ClickEvent event) {
    getUiHandlers().onClearVariables();
  }

  @UiHandler("filter")
  public void onFilterUpdate(KeyUpEvent event) {
    renderVariables(filterVariables(filter.getText()));
  }

  @UiHandler("searchEntitiesAll")
  public void onSearchEntitiesAll(ClickEvent event) {
    getUiHandlers().onSearchEntities(variableCartProvider.getList());
  }

  @UiHandler("searchEntities")
  public void onSearchEntities(ClickEvent event) {
    getUiHandlers().onSearchEntities(variableCartTable.getSelectedItems());
  }

  @UiHandler("addToViewAll")
  public void onAddToViewAll(ClickEvent event) {
    getUiHandlers().onAddToView(variableCartProvider.getList());
  }

  @UiHandler("addToView")
  public void onAddToView(ClickEvent event) {
    getUiHandlers().onAddToView(variableCartTable.getSelectedItems());
  }

  @UiHandler("applyAnnotationAll")
  public void onApplyAnnotationAll(ClickEvent event) {
    getUiHandlers().onApplyAnnotation(variableCartProvider.getList());
  }

  @UiHandler("applyAnnotation")
  public void onApplyAnnotation(ClickEvent event) {
    getUiHandlers().onApplyAnnotation(variableCartTable.getSelectedItems());
  }

  @UiHandler("deleteAnnotationAll")
  public void onDeleteAnnotationAll(ClickEvent event) {
    getUiHandlers().onDeleteAnnotation(variableCartProvider.getList());
  }

  @UiHandler("deleteAnnotation")
  public void onDeleteAnnotation(ClickEvent event) {
    getUiHandlers().onDeleteAnnotation(variableCartTable.getSelectedItems());
  }

  @UiHandler("removeFromCart")
  public void onRemoveFromCart(ClickEvent event) {
    getUiHandlers().onRemoveVariables(variableCartTable.getSelectedItems());
  }

  @Override
  public void showVariables(List<CartVariableItem> variables) {
    initCartVariableTable();
    originalVariables = variables;
    renderVariables(originalVariables);
    boolean hasVariables = variables.size()>0;
    addToViewAll.setEnabled(hasVariables);
    searchEntitiesAll.setEnabled(hasVariables);
    applyAnnotationAll.setDisabled(!hasVariables);
    deleteAnnotationAll.setDisabled(!hasVariables);
  }

  private void renderVariables(List<CartVariableItem> variables) {
    variableCartTable.clearSelectedItems();
    variableCartProvider.setList(variables);
    variableCartProvider.refresh();
    variableCartPager.setPagerVisible(variables.size() > Table.DEFAULT_PAGESIZE);
  }

  private List<CartVariableItem> filterVariables(String filterText) {
    List<CartVariableItem> variables = Lists.newArrayList();
    if (originalVariables == null || originalVariables.isEmpty()) return variables;
    List<String> tokens = FilterHelper.tokenize(filterText);
    for (CartVariableItem var : originalVariables) {
      if (FilterHelper.matches(var.getIdentifier(), tokens) || FilterHelper.labelMatches(var.getVariable().getAttributesArray(), tokens)) variables.add(var);
    }
    return variables;
  }

  private void initCartVariableTable() {
    if (variableCartProvider == null) {
      variableCartProvider = new ListDataProvider<CartVariableItem>();
      variableCartTable.initialize(placeManager, new CartVariableTable.CartVariableCheckDisplay() {
        @Override
        public IconAnchor getClearSelection() {
          return clearSelectionAnchor;
        }

        @Override
        public IconAnchor getSelectAll() {
          return selectAllAnchor;
        }

        @Override
        public HasText getSelectAllStatus() {
          return selectAllStatus;
        }

        @Override
        public void selectAllItems(CheckboxColumn.ItemSelectionHandler<CartVariableItem> handler) {
          for (CartVariableItem item : variableCartProvider.getList())
            handler.onItemSelection(item);
        }

        @Override
        public String getNItemLabel(int nb) {
          return translationMessages.nVariablesLabel(nb);
        }

        @Override
        public Alert getSelectActionsAlert() {
          return selectAllItemsAlert;
        }

        @Override
        public Alert getSelectTipsAlert() {
          return selectItemTipsAlert;
        }
      }, new ActionHandler<CartVariableItem>() {
        @Override
        public void doAction(CartVariableItem item, String actionName) {
          getUiHandlers().onRemoveVariable(item.getIdentifier());
        }
      });
      variableCartPager.setDisplay(variableCartTable);
      variableCartProvider.addDataDisplay(variableCartTable);
    }
  }
}
