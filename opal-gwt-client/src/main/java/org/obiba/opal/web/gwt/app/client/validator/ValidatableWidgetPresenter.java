/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.validator;

import java.util.ArrayList;
import java.util.List;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.presenter.NotificationPresenter.NotificationType;

public abstract class ValidatableWidgetPresenter<D extends WidgetDisplay> extends WidgetPresenter<D> {

  private List<FieldValidator> validators;

  public ValidatableWidgetPresenter(D display, EventBus eventBus) {
    super(display, eventBus);
    validators = new ArrayList<FieldValidator>();
  }

  protected boolean validate() {
    List<String> messages = new ArrayList<String>();
    String message;
    for(FieldValidator validator : validators) {
      message = validator.validate();
      if(message != null) {
        messages.add(message);
      }
    }

    if(messages.size() > 0) {
      eventBus.fireEvent(new NotificationEvent(NotificationType.ERROR, messages, null));
      return false;
    } else {
      return true;
    }
  }

  protected void addValidator(FieldValidator validator) {
    validators.add(validator);
  }

}
