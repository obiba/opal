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

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import net.customware.gwt.presenter.client.EventBus;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.obiba.opal.web.gwt.app.client.event.DatasourceSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.event.NavigatorSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.event.SiblingTableSelectionEvent;
import org.obiba.opal.web.gwt.app.client.presenter.DatasourcePresenter;
import org.obiba.opal.web.gwt.app.client.ui.HasFieldUpdater;
import org.obiba.opal.web.gwt.test.AbstractGwtTestSetup;
import org.obiba.opal.web.model.client.TableDto;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.GwtEvent.Type;

public class DatasourcePresenterTest extends AbstractGwtTestSetup {

  private EventBus eventBusMock;

  private DatasourcePresenter.Display displayMock;

  private DatasourcePresenter datasourcePresenter;

  @Before
  public void setUp() {
    displayMock = createMock(DatasourcePresenter.Display.class);
    eventBusMock = createMock(EventBus.class);
    datasourcePresenter = new DatasourcePresenter(displayMock, eventBusMock);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testThatEventHandlersAreAddedToUIComponents() throws Exception {
    HandlerRegistration handlerRegistrationMock = createMock(HandlerRegistration.class);
    expect(eventBusMock.addHandler((Type<NavigatorSelectionChangeEvent.Handler>) EasyMock.anyObject(), (NavigatorSelectionChangeEvent.Handler) EasyMock.anyObject())).andReturn(handlerRegistrationMock).once();
    expect(eventBusMock.addHandler((Type<DatasourceSelectionChangeEvent.Handler>) EasyMock.anyObject(), (DatasourceSelectionChangeEvent.Handler) EasyMock.anyObject())).andReturn(handlerRegistrationMock).once();
    expect(eventBusMock.addHandler((Type<SiblingTableSelectionEvent.Handler>) EasyMock.anyObject(), (SiblingTableSelectionEvent.Handler) EasyMock.anyObject())).andReturn(handlerRegistrationMock).once();

    HasClickHandlers hasClickHandlerMock = createMock(HasClickHandlers.class);
    expect(displayMock.getSpreadsheetIcon()).andReturn(hasClickHandlerMock);

    HasFieldUpdater<TableDto, String> hasFieldUpdater = createMock(HasFieldUpdater.class);
    expect(displayMock.getTableNameColumn()).andReturn(hasFieldUpdater);

    expect(hasClickHandlerMock.addClickHandler((ClickHandler) anyObject())).andReturn(handlerRegistrationMock).atLeastOnce();

    replay(displayMock, eventBusMock, hasClickHandlerMock);
    datasourcePresenter.bind();

    verify(displayMock, hasClickHandlerMock);
  }
}
