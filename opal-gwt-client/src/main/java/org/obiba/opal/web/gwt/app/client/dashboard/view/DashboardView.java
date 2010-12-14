/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.dashboard.view;

import org.obiba.opal.web.gwt.app.client.dashboard.presenter.DashboardPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 */
public class DashboardView extends Composite implements DashboardPresenter.Display {

  @UiTemplate("DashboardView.ui.xml")
  interface ViewUiBinder extends UiBinder<Widget, DashboardView> {
  }

  //
  // Constants
  //

  //
  // Static Variables
  //

  private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  @UiField
  Label participantCount;

  @UiField
  Anchor exploreVariablesLink;

  @UiField
  Anchor exploreFilesLink;

  @UiField
  Anchor jobsLink;

  @UiField
  Anchor reportsLink;

  @UiField
  Anchor unitsLink;

  //
  // Instance Variables
  //

  //
  // Constructors
  //

  public DashboardView() {
    super();
    initWidget(uiBinder.createAndBindUi(this));
  }

  //
  // DashboardPresenter.Display Methods
  //

  @Override
  public Widget asWidget() {
    return this;
  }

  @Override
  public void startProcessing() {

  }

  @Override
  public void stopProcessing() {

  }

  public void setParticipantCount(int count) {
    participantCount.setText(String.valueOf(count));
  }

  public Anchor getExploreVariablesLink() {
    return exploreVariablesLink;
  }

  @Override
  public HasClickHandlers getExploreFilesLink() {
    return exploreFilesLink;
  }

  @Override
  public HasClickHandlers getJobListLink() {
    return jobsLink;
  }

  @Override
  public HasClickHandlers getReportsLink() {
    return reportsLink;
  }

  @Override
  public HasClickHandlers getUnitsLink() {
    return unitsLink;
  }

  //
  // Methods
  //

}
