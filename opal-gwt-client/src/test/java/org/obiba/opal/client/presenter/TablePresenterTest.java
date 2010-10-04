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
import static org.junit.Assert.assertEquals;
import net.customware.gwt.presenter.client.EventBus;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.obiba.opal.web.gwt.app.client.event.SiblingVariableSelectionEvent;
import org.obiba.opal.web.gwt.app.client.event.TableSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.presenter.TablePresenter;
import org.obiba.opal.web.gwt.app.client.wizard.configureview.presenter.ConfigureViewStepPresenter;
import org.obiba.opal.web.gwt.test.AbstractGwtTestSetup;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.gwt.user.client.Command;

public class TablePresenterTest extends AbstractGwtTestSetup {

  private EventBus eventBusMock;

  private TablePresenter.Display displayMock;

  private TablePresenter presenter;

  private ConfigureViewStepPresenterSpy configureViewStepPresenterSpy;

  @Before
  public void setUp() {
    displayMock = createMock(TablePresenter.Display.class);
    eventBusMock = createMock(EventBus.class);
    configureViewStepPresenterSpy = createConfigureViewStepPresenterSpy(eventBusMock);

    presenter = new TablePresenter(displayMock, eventBusMock, configureViewStepPresenterSpy);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testOnBind_RegistersHandlersAndBindsDependencies() {
    HandlerRegistration handlerRegistrationMock = createMock(HandlerRegistration.class);
    expect(eventBusMock.addHandler((Type<TableSelectionChangeEvent.Handler>) EasyMock.anyObject(), (TableSelectionChangeEvent.Handler) EasyMock.anyObject())).andReturn(handlerRegistrationMock).once();
    expect(eventBusMock.addHandler((Type<SiblingVariableSelectionEvent.Handler>) EasyMock.anyObject(), (SiblingVariableSelectionEvent.Handler) EasyMock.anyObject())).andReturn(handlerRegistrationMock).once();

    displayMock.setNextCommand((Command) EasyMock.anyObject());
    displayMock.setPreviousCommand((Command) EasyMock.anyObject());
    displayMock.setParentCommand((Command) EasyMock.anyObject());
    displayMock.setExcelDownloadCommand((Command) EasyMock.anyObject());
    displayMock.setVariableNameFieldUpdater((FieldUpdater<VariableDto, String>) EasyMock.anyObject());

    replay(displayMock, eventBusMock);
    presenter.bind();

    verify(displayMock, eventBusMock);

    assertEquals(1, configureViewStepPresenterSpy.getBindCount());
  }

  @Test
  public void testOnUnbind_UnbindsDependencies() {
    replay(displayMock, eventBusMock);
    presenter.unbind();

    verify(displayMock, eventBusMock);

    assertEquals(1, configureViewStepPresenterSpy.getUnbindCount());
  }

  private ConfigureViewStepPresenterSpy createConfigureViewStepPresenterSpy(EventBus eventBus) {
    ConfigureViewStepPresenter.Display displayMock = createMock(ConfigureViewStepPresenter.Display.class);

    return new ConfigureViewStepPresenterSpy(displayMock, eventBus);
  }

  private static class ConfigureViewStepPresenterSpy extends ConfigureViewStepPresenter {

    private int bindCount;

    private int unbindCount;

    public ConfigureViewStepPresenterSpy(ConfigureViewStepPresenter.Display display, EventBus eventBus) {
      super(display, eventBus);
    }

    protected void onBind() {
      bindCount++;
    }

    protected void onUnbind() {
      unbindCount++;
    }

    public int getBindCount() {
      return bindCount;
    }

    public int getUnbindCount() {
      return unbindCount;
    }
  }
}
