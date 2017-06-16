/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.cart;

import com.github.gwtbootstrap.client.ui.Alert;
import com.github.gwtbootstrap.client.ui.base.IconAnchor;
import com.google.common.collect.Lists;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import org.obiba.opal.web.gwt.app.client.cart.service.CartVariableItem;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.ui.OpalSimplePager;
import org.obiba.opal.web.gwt.app.client.ui.Table;
import org.obiba.opal.web.gwt.app.client.ui.TextBoxClearable;
import org.obiba.opal.web.gwt.app.client.ui.celltable.ActionHandler;

import java.util.List;

public class CartView extends ViewWithUiHandlers<CartUiHandlers> implements CartPresenter.Display {

  interface Binder extends UiBinder<Widget, CartView> {
  }

  @UiField
  Alert noVariablesAlert;

  @UiField
  Panel variableCartPanel;

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
  }

  @UiHandler("clearVariables")
  void onClearVariables(ClickEvent event) {
    getUiHandlers().onClearVariables();
  }

  @UiHandler("filter")
  public void onFilterUpdate(KeyUpEvent event) {
    renderVariables(filterVariables(filter.getText()));
  }

  @UiHandler("searchEntities")
  public void onSearchEntities(ClickEvent event) {
    getUiHandlers().onSearchEntities(variableCartTable.getSelectedItems());
  }


  @Override
  public void showVariables(List<CartVariableItem> variables) {
    initCartVariableTable();
    originalVariables = variables;
    variableCartPanel.setVisible(!originalVariables.isEmpty());
    noVariablesAlert.setVisible(originalVariables.isEmpty());
    renderVariables(originalVariables);
  }

  private void renderVariables(List<CartVariableItem> variables) {
    variableCartProvider.setList(variables);
    variableCartProvider.refresh();
    variableCartPager.setPagerVisible(variables.size() > Table.DEFAULT_PAGESIZE);
  }

  private List<CartVariableItem> filterVariables(String filterText) {
    List<CartVariableItem> variables = Lists.newArrayList();
    if (originalVariables == null || originalVariables.isEmpty()) return variables;
    for (CartVariableItem var : originalVariables) {
      // TODO multi-tokens with negation (like 'cancer -lung')
      if (var.getIdentifier().toLowerCase().contains(filterText.toLowerCase())) variables.add(var);
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
        public List<CartVariableItem> getDataList() {
          return variableCartProvider.getList();
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
