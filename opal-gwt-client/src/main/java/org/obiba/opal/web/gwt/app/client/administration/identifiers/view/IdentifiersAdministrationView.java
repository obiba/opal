package org.obiba.opal.web.gwt.app.client.administration.identifiers.view;

import org.obiba.opal.web.gwt.app.client.administration.identifiers.presenter.IdentifiersAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.identifiers.presenter.IdentifiersAdministrationUiHandlers;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.model.client.magma.TableDto;

import com.github.gwtbootstrap.client.ui.NavHeader;
import com.github.gwtbootstrap.client.ui.NavLink;
import com.github.gwtbootstrap.client.ui.base.UnorderedList;
import com.google.common.base.Strings;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

public class IdentifiersAdministrationView extends ViewWithUiHandlers<IdentifiersAdministrationUiHandlers>
    implements IdentifiersAdministrationPresenter.Display {

  interface Binder extends UiBinder<Widget, IdentifiersAdministrationView> {}

  private final Translations translations;
  @UiField
  Panel breadcrumbs;

  @UiField
  UnorderedList selector;

  @UiField
  Panel body;

  private JsArray<TableDto> identifiersTables;

  @Inject
  public IdentifiersAdministrationView(Binder uiBinder, Translations translations) {
    initWidget(uiBinder.createAndBindUi(this));
    this.translations = translations;
  }

  @Override
  public void setInSlot(Object slot, IsWidget content) {
    body.clear();
    body.add(content);
  }

  @Override
  public void showIdentifiersTables(JsArray<TableDto> identifiersTables, String entityType) {
    this.identifiersTables = identifiersTables;
    selector.clear();
    body.setVisible(identifiersTables.length() > 0);
    if (identifiersTables.length() > 0) {
      selector.add(new NavHeader(translations.identifiersTablesTitle()));
      TableSelectionHandler linkToSelect = null;
      for(int i = 0; i < identifiersTables.length(); i++) {
        TableDto table = identifiersTables.get(i);
        NavLink link = new NavLink(table.getEntityType());
        TableSelectionHandler handler = new TableSelectionHandler(i);
        link.addClickHandler(handler);
        if(i == 0) {
          linkToSelect = handler;
        }
        if(!Strings.isNullOrEmpty(entityType) && entityType.toLowerCase().equals(table.getEntityType().toLowerCase())) {
          linkToSelect = handler;
        }
        selector.add(link);
      }
      if(linkToSelect != null) {
        linkToSelect.select();
      }
    }
  }


  @Override
  public HasWidgets getBreadcrumbs() {
    return breadcrumbs;
  }

  @UiHandler("addIdTable")
  void onAddTable(ClickEvent event) {
    getUiHandlers().onAddIdentifiersTable();
  }

  private class TableSelectionHandler implements ClickHandler {

    private final int i;

    TableSelectionHandler(int i) {
      this.i = i;
    }

    @Override
    public void onClick(ClickEvent event) {
      getUiHandlers().onSelection(identifiersTables.get(i));
      for (int j = 1 ; j<selector.getWidgetCount(); j++) {
        ((NavLink)selector.getWidget(j)).setActive(false);
      }
      ((NavLink)selector.getWidget(i+1)).setActive(true);
    }

    public void select() {
      onClick(null);
    }
  }
}
