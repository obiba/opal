/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.client.presenter;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import net.customware.gwt.presenter.client.EventBus;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.obiba.opal.web.gwt.app.client.authz.presenter.AuthorizationPresenter;
import org.obiba.opal.web.gwt.app.client.authz.presenter.SubjectAuthorizationPresenter;
import org.obiba.opal.web.gwt.app.client.authz.presenter.SubjectAuthorizationPresenter.AddPrincipalHandler;
import org.obiba.opal.web.gwt.app.client.authz.presenter.SubjectAuthorizationPresenter.Display;
import org.obiba.opal.web.gwt.app.client.navigator.event.SiblingVariableSelectionEvent;
import org.obiba.opal.web.gwt.app.client.navigator.event.TableSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.navigator.presenter.TablePresenter;
import org.obiba.opal.web.gwt.app.client.widgets.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.event.ViewSavedEvent;
import org.obiba.opal.web.gwt.test.AbstractGwtTestSetup;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.SuggestOracle.Suggestion;

public class TablePresenterTest extends AbstractGwtTestSetup {

  private EventBus eventBusMock;

  private TablePresenter.Display displayMock;

  private TablePresenter presenter;

  private AuthorizationPresenter.Display authzDisplayMock;

  private AuthorizationPresenter authorizationPresenter;

  private Display usersAuthzDisplayMock;

  private Display groupsAuthzDisplayMock;

  @Before
  public void setUp() {
    displayMock = createMock(TablePresenter.Display.class);
    eventBusMock = createMock(EventBus.class);
    authzDisplayMock = createMock(AuthorizationPresenter.Display.class);
    usersAuthzDisplayMock = createMock(SubjectAuthorizationPresenter.Display.class);
    groupsAuthzDisplayMock = createMock(SubjectAuthorizationPresenter.Display.class);

    authorizationPresenter = new AuthorizationPresenter(authzDisplayMock, eventBusMock, new SubjectAuthorizationPresenter(usersAuthzDisplayMock, eventBusMock), new SubjectAuthorizationPresenter(groupsAuthzDisplayMock, eventBusMock));
    presenter = new TablePresenter(displayMock, eventBusMock, authorizationPresenter);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testOnBind_RegistersHandlersAndBindsDependencies() {
    HandlerRegistration handlerRegistrationMock = createMock(HandlerRegistration.class);
    expect(eventBusMock.addHandler((Type<TableSelectionChangeEvent.Handler>) EasyMock.anyObject(), (TableSelectionChangeEvent.Handler) EasyMock.anyObject())).andReturn(handlerRegistrationMock).once();
    expect(eventBusMock.addHandler((Type<SiblingVariableSelectionEvent.Handler>) EasyMock.anyObject(), (SiblingVariableSelectionEvent.Handler) EasyMock.anyObject())).andReturn(handlerRegistrationMock).once();
    expect(eventBusMock.addHandler((Type<ConfirmationEvent.Handler>) EasyMock.anyObject(), (ConfirmationEvent.Handler) EasyMock.anyObject())).andReturn(handlerRegistrationMock).once();
    expect(eventBusMock.addHandler((Type<ViewSavedEvent.Handler>) EasyMock.anyObject(), (ViewSavedEvent.Handler) EasyMock.anyObject())).andReturn(handlerRegistrationMock).once();

    expect(displayMock.addVariableSuggestionHandler((SelectionHandler<Suggestion>) EasyMock.anyObject())).andReturn(handlerRegistrationMock).once();
    expect(displayMock.addVariableSortHandler((ColumnSortEvent.Handler) EasyMock.anyObject())).andReturn(handlerRegistrationMock).once();
    displayMock.setNextCommand((Command) EasyMock.anyObject());
    displayMock.setPreviousCommand((Command) EasyMock.anyObject());
    displayMock.setParentCommand((Command) EasyMock.anyObject());
    displayMock.setExcelDownloadCommand((Command) EasyMock.anyObject());
    displayMock.setExportDataCommand((Command) EasyMock.anyObject());
    displayMock.setCopyDataCommand((Command) EasyMock.anyObject());
    displayMock.setCreateCodingViewCommand((Command) EasyMock.anyObject());
    displayMock.setVariableNameFieldUpdater((FieldUpdater<VariableDto, String>) EasyMock.anyObject());
    displayMock.setVariableIndexFieldUpdater((FieldUpdater<VariableDto, String>) EasyMock.anyObject());
    displayMock.setPermissionsDisplay(authzDisplayMock);

    expect(usersAuthzDisplayMock.getActionsColumn()).andReturn(null).once();
    usersAuthzDisplayMock.addPrincipalHandler((AddPrincipalHandler) EasyMock.anyObject());

    expect(groupsAuthzDisplayMock.getActionsColumn()).andReturn(null).once();
    groupsAuthzDisplayMock.addPrincipalHandler((AddPrincipalHandler) EasyMock.anyObject());

    replay(displayMock, eventBusMock, usersAuthzDisplayMock, groupsAuthzDisplayMock);
    presenter.bind();

    verify(displayMock, eventBusMock, usersAuthzDisplayMock, groupsAuthzDisplayMock);
  }
}
