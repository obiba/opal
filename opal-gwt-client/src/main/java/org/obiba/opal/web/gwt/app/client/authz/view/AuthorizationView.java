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

import java.util.List;

import org.obiba.opal.web.gwt.app.client.authz.presenter.AuthorizationPresenter;
import org.obiba.opal.web.gwt.app.client.authz.presenter.AuthorizationPresenter.AddPrincipalHandler;
import org.obiba.opal.web.gwt.app.client.authz.presenter.SubjectPermissions;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ActionsColumn;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.ConstantActionsProvider;
import org.obiba.opal.web.gwt.app.client.widgets.celltable.HasActionHandler;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
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
import com.google.gwt.view.client.ListDataProvider;

/**
 *
 */
public class AuthorizationView extends Composite implements AuthorizationPresenter.Display {

  @UiField
  CellTable<SubjectPermissions> table;

  @UiField
  TextBox principal;

  @UiField
  Image add;

  private ListDataProvider<SubjectPermissions> subjectPermissionsDataProvider = new ListDataProvider<SubjectPermissions>();

  private boolean permColumnAdded;

  private ActionsColumn<SubjectPermissions> actionsColumn;

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
    table.addColumn(new TextColumn<SubjectPermissions>() {
      @Override
      public String getValue(SubjectPermissions object) {
        return object.getSubject();
      }
    }, translations.whoLabel());

    actionsColumn = new ActionsColumn<SubjectPermissions>(new ConstantActionsProvider<SubjectPermissions>(DELETE_ACTION));

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
  public HasActionHandler<SubjectPermissions> getActionsColumn() {
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
  public void renderPermissions(Iterable<String> names, List<SubjectPermissions> subjectPermissions) {
    if(!permColumnAdded) {
      for(String name : names) {
        addPermissionColumn(name);
      }
      table.addColumn(actionsColumn, translations.actionsLabel());
      permColumnAdded = true;
    }
    subjectPermissionsDataProvider.setList(subjectPermissions);
    subjectPermissionsDataProvider.refresh();
    table.setVisible(subjectPermissions.size() > 0);
  }

  private void addPermissionColumn(final String name) {
    EnablableCheckboxCell cell = new EnablableCheckboxCell();
    Column<SubjectPermissions, Boolean> column = new Column<SubjectPermissions, Boolean>(cell) {

      @Override
      public Boolean getValue(SubjectPermissions object) {
        return object.hasPermission(name);
      }
    };

    cell.setEnabled(false);

    FieldUpdater<SubjectPermissions, Boolean> fieldUpdater = new FieldUpdater<SubjectPermissions, Boolean>() {

      @Override
      public void update(int index, SubjectPermissions object, Boolean value) {
        // TODO
        GWT.log(object.getSubject() + ":" + name + "=" + value);
      }
    };

    column.setFieldUpdater(fieldUpdater);
    table.addColumn(column, name);
  }

}
