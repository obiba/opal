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

import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;

import com.google.gwt.event.shared.EventBus;

public abstract class AbstractValidationHandler implements ValidationHandler {

  protected abstract Set<FieldValidator> getValidators();

  private final EventBus eventBus;

  public AbstractValidationHandler(EventBus eventBus) {
    this.eventBus = eventBus;
  }

  @Override
  public boolean validate() {
    List<String> messages = new ArrayList<String>();
    List<String> args = new ArrayList<String>();

    String message;
    for(FieldValidator validator : getValidators()) {
      message = validator.validate();
      if(message != null) {
        messages.add(message);

        if(validator instanceof AbstractFieldValidator && !((AbstractFieldValidator) validator).getArgs().isEmpty()) {
          args.addAll(((AbstractFieldValidator) validator).getArgs());
        }
      }
    }

    if(messages.size() > 0) {
      eventBus.fireEvent(NotificationEvent.newBuilder().error(messages).args(args).build());
      return false;
    } else {
      return true;
    }
  }

}