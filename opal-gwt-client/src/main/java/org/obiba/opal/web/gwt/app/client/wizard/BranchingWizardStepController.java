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

import java.util.ArrayList;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.workbench.view.WizardStep;

import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Widget;

public class BranchingWizardStepController extends DefaultWizardStepController {

  public static class Builder extends DefaultWizardStepController.Builder {

    private BranchingWizardStepController ctrl;

    private Builder(BranchingWizardStepController ctrl) {
      super(ctrl);
      this.ctrl = ctrl;
    }

    public static Builder create(WizardStep step, Widget help) {
      return new Builder(new BranchingWizardStepController(step, help));
    }

    public static Builder create(WizardStep step) {
      return create(step, null);
    }

    public Builder branch(DefaultWizardStepController next, HasValue<Boolean> condition) {
      next.setPrevious(ctrl);
      this.ctrl.addNext(next, condition);
      return this;
    }

  }

  private List<Candidate> nextCandidates = new ArrayList<Candidate>();

  /**
   * @param step
   * @param help
   */
  public BranchingWizardStepController(WizardStep step, Widget help) {
    super(step, help);
  }

  @Override
  public WizardStepController getNext() {
    for(Candidate candidate : nextCandidates) {
      if(candidate.condition.getValue()) {
        return candidate.ctrl;
      }
    }
    return super.getNext();
  }

  @Override
  public boolean hasNext() {
    return nextCandidates.size() > 0 || super.hasNext();
  }

  private final void addNext(WizardStepController ctrl, HasValue<Boolean> condition) {
    this.nextCandidates.add(new Candidate(condition, ctrl));
  }

  private static final class Candidate {
    private final HasValue<Boolean> condition;

    private final WizardStepController ctrl;

    public Candidate(HasValue<Boolean> condition, WizardStepController ctrl) {
      this.condition = condition;
      this.ctrl = ctrl;
    }
  }
}
