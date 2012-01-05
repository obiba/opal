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
import org.junit.Ignore;
import org.junit.Test;
import org.obiba.opal.web.gwt.app.client.authz.presenter.AuthorizationPresenter;
import org.obiba.opal.web.gwt.app.client.authz.presenter.SubjectAuthorizationPresenter;
import org.obiba.opal.web.gwt.app.client.authz.presenter.SubjectAuthorizationPresenter.AddPrincipalHandler;
import org.obiba.opal.web.gwt.app.client.authz.presenter.SubjectAuthorizationPresenter.Display;
import org.obiba.opal.web.gwt.app.client.navigator.event.VariableSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.navigator.presenter.VariablePresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.SummaryTabPresenter;
import org.obiba.opal.web.gwt.test.AbstractGwtTestSetup;

import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.testing.CountingEventBus;
import com.google.gwt.user.client.Command;

public class VariablePresenterTest extends AbstractGwtTestSetup {

  private EventBus eventBusMock;

  private VariablePresenter.Display displayMock;

  private SummaryTabPresenter.Display summaryTabMock;

  private VariablePresenter variablePresenter;

  private AuthorizationPresenter.Display authzDisplayMock;

  private AuthorizationPresenter authorizationPresenter;

  private Display usersAuthzDisplayMock;

  private Display groupsAuthzDisplayMock;

  @Before
  public void setUp() {
    displayMock = createMock(VariablePresenter.Display.class);
    summaryTabMock = createMock(SummaryTabPresenter.Display.class);
    eventBusMock = createMock(EventBus.class);
    authzDisplayMock = createMock(AuthorizationPresenter.Display.class);
    usersAuthzDisplayMock = createMock(SubjectAuthorizationPresenter.Display.class);
    groupsAuthzDisplayMock = createMock(SubjectAuthorizationPresenter.Display.class);

    authorizationPresenter = new AuthorizationPresenter(authzDisplayMock, eventBusMock, new SubjectAuthorizationPresenter(usersAuthzDisplayMock, eventBusMock), new SubjectAuthorizationPresenter(groupsAuthzDisplayMock, eventBusMock));
    variablePresenter = new VariablePresenter(displayMock, new CountingEventBus(), createMock(VariablePresenter.Proxy.class), null, new SummaryTabPresenter(summaryTabMock, eventBusMock) {
      @Override
      public void bind() {
        // noop for testing
      }
    }, authorizationPresenter);
  }

  @SuppressWarnings("unchecked")
  @Test
  @Ignore
  public void testThatEventHandlersAreAddedToUIComponents() throws Exception {
    HandlerRegistration handlerRegistrationMock = createMock(HandlerRegistration.class);
    expect(eventBusMock.addHandler((Type<VariableSelectionChangeEvent.Handler>) EasyMock.anyObject(), (VariableSelectionChangeEvent.Handler) EasyMock.anyObject())).andReturn(handlerRegistrationMock).once();

    displayMock.setNextCommand((Command) EasyMock.anyObject());
    displayMock.setPreviousCommand((Command) EasyMock.anyObject());
    displayMock.setParentCommand((Command) EasyMock.anyObject());
    displayMock.setSummaryTabCommand((Command) EasyMock.anyObject());
    displayMock.setDeriveCategorizeCommand((Command) EasyMock.anyObject());
    displayMock.setDeriveCustomCommand((Command) EasyMock.anyObject());
    displayMock.setSummaryTabWidget(summaryTabMock);
    displayMock.setPermissionsTabWidget(authzDisplayMock);

    expect(usersAuthzDisplayMock.getActionsColumn()).andReturn(null).once();
    usersAuthzDisplayMock.addPrincipalHandler((AddPrincipalHandler) EasyMock.anyObject());

    expect(groupsAuthzDisplayMock.getActionsColumn()).andReturn(null).once();
    groupsAuthzDisplayMock.addPrincipalHandler((AddPrincipalHandler) EasyMock.anyObject());

    replay(displayMock, eventBusMock, usersAuthzDisplayMock, groupsAuthzDisplayMock);
    variablePresenter.bind();

    verify(displayMock, eventBusMock, usersAuthzDisplayMock, groupsAuthzDisplayMock);
  }

}
