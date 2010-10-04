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
import org.obiba.opal.web.gwt.app.client.event.DatasourceSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.event.SiblingTableSelectionEvent;
import org.obiba.opal.web.gwt.app.client.event.TableSelectionChangeEvent;
import org.obiba.opal.web.gwt.app.client.presenter.DatasourcePresenter;
import org.obiba.opal.web.gwt.app.client.widgets.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.wizard.createview.presenter.CreateViewStepPresenter;
import org.obiba.opal.web.gwt.test.AbstractGwtTestSetup;
import org.obiba.opal.web.model.client.magma.TableDto;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Command;

public class DatasourcePresenterTest extends AbstractGwtTestSetup {

  private EventBus eventBusMock;

  private DatasourcePresenter.Display displayMock;

  private CreateViewStepPresenterSpy createViewStepPresenterSpy;

  private DatasourcePresenter datasourcePresenter;

  @Before
  public void setUp() {
    displayMock = createMock(DatasourcePresenter.Display.class);
    eventBusMock = createMock(EventBus.class);
    createViewStepPresenterSpy = createCreateViewStepPresenterSpy(eventBusMock);

    datasourcePresenter = new DatasourcePresenter(displayMock, eventBusMock, createViewStepPresenterSpy);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testThatEventHandlersAreAddedToUIComponents() throws Exception {
    HandlerRegistration handlerRegistrationMock = createMock(HandlerRegistration.class);
    expect(eventBusMock.addHandler((Type<TableSelectionChangeEvent.Handler>) EasyMock.anyObject(), (TableSelectionChangeEvent.Handler) EasyMock.anyObject())).andReturn(handlerRegistrationMock).once();
    expect(eventBusMock.addHandler((Type<DatasourceSelectionChangeEvent.Handler>) EasyMock.anyObject(), (DatasourceSelectionChangeEvent.Handler) EasyMock.anyObject())).andReturn(handlerRegistrationMock).once();
    expect(eventBusMock.addHandler((Type<ConfirmationEvent.Handler>) EasyMock.anyObject(), (ConfirmationEvent.Handler) EasyMock.anyObject())).andReturn(handlerRegistrationMock).once();
    expect(eventBusMock.addHandler((Type<SiblingTableSelectionEvent.Handler>) EasyMock.anyObject(), (SiblingTableSelectionEvent.Handler) EasyMock.anyObject())).andReturn(handlerRegistrationMock).once();

    displayMock.setExcelDownloadCommand((Command) EasyMock.anyObject());
    displayMock.setRemoveDatasourceCommand((Command) EasyMock.anyObject());
    displayMock.setAddViewCommand((Command) EasyMock.anyObject());
    displayMock.setNextCommand((Command) EasyMock.anyObject());
    displayMock.setPreviousCommand((Command) EasyMock.anyObject());
    displayMock.setTableNameFieldUpdater((FieldUpdater<TableDto, String>) EasyMock.anyObject());

    replay(displayMock, eventBusMock);
    datasourcePresenter.bind();

    verify(displayMock, eventBusMock);
  }

  private CreateViewStepPresenterSpy createCreateViewStepPresenterSpy(EventBus eventBus) {
    CreateViewStepPresenter.Display displayMock = createMock(CreateViewStepPresenter.Display.class);

    return new CreateViewStepPresenterSpy(displayMock, eventBus);
  }

  private static class CreateViewStepPresenterSpy extends CreateViewStepPresenter {

    private int bindCount;

    private int unbindCount;

    public CreateViewStepPresenterSpy(CreateViewStepPresenter.Display display, EventBus eventBus) {
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
