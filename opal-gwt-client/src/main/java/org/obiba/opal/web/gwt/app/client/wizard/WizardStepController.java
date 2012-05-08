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

import org.obiba.opal.web.gwt.app.client.workbench.view.WizardStep;

import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 */
public interface WizardStepController {

  WizardStep getStep();

  Widget getHelp();

  boolean hasNext();

  boolean hasPrevious();

  void onStepIn();

  WizardStepController onNext();

  WizardStepController onPrevious();

  void reset();

  boolean validate();

  boolean isConclusion();

  boolean isFinish();

  boolean shouldSkip();

  void addSteps(HasWidgets widgetsContainer);

  void addSteps(HasWidgets widgetsContainer, boolean visible);

  interface WidgetProvider {

    Widget getWidget();

  }

  interface ResetHandler {

    void onReset();

  }

  interface StepInHandler {

    void onStepIn();

  }

}
