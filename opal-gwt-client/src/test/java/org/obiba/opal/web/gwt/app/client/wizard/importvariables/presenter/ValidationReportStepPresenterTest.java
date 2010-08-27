/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.importvariables.presenter;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;
import net.customware.gwt.presenter.client.EventBus;

import org.easymock.EasyMock;
import org.easymock.IArgumentMatcher;
import org.junit.Before;
import org.junit.Test;
import org.obiba.opal.web.gwt.app.client.event.WorkbenchChangeEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileDownloadEvent;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.inject.Provider;

/**
 * Unit tests for {@link ValidationReportStepPresenter}.
 */
public class ValidationReportStepPresenterTest {
  //
  // Instance Variables
  //

  private ValidationReportStepPresenter.Display displayMock;

  private EventBus eventBusMock;

  private UploadVariablesStepPresenter uploadVariablesStepPresenter;

  //
  // Fixture Methods (setUp / tearDown)
  //

  @Before
  public void setUp() {
    displayMock = createMock(ValidationReportStepPresenter.Display.class);
    eventBusMock = createMock(EventBus.class);

    uploadVariablesStepPresenter = new UploadVariablesStepPresenter(null, eventBusMock);
  }

  //
  // Test Methods
  //

  @Test
  public void testConstructor() {
    // Setup
    replay(displayMock, eventBusMock);

    // Exercise
    ValidationReportStepPresenter sut = new ValidationReportStepPresenter(displayMock, eventBusMock);

    // Verify
    assertEquals(displayMock, sut.getDisplay());
    verify(displayMock, eventBusMock);
  }

  @Test
  public void testOnBind() {
    // Setup
    expect(displayMock.addCancelClickHandler((ClickHandler) anyObject())).andReturn(null).once();

    replay(displayMock, eventBusMock);

    // Exercise
    ValidationReportStepPresenter sut = new ValidationReportStepPresenter(displayMock, eventBusMock);
    sut.onBind();

    // Verify
    verify(displayMock, eventBusMock);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testCancelButton_FiresWorkbenchChangeEventToReturnToUploadVariablesStep() {
    // Setup
    Provider<UploadVariablesStepPresenter> uploadVariablesStepPresenterProviderMock = createMockUploadVariablesStepProvider();

    eventBusMock.fireEvent(eqWorkbenchChangeEvent(new WorkbenchChangeEvent(uploadVariablesStepPresenterProviderMock.get())));
    expectLastCall().once();

    replay(displayMock, eventBusMock);

    ValidationReportStepPresenter presenter = new ValidationReportStepPresenter(displayMock, eventBusMock, uploadVariablesStepPresenterProviderMock);

    // Exercise
    ValidationReportStepPresenter.CancelClickHandler sut = presenter.new CancelClickHandler();
    sut.onClick(new DummyClickEvent());

    // Verify
    verify(eventBusMock, displayMock);
  }

  //
  // Methods
  //

  @SuppressWarnings("unchecked")
  private Provider<UploadVariablesStepPresenter> createMockUploadVariablesStepProvider() {
    Provider<UploadVariablesStepPresenter> uploadVariablesStepPresenterProviderMock = createMock(Provider.class);
    expect(uploadVariablesStepPresenterProviderMock.get()).andReturn(uploadVariablesStepPresenter).atLeastOnce();

    replay(uploadVariablesStepPresenterProviderMock);

    return uploadVariablesStepPresenterProviderMock;
  }

  //
  // Inner Classes
  //

  private static class DummyClickEvent extends ClickEvent {
  }

  private static class WorkbenchChangeEventMatcher implements IArgumentMatcher {

    private WorkbenchChangeEvent expected;

    public WorkbenchChangeEventMatcher(WorkbenchChangeEvent expected) {
      this.expected = expected;
    }

    @Override
    public boolean matches(Object actual) {
      if(actual instanceof WorkbenchChangeEvent) {
        return ((WorkbenchChangeEvent) actual).getWorkbench().equals(expected.getWorkbench());
      } else {
        return false;
      }
    }

    @Override
    public void appendTo(StringBuffer buffer) {
      buffer.append("eqWorkbenchChangeEvent(");
      buffer.append(expected.getClass().getName());
      buffer.append(" with workbench \"");
      buffer.append(expected.getWorkbench().getClass().getName());
      buffer.append("\")");
    }

  }

  private static FileDownloadEvent eqWorkbenchChangeEvent(WorkbenchChangeEvent in) {
    EasyMock.reportMatcher(new WorkbenchChangeEventMatcher(in));
    return null;
  }
}
