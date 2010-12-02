/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard;

import org.obiba.opal.web.gwt.app.client.wizard.event.WizardRequiredEvent;

/**
 *
 */
public interface Wizard {

  /**
   * Called by the {@link WizardManager} before binding and displaying the wizard.
   * 
   * The {@link WizardRequiredEvent} may contain information the wizard needs to configure itself prior to being
   * displayed.
   * 
   * @param event the event causing the wizard to be displayed
   */
  void onWizardRequired(WizardRequiredEvent event);

  void bind();

  void unbind();

  void revealDisplay();
}
