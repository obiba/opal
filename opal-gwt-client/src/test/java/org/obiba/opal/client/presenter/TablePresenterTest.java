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
import org.obiba.opal.web.gwt.app.client.event.SiblingVariableSelectionEvent;
import org.obiba.opal.web.gwt.app.client.event.TableSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.presenter.TablePresenter;
import org.obiba.opal.web.gwt.app.client.ui.HasFieldUpdater;
import org.obiba.opal.web.gwt.test.AbstractGwtTestSetup;
import org.obiba.opal.web.model.client.VariableDto;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.GwtEvent.Type;

public class TablePresenterTest extends AbstractGwtTestSetup {

  private EventBus eventBusMock;

  private TablePresenter.Display displayMock;

  private TablePresenter presenter;

  @Before
  public void setUp() {
    displayMock = createMock(TablePresenter.Display.class);
    eventBusMock = createMock(EventBus.class);
    presenter = new TablePresenter(displayMock, eventBusMock);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testThatEventHandlersAreAddedToUIComponents() throws Exception {
    HandlerRegistration handlerRegistrationMock = createMock(HandlerRegistration.class);
    expect(eventBusMock.addHandler((Type<TableSelectionChangeEvent.Handler>) EasyMock.anyObject(), (TableSelectionChangeEvent.Handler) EasyMock.anyObject())).andReturn(handlerRegistrationMock).once();
    expect(eventBusMock.addHandler((Type<SiblingVariableSelectionEvent.Handler>) EasyMock.anyObject(), (SiblingVariableSelectionEvent.Handler) EasyMock.anyObject())).andReturn(handlerRegistrationMock).once();

    HasClickHandlers hasClickHandlerMock = createMock(HasClickHandlers.class);
    expect(displayMock.getSpreadsheetIcon()).andReturn(hasClickHandlerMock);
    expect(displayMock.getParentLink()).andReturn(hasClickHandlerMock);
    expect(displayMock.getNextLink()).andReturn(hasClickHandlerMock);
    expect(displayMock.getPreviousLink()).andReturn(hasClickHandlerMock);
    expect(hasClickHandlerMock.addClickHandler((ClickHandler) anyObject())).andReturn(handlerRegistrationMock).atLeastOnce();

    HasFieldUpdater<VariableDto, String> hasFieldUpdaterMock = createMock(HasFieldUpdater.class);
    expect(displayMock.getVariableNameColumn()).andReturn(hasFieldUpdaterMock);

    replay(displayMock, eventBusMock, hasClickHandlerMock);
    presenter.bind();

    verify(displayMock, eventBusMock, hasClickHandlerMock);
  }
}
