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

import org.obiba.opal.web.gwt.app.client.wizard.WizardType;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 * Signals that a wizard is required for some purpose (import data, export data, etc.)
 * 
 * The <code>getWizardType</code> method returns the type of wizard required.
 */
public class WizardRequiredEvent extends GwtEvent<WizardRequiredEvent.Handler> {
  //
  // Static Variables
  //

  private static Type<Handler> TYPE;

  //
  // Instance Variables
  //

  private final WizardType wizardType;

  //
  // Constructors
  //

  public WizardRequiredEvent(WizardType type) {
    wizardType = type;
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
    return TYPE;
  }

  //
  // Methods
  //

  public static Type<Handler> getType() {
    return TYPE != null ? TYPE : (TYPE = new Type<Handler>());
  }

  public WizardType getWizardType() {
    return wizardType;
  }

  //
  // Inner Classes / Interfaces
  //

  public interface Handler extends EventHandler {

    public void onWizardRequired(WizardRequiredEvent event);
  }
}