/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.authz.view;

import static org.obiba.opal.web.gwt.app.client.widgets.celltable.ActionsColumn.DELETE_ACTION;

import org.obiba.opal.web.gwt.app.client.authz.presenter.SubjectAuthorizationPresenter;
import org.obiba.opal.web.gwt.app.client.authz.presenter.SubjectAuthorizationPresenter.AddPrincipalHandler;
import org.obiba.opal.web.gwt.app.client.authz.presenter.SubjectAuthorizationPresenter.PermissionSelectionHandler;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrayDataProvider;
import org.obiba.opal.web.gwt.app.client.js.JsArrays;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ConstantActionsProvider;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.HasActionHandler;
import org.obiba.opal.web.model.client.opal.Acls;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.MultiWordSuggestOracle;
import com.google.gwt.user.client.ui.SuggestBox;
import com.google.gwt.user.client.ui.SuggestOracle;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.gwtplatform.mvp.client.ViewImpl;

/**
 *
 */
public class SubjectAuthorizationView extends ViewImpl implements SubjectAuthorizationPresenter.Display {

  private static final int PAGER_SIZE = 20;

  private final Widget widget;

  @UiField
  InlineLabel typeLabel;

  @UiField
  CellTable<Acls> table;

  @UiField
  SimplePager pager;

  @UiField(provided = true)
  SuggestBox principal;

  @UiField
  HasClickHandlers add;

  private MultiWordSuggestOracle suggestions;

  private SubjectSuggestionDisplay suggestionDisplay;

  private JsArrayDataProvider<Acls> subjectPermissionsDataProvider = new JsArrayDataProvider<Acls>();

  private boolean actionsColumnAdded;

  private ActionsColumn<Acls> actionsColumn;

  //
  // Static Variables
  //

  private static AuthorizationViewUiBinder uiBinder = GWT.create(AuthorizationViewUiBinder.class);

  private static Translations translations = GWT.create(Translations.class);

  //
  // Constructors
  //

  public SubjectAuthorizationView() {
    principal = new SuggestBox(suggestions = new MultiWordSuggestOracle(), new TextBox(), suggestionDisplay = new SubjectSuggestionDisplay());
    widget = uiBinder.createAndBindUi(this);
    initAclsTable();
  }

  @Override
  public Widget asWidget() {
    return widget;
  }

  @Override
  public void renderSubjectType(String type) {
    typeLabel.setText(translations.subjectTypeMap().get(type));
  }

  private final class SubjectSuggestionDisplay extends SuggestBox.DefaultSuggestionDisplay {
    public boolean hasSelection() {
      return getCurrentSelection() != null;
    }
  }

  @UiTemplate("SubjectAuthorizationView.ui.xml")
  interface AuthorizationViewUiBinder extends UiBinder<Widget, SubjectAuthorizationView> {
  }

  @Override
  public HasActionHandler<Acls> getActionsColumn() {
    return actionsColumn;
  }

  @Override
  public String getPrincipal() {
    return principal.getText();
  }

  @Override
  public void addPrincipalHandler(final AddPrincipalHandler handler) {
    principal.setAutoSelectEnabled(false);

    principal.addKeyDownHandler(new KeyDownHandler() {

      @Override
      public void onKeyDown(KeyDownEvent event) {
        if(event.getNativeKeyCode() == KeyCodes.KEY_ENTER && !suggestionDisplay.hasSelection()) {
          handler.onAdd(principal.getText());
        }
      }
    });

    principal.addSelectionHandler(new SelectionHandler<SuggestOracle.Suggestion>() {

      @Override
      public void onSelection(SelectionEvent<Suggestion> event) {
        handler.onAdd(principal.getText());
      }
    });

    add.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        handler.onAdd(principal.getText());
      }
    });

  }

  @Override
  public void clear() {
    principal.setText("");
  }

  @Override
  public void initColumn(String header, PermissionSelectionHandler permHandler) {
    if(!actionsColumnAdded) {
      addPermissionColumn(header, permHandler);
    }
  }

  @Override
  public void renderSubjectSuggestions(JsArray<Acls> subjects) {
    suggestions.clear();
    for(Acls acls : JsArrays.toIterable(subjects)) {
      suggestions.add(acls.getSubject().getPrincipal());
    }
  }

  @Override
  public void renderPermissions(JsArray<Acls> subjectPermissions) {
    if(!actionsColumnAdded) {
      table.addColumn(actionsColumn, translations.actionsLabel());
      actionsColumnAdded = true;
    }
    subjectPermissionsDataProvider.setArray(subjectPermissions);
    subjectPermissionsDataProvider.refresh();

    int count = subjectPermissions.length();
    pager.setVisible(count > PAGER_SIZE);
    table.setVisible(count > 0);
  }

  private void initAclsTable() {
    table.addColumn(new TextColumn<Acls>() {
      @Override
      public String getValue(Acls object) {
        return object.getSubject().getPrincipal();
      }
    }, translations.whoLabel());

    actionsColumn = new ActionsColumn<Acls>(new ConstantActionsProvider<Acls>(DELETE_ACTION));

    actionsColumnAdded = false;

    table.setPageSize(PAGER_SIZE);
    pager.setDisplay(table);
    subjectPermissionsDataProvider.addDataDisplay(table);

    table.setVisible(false);
    pager.setVisible(false);
  }

  private void addPermissionColumn(final String header, final PermissionSelectionHandler permHandler) {
    Column<Acls, Boolean> column = new Column<Acls, Boolean>(new CheckboxCell()) {

      @Override
      public Boolean getValue(Acls acls) {
        return permHandler.hasPermission(header, acls);
      }
    };

    FieldUpdater<Acls, Boolean> fieldUpdater = new FieldUpdater<Acls, Boolean>() {

      @Override
      public void update(int index, Acls object, Boolean value) {
        if(value) {
          permHandler.authorize(object.getSubject(), header);
        } else {
          permHandler.unauthorize(object.getSubject(), header);
        }
      }
    };

    column.setFieldUpdater(fieldUpdater);
    String headerStr = translations.permissionMap().containsKey(header) ? translations.permissionMap().get(header) : header;
    table.addColumn(column, headerStr);

  }

}
