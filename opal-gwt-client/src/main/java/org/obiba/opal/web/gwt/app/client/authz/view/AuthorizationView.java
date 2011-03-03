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
import org.obiba.opal.web.model.client.opal.Acl;

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
  CellTable<Acl> table;

  @UiField
  TextBox principal;

  @UiField
  Image add;

  private JsArrayDataProvider<Acl> aclsDataProvider = new JsArrayDataProvider<Acl>();

  private Column<Acl, ?> permColumn;

  private ActionsColumn<Acl> actionsColumn;

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
    table.addColumn(new TextColumn<Acl>() {
      @Override
      public String getValue(Acl object) {
        return object.getPrincipal();
      }
    }, translations.whoLabel());

    actionsColumn = new ActionsColumn<Acl>(new ConstantActionsProvider<Acl>(DELETE_ACTION));
    table.addColumn(actionsColumn, translations.actionsLabel());

    aclsDataProvider.addDataDisplay(table);
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
  public void renderAcls(JsArray<Acl> acls) {
    aclsDataProvider.setArray(acls);
    aclsDataProvider.refresh();
    table.setVisible(acls.length() > 0);
  }

  @Override
  public HasActionHandler<Acl> getActionsColumn() {
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

}
