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

import org.obiba.opal.web.gwt.app.client.authz.presenter.AuthorizationPresenter;
import org.obiba.opal.web.gwt.app.client.authz.presenter.AuthorizationPresenter.AddPrincipalHandler;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.js.JsArrayDataProvider;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ConstantActionsProvider;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.HasActionHandler;
import org.obiba.opal.web.model.client.opal.SubjectAcls;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 */
public class AuthorizationView extends Composite implements AuthorizationPresenter.Display {

  @UiField
  CellTable<SubjectAcls> table;

  @UiField
  TextBox principal;

  @UiField
  Image add;

  private JsArrayDataProvider<SubjectAcls> subjectPermissionsDataProvider = new JsArrayDataProvider<SubjectAcls>();

  private boolean permColumnAdded;

  private ActionsColumn<SubjectAcls> actionsColumn;

  //
  // Static Variables
  //

  private static AuthorizationViewUiBinder uiBinder = GWT.create(AuthorizationViewUiBinder.class);

  private static Translations translations = GWT.create(Translations.class);

  //
  // Constructors
  //

  public AuthorizationView() {
    initWidget(uiBinder.createAndBindUi(this));
    initAclsTable();
  }

  private void initAclsTable() {
    table.addColumn(new TextColumn<SubjectAcls>() {
      @Override
      public String getValue(SubjectAcls object) {
        return object.getPrincipal();
      }
    }, translations.whoLabel());

    actionsColumn = new ActionsColumn<SubjectAcls>(new ConstantActionsProvider<SubjectAcls>(DELETE_ACTION));

    permColumnAdded = false;

    subjectPermissionsDataProvider.addDataDisplay(table);
  }

  //
  // WidgetDisplay methods
  //

  @Override
  public void startProcessing() {
  }

  @Override
  public void stopProcessing() {
  }

  //
  // UiBinder
  //

  @UiTemplate("AuthorizationView.ui.xml")
  interface AuthorizationViewUiBinder extends UiBinder<Widget, AuthorizationView> {
  }

  @Override
  public HasActionHandler<SubjectAcls> getActionsColumn() {
    return actionsColumn;
  }

  @Override
  public String getPrincipal() {
    return principal.getText();
  }

  @Override
  public void addHandler(final AddPrincipalHandler handler) {
    principal.addKeyDownHandler(new KeyDownHandler() {

      @Override
      public void onKeyDown(KeyDownEvent event) {
        if(event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
          handler.onAdd(principal.getText());
        }
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
  public void initColumn(String header, Iterable<String> resources) {
    addPermissionColumn(header, resources);
  }

  @Override
  public void renderPermissions(JsArray<SubjectAcls> subjectPermissions) {
    if(!permColumnAdded) {
      table.addColumn(actionsColumn, translations.actionsLabel());
      permColumnAdded = true;
    }
    subjectPermissionsDataProvider.setArray(subjectPermissions);
    subjectPermissionsDataProvider.refresh();
    table.setVisible(subjectPermissions.length() > 0);
  }

  private void addPermissionColumn(final String name, final Iterable<String> resources) {
    EnablableCheckboxCell cell = new EnablableCheckboxCell();
    Column<SubjectAcls, Boolean> column = new Column<SubjectAcls, Boolean>(cell) {

      @Override
      public Boolean getValue(SubjectAcls object) {
        return true;// object.hasPermission(name);
      }
    };

    cell.setEnabled(false);

    FieldUpdater<SubjectAcls, Boolean> fieldUpdater = new FieldUpdater<SubjectAcls, Boolean>() {

      @Override
      public void update(int index, SubjectAcls object, Boolean value) {
        // TODO
        GWT.log(object.getPrincipal() + ":" + name + "=" + value);
      }
    };

    column.setFieldUpdater(fieldUpdater);
    table.addColumn(column, name);

  }

}
