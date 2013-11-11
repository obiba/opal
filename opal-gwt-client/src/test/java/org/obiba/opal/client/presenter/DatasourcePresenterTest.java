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

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.obiba.opal.web.gwt.app.client.authz.presenter.SubjectAuthorizationPresenter;
import org.obiba.opal.web.gwt.app.client.authz.presenter.SubjectAuthorizationPresenter.AddPrincipalHandler;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.magma.configureview.event.ViewSavedEvent;
import org.obiba.opal.web.gwt.app.client.magma.event.DatasourceSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.magma.event.DatasourceUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.magma.event.TableSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.magma.presenter.DatasourcePresenter;
import org.obiba.opal.web.gwt.app.client.magma.table.presenter.TablePropertiesModalPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.test.AbstractGwtTestSetup;

import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.gwt.event.shared.testing.CountingEventBus;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

public class DatasourcePresenterTest extends AbstractGwtTestSetup {

  private EventBus eventBusMock;

  private DatasourcePresenter.Display displayMock;

  private DatasourcePresenter datasourcePresenter;

  private SubjectAuthorizationPresenter.Display usersAuthzDisplayMock;

  private SubjectAuthorizationPresenter.Display groupsAuthzDisplayMock;

  @SuppressWarnings("unchecked")
  @Before
  public void setUp() {
    displayMock = createMock(DatasourcePresenter.Display.class);
    eventBusMock = createMock(EventBus.class);
    usersAuthzDisplayMock = createMock(SubjectAuthorizationPresenter.Display.class);
    groupsAuthzDisplayMock = createMock(SubjectAuthorizationPresenter.Display.class);
    ModalProvider<TablePropertiesModalPresenter> propertiesEditorModalProvider = createMock(ModalProvider.class);

    datasourcePresenter = new DatasourcePresenter(displayMock, new CountingEventBus(), propertiesEditorModalProvider,
        null);
  }

  @SuppressWarnings("unchecked")
  @Test
  @Ignore
  public void testThatEventHandlersAreAddedToUIComponents() throws Exception {
    HandlerRegistration handlerRegistrationMock = createMock(HandlerRegistration.class);
    expect(eventBusMock.addHandler((Type<TableSelectionChangeEvent.Handler>) EasyMock.anyObject(),
        (TableSelectionChangeEvent.Handler) EasyMock.anyObject())).andReturn(handlerRegistrationMock).once();
    expect(eventBusMock.addHandler((Type<DatasourceSelectionChangeEvent.Handler>) EasyMock.anyObject(),
        (DatasourceSelectionChangeEvent.Handler) EasyMock.anyObject())).andReturn(handlerRegistrationMock).once();
    expect(eventBusMock.addHandler((Type<ConfirmationEvent.Handler>) EasyMock.anyObject(),
        (ConfirmationEvent.Handler) EasyMock.anyObject())).andReturn(handlerRegistrationMock).once();
    expect(eventBusMock.addHandler((Type<DatasourceUpdatedEvent.Handler>) EasyMock.anyObject(),
        (DatasourceUpdatedEvent.Handler) EasyMock.anyObject())).andReturn(handlerRegistrationMock).once();
    expect(eventBusMock
        .addHandler((Type<ViewSavedEvent.Handler>) EasyMock.anyObject(), (ViewSavedEvent.Handler) EasyMock.anyObject()))
        .andReturn(handlerRegistrationMock).once();

    usersAuthzDisplayMock.addPrincipalHandler((AddPrincipalHandler) EasyMock.anyObject());

    groupsAuthzDisplayMock.addPrincipalHandler((AddPrincipalHandler) EasyMock.anyObject());

    replay(displayMock, eventBusMock, usersAuthzDisplayMock, groupsAuthzDisplayMock);
    datasourcePresenter.bind();

    verify(displayMock, eventBusMock, usersAuthzDisplayMock, groupsAuthzDisplayMock);
  }
}
