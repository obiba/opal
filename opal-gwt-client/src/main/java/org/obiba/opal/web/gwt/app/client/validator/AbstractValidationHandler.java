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
import java.util.Set;

import net.customware.gwt.presenter.client.EventBus;

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;

public abstract class AbstractValidationHandler implements ValidationHandler {

  protected abstract Set<FieldValidator> getValidators();

  private final EventBus eventBus;

  public AbstractValidationHandler(EventBus eventBus) {
    super();
    this.eventBus = eventBus;
  }

  @Override
  public boolean validate() {
    List<String> messages = new ArrayList<String>();
    String message;
    for(FieldValidator validator : getValidators()) {
      message = validator.validate();
      if(message != null) {
        messages.add(message);
      }
    }

    if(messages.size() > 0) {
      eventBus.fireEvent(NotificationEvent.newBuilder().error(messages).build());
      return false;
    } else {
      return true;
    }
  }

}