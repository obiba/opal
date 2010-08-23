/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.importdata.presenter;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.event.WorkbenchChangeEvent;
import org.obiba.opal.web.gwt.app.client.presenter.JobListPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.ImportData;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.HasText;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class ConclusionStepPresenter extends WidgetPresenter<ConclusionStepPresenter.Display> {

  public interface Display extends WidgetDisplay {

    HandlerRegistration addReturnClickHandler(ClickHandler handler);

    HandlerRegistration addJobLinkClickHandler(ClickHandler handler);

    HasText getJobLinkText();

  }

  @Inject
  private JobListPresenter jobListPresenter;

  // Provider used here to break a circular dependency.
  @Inject
  private Provider<FormatSelectionStepPresenter> formatSelectionStepPresenter;

  @Inject
  private ImportData importData;

  @Inject
  public ConclusionStepPresenter(final Display display, final EventBus eventBus) {
    super(display, eventBus);
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onBind() {
    addEventHandlers();
    getDisplay().getJobLinkText().setText(importData.getJobId());
  }

  protected void addEventHandlers() {
    super.registerHandler(getDisplay().addJobLinkClickHandler(new JobLinkClickHandler()));
    super.registerHandler(getDisplay().addReturnClickHandler(new ReturnClickHandler()));
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  @Override
  protected void onUnbind() {
  }

  @Override
  public void refreshDisplay() {
  }

  @Override
  public void revealDisplay() {
  }

  class JobLinkClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent arg0) {
      eventBus.fireEvent(new WorkbenchChangeEvent(jobListPresenter));
    }

  }

  class ReturnClickHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent arg0) {
      eventBus.fireEvent(new WorkbenchChangeEvent(formatSelectionStepPresenter.get()));
    }

  }

}
