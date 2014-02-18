/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.magma.presenter;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.obiba.opal.web.gwt.app.client.administration.index.presenter.IndexPresenter;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.magma.event.TableSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.magma.table.presenter.TablePropertiesModalPresenter;
import org.obiba.opal.web.gwt.app.client.magma.table.presenter.ViewPropertiesModalPresenter;
import org.obiba.opal.web.gwt.app.client.magma.variable.presenter.ContingencyTablePresenter;
import org.obiba.opal.web.gwt.app.client.magma.variable.presenter.VariableAttributeModalPresenter;
import org.obiba.opal.web.gwt.app.client.magma.variable.presenter.VariablePropertiesModalPresenter;
import org.obiba.opal.web.gwt.app.client.magma.variablestoview.presenter.VariablesToViewPresenter;
import org.obiba.opal.web.gwt.app.client.permissions.presenter.ResourcePermissionsPresenter;
import org.obiba.opal.web.gwt.app.client.presenter.ModalProvider;
import org.obiba.opal.web.gwt.test.AbstractGwtTestSetup;

import com.google.gwt.event.shared.testing.CountingEventBus;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.inject.Provider;
import com.google.web.bindery.event.shared.Event;
import com.google.web.bindery.event.shared.EventBus;
import com.google.web.bindery.event.shared.HandlerRegistration;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

public class TablePresenterTest extends AbstractGwtTestSetup {

  private EventBus eventBusMock;

  private TablePresenter.Display displayMock;

  private TablePresenter presenter;

  @Before
  public void setUp() {
    displayMock = createMock(TablePresenter.Display.class);
    eventBusMock = createMock(EventBus.class);
    Provider<ResourcePermissionsPresenter> resourcePermissionsPresenterProvider = createMock(Provider.class);
    Provider<ContingencyTablePresenter> crossVariablePresenterProvider = createMock(Provider.class);
    ModalProvider<IndexPresenter> mockIndexProvider = createMock(ModalProvider.class);
    ModalProvider<EntityModalPresenter> modalEntityModalPresenter = createMock(ModalProvider.class);
    ModalProvider<ValueSequencePopupPresenter> modalProviderValueSequence = createMock(ModalProvider.class);
    ModalProvider<VariablesToViewPresenter> variablesToViewProvider = createMock(ModalProvider.class);
    ModalProvider<VariablePropertiesModalPresenter> variablePropertiesModalProvider = createMock(ModalProvider.class);
    ModalProvider<TablePropertiesModalPresenter> tablePropertiesModalPresenterModalProvider = createMock(
        ModalProvider.class);
    ModalProvider<ViewPropertiesModalPresenter> viewPropertiesModalProvider = createMock(ModalProvider.class);
    ModalProvider<VariableAttributeModalPresenter> attributeModalProvider = createMock(ModalProvider.class);

    Translations translations = createMock(Translations.class);

    ValuesTablePresenter values = new ValuesTablePresenter(null, null, null, modalProviderValueSequence,
        modalEntityModalPresenter);

    presenter = new TablePresenter(displayMock, new CountingEventBus(), null, values, crossVariablePresenterProvider,
        resourcePermissionsPresenterProvider, mockIndexProvider, variablesToViewProvider,
        variablePropertiesModalProvider, viewPropertiesModalProvider, tablePropertiesModalPresenterModalProvider, null,
        null, attributeModalProvider, translations, null);
  }

  @SuppressWarnings({ "unchecked" })
  @Test
  @Ignore
  public void testOnBind_RegistersHandlersAndBindsDependencies() {
    HandlerRegistration handlerRegistrationMock = createMock(HandlerRegistration.class);
    expect(eventBusMock.addHandler((Event.Type<TableSelectionChangeEvent.Handler>) EasyMock.anyObject(),
        (TableSelectionChangeEvent.Handler) EasyMock.anyObject())).andReturn(handlerRegistrationMock).once();
    expect(eventBusMock.addHandler((Event.Type<ConfirmationEvent.Handler>) EasyMock.anyObject(),
        (ConfirmationEvent.Handler) EasyMock.anyObject())).andReturn(handlerRegistrationMock).once();
    expect(displayMock.addVariableSortHandler((ColumnSortEvent.Handler) EasyMock.anyObject()))
        .andReturn(handlerRegistrationMock).once();

    replay(displayMock, eventBusMock);
    presenter.bind();

    verify(displayMock, eventBusMock);
  }
}
