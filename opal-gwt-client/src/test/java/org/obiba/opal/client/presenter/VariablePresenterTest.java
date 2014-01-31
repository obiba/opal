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
import org.obiba.opal.web.gwt.app.client.magma.event.VariableSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.magma.presenter.SummaryTabPresenter;
import org.obiba.opal.web.gwt.app.client.magma.presenter.VariablePresenter;
import org.obiba.opal.web.gwt.app.client.magma.presenter.VariableVcsCommitHistoryPresenter;
import org.obiba.opal.web.gwt.app.client.magma.variable.presenter.CategoriesEditorModalPresenter;
import org.obiba.opal.web.gwt.app.client.magma.variable.presenter.VariableAttributeModalPresenter;
import org.obiba.opal.web.gwt.app.client.magma.variable.presenter.VariablePropertiesModalPresenter;
import org.obiba.opal.web.gwt.app.client.magma.variablestoview.presenter.VariablesToViewPresenter;
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

public class VariablePresenterTest extends AbstractGwtTestSetup {

  private EventBus eventBusMock;

  private VariablePresenter.Display displayMock;

  private SummaryTabPresenter.Display summaryTabMock;

  private VariablePresenter variablePresenter;

  private VariableVcsCommitHistoryPresenter variableVcsCommitHistoryPresenterMock;

  @Before
  public void setUp() {
    displayMock = createMock(VariablePresenter.Display.class);
    summaryTabMock = createMock(SummaryTabPresenter.Display.class);
    eventBusMock = createMock(EventBus.class);
    variableVcsCommitHistoryPresenterMock = createMock(VariableVcsCommitHistoryPresenter.class);
    Provider<ResourcePermissionsPresenter> mockResourcePermissionProvider = createMock(Provider.class);
    ModalProvider<VariablesToViewPresenter> variablesToViewProvider = createMock(ModalProvider.class);
    ModalProvider<CategoriesEditorModalPresenter> categoriesEditorModalProvider = createMock(ModalProvider.class);
    ModalProvider<VariablePropertiesModalPresenter> propertiesEditorModalProvider = createMock(ModalProvider.class);

    ModalProvider<VariableAttributeModalPresenter> varAttributeModalProvider = createMock(ModalProvider.class);
    variablePresenter = new VariablePresenter(displayMock, new CountingEventBus(), null, null,
        new SummaryTabPresenter(eventBusMock, summaryTabMock), null, mockResourcePermissionProvider,
        variableVcsCommitHistoryPresenterMock, variablesToViewProvider, categoriesEditorModalProvider,
        propertiesEditorModalProvider, varAttributeModalProvider);
  }

  @SuppressWarnings("unchecked")
  @Test
  @Ignore
  public void testThatEventHandlersAreAddedToUIComponents() throws Exception {
    HandlerRegistration handlerRegistrationMock = createMock(HandlerRegistration.class);
    expect(eventBusMock.addHandler((Type<VariableSelectionChangeEvent.Handler>) EasyMock.anyObject(),
        (VariableSelectionChangeEvent.Handler) EasyMock.anyObject())).andReturn(handlerRegistrationMock).once();

    replay(displayMock, eventBusMock);
    variablePresenter.bind();

    verify(displayMock, eventBusMock);
  }

}
