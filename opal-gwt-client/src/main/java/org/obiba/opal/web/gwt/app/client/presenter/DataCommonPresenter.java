/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.presenter;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;

import org.obiba.opal.web.gwt.app.client.event.WorkbenchChangeEvent;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.opal.FunctionalUnitDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;

/**
 * Presenter elements common between the export and import dialog.
 */
public abstract class DataCommonPresenter {

  static class JobLinkClickHandler implements ClickHandler {

    private final EventBus eventBus;

    private final JobListPresenter jobListPresenter;

    public JobLinkClickHandler(EventBus eventBus, JobListPresenter jobListPresenter) {
      super();
      this.eventBus = eventBus;
      this.jobListPresenter = jobListPresenter;
    }

    @Override
    public void onClick(ClickEvent arg0) {
      eventBus.fireEvent(new WorkbenchChangeEvent(jobListPresenter));
    }
  }

  public interface Display extends WidgetDisplay {

    /** Set a collection of Opal datasources retrieved from Opal. */
    void setDatasources(JsArray<DatasourceDto> datasources);

    /** Get the Opal datasource selected by the user. */
    String getSelectedDatasource();

    /** Set a collection of Opal units retrieved from Opal. */
    void setUnits(JsArray<FunctionalUnitDto> units);

    /** Get the Opal unit selected by the user. */
    String getSelectedUnit();

    /** Get the form submit button. */
    HandlerRegistration addSubmitClickHandler(ClickHandler handler);

    /** Display the conclusion step */
    void renderConclusionStep(String jobId);

    /** Display the form step */
    void renderFormStep();

    /** Add a handler to the job list */
    HandlerRegistration addJobLinkClickHandler(ClickHandler handler);
  }

}
