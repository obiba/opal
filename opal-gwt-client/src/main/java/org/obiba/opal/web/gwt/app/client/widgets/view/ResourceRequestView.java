/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.widgets.view;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.ResourceRequestPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.ResourceRequestPresenter.ResourceClickHandler;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 */
public class ResourceRequestView extends Composite implements ResourceRequestPresenter.Display {
  //
  // Constants
  //

  private static final String IN_PROGRESS_IMAGE_URL = "image/in-progress.gif";

  private static final String COMPLETED_IMAGE_URL = "image/20/tick.png";

  private static final String FAILED_IMAGE_URL = "image/20/error.png";

  //
  // Static Variables
  //

  private static ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);

  private static Translations translations = GWT.create(Translations.class);

  //
  // Instance Variables
  //

  @UiField
  Anchor resourceName;

  @UiField
  Label errorMessage;

  @UiField
  Image requestStatus;

  //
  // Constructors
  //

  public ResourceRequestView() {
    initWidget(uiBinder.createAndBindUi(this));
  }

  //
  // ResourceRequestPresenter.Display Methods
  //

  public void setResourceName(String resourceName) {
    this.resourceName.setText(resourceName);
  }

  public HandlerRegistration setResourceClickHandler(ResourceClickHandler handler) {
    return resourceName.addClickHandler(handler);
  }

  public void inProgress() {
    requestStatus.setUrl(IN_PROGRESS_IMAGE_URL);
    resourceName.setEnabled(false);
  }

  public void completed() {
    requestStatus.setUrl(COMPLETED_IMAGE_URL);
    resourceName.setEnabled(true);
  }

  public void failed() {
    requestStatus.setUrl(FAILED_IMAGE_URL);
    resourceName.setEnabled(false);
  }

  public Widget asWidget() {
    return this;
  }

  @Override
  public void startProcessing() {
  }

  @Override
  public void stopProcessing() {
  }

  @Override
  public void showErrorMessage(String status) {
    String errorMessage = translations.userMessageMap().get(status);
    if(errorMessage == null) {
      errorMessage = status;
    }
    this.errorMessage.setText(errorMessage);
  }

  //
  // Inner Classes / Interfaces
  //

  @UiTemplate("ResourceRequestView.ui.xml")
  interface ViewUiBinder extends UiBinder<HTMLPanel, ResourceRequestView> {
  }

}
