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

import net.customware.gwt.presenter.client.widget.WidgetDisplay;

import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.opal.FunctionalUnitDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;

/**
 * Presenter elements common between the export and import dialog.
 */
public abstract class DataCommonPresenter {

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

    void renderConclusionStep(String jobId);

    void renderFormStep();
  }

}
