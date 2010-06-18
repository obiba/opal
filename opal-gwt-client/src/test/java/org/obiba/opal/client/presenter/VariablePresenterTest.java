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

import org.junit.Before;
import org.junit.Test;
import org.obiba.opal.web.gwt.app.client.event.VariableSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.presenter.VariablePresenter;
import org.obiba.opal.web.gwt.test.AbstractGwtTestSetup;

import com.google.gwt.event.shared.HandlerRegistration;

public class VariablePresenterTest extends AbstractGwtTestSetup {

  private EventBus eventBusMock;

  private VariablePresenter.Display displayMock;

  private VariablePresenter variablePresenter;

  @Before
  public void setUp() {
    displayMock = createMock(VariablePresenter.Display.class);
    eventBusMock = createMock(EventBus.class);
    variablePresenter = new VariablePresenter(displayMock, eventBusMock);
  }

  @Test
  public void testThatEventHandlersAreAddedToUIComponents() throws Exception {
    HandlerRegistration handlerRegistrationMock = createMock(HandlerRegistration.class);
    VariableSelectionChangeEvent.Handler variableSelectionChangeEventMock = createMock(VariableSelectionChangeEvent.Handler.class);
    expect(eventBusMock.addHandler(VariableSelectionChangeEvent.getType(), variableSelectionChangeEventMock)).andReturn(handlerRegistrationMock).anyTimes();

    replay(displayMock);
    variablePresenter.bind();

    verify(displayMock);
  }

}
