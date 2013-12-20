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
import org.obiba.opal.web.gwt.app.client.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.magma.event.DatasourceSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.magma.event.DatasourceUpdatedEvent;
import org.obiba.opal.web.gwt.app.client.magma.event.TableSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.magma.presenter.DatasourcePresenter;
import org.obiba.opal.web.gwt.app.client.magma.table.presenter.AddViewModalPresenter;
import org.obiba.opal.web.gwt.app.client.magma.table.presenter.TablePropertiesModalPresenter;
import org.obiba.opal.web.gwt.app.client.permissions.presenter.ResourcePermissionsPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.test.AbstractGwtTestSetup;

import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.gwt.event.shared.testing.CountingEventBus;
import com.google.inject.Provider;
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

  @SuppressWarnings("unchecked")
  @Before
  public void setUp() {
    displayMock = createMock(DatasourcePresenter.Display.class);
    eventBusMock = createMock(EventBus.class);
    Provider<ResourcePermissionsPresenter> mockPermissionsProvider = createMock(Provider.class);
    ModalProvider<TablePropertiesModalPresenter> propertiesEditorModalProvider = createMock(ModalProvider.class);
    ModalProvider<AddViewModalPresenter> createViewModalProvider = createMock(ModalProvider.class);

    datasourcePresenter = new DatasourcePresenter(displayMock, new CountingEventBus(), propertiesEditorModalProvider,
        null, createViewModalProvider, null, mockPermissionsProvider, null);
  }

  @Test
  @Ignore
  public void testThatEventHandlersAreAddedToUIComponents() throws Exception {
    HandlerRegistration handlerRegistrationMock = createMock(HandlerRegistration.class);
    expect(eventBusMock.addHandler((Type<TableSelectionChangeEvent.Handler>) EasyMock.anyObject(),
        (TableSelectionChangeEvent.Handler) EasyMock.anyObject())).andReturn(handlerRegistrationMock).once();
    expect(eventBusMock
        .addHandler((Type<DatasourceSelectionChangeEvent.DatasourceSelectionChangeHandler>) EasyMock.anyObject(),
            (DatasourceSelectionChangeEvent.DatasourceSelectionChangeHandler) EasyMock.anyObject()))
        .andReturn(handlerRegistrationMock).once();
    expect(eventBusMock.addHandler((Type<ConfirmationEvent.Handler>) EasyMock.anyObject(),
        (ConfirmationEvent.Handler) EasyMock.anyObject())).andReturn(handlerRegistrationMock).once();
    expect(eventBusMock.addHandler((Type<DatasourceUpdatedEvent.Handler>) EasyMock.anyObject(),
        (DatasourceUpdatedEvent.Handler) EasyMock.anyObject())).andReturn(handlerRegistrationMock).once();

    replay(displayMock, eventBusMock);
    datasourcePresenter.bind();

    verify(displayMock, eventBusMock);
  }
}
