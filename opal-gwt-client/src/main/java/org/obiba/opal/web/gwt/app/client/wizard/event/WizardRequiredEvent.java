/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 * Signals that a wizard is required for some purpose (import data, export data, etc.)
 * 
 * The <code>getWizardType</code> method returns the type of wizard required.
 */
public class WizardRequiredEvent extends GwtEvent<WizardRequiredEvent.Handler> {

  private final Type<Handler> type;

  private final Object[] eventParameters;

  //
  // Constructors
  //

  public WizardRequiredEvent(Type<Handler> type, Object... eventParameters) {
    this.type = type;
    if(eventParameters != null) {
      this.eventParameters = new Object[eventParameters.length];
      System.arraycopy(eventParameters, 0, this.eventParameters, 0, eventParameters.length);
    } else {
      this.eventParameters = new Object[0]; // return an empty array rather than null
    }
  }

  //
  // GwtEvent Methods
  //

  @Override
  protected void dispatch(Handler handler) {
    handler.onWizardRequired(this);
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return type;
  }

  public Object[] getEventParameters() {
    Object[] eventParametersCopy = new Object[eventParameters.length];
    System.arraycopy(eventParameters, 0, eventParametersCopy, 0, eventParameters.length);

    return eventParametersCopy;
  }

  //
  // Inner Classes / Interfaces
  //

  public interface Handler extends EventHandler {

    void onWizardRequired(WizardRequiredEvent event);
  }
}