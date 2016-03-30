/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.ui.wizard;

import java.util.ArrayList;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.ui.WizardStep;

import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.Widget;

public class BranchingWizardStepController extends DefaultWizardStepController {

  public static class Builder extends DefaultWizardStepController.Builder {

    private final BranchingWizardStepController ctrl;

    private Builder(BranchingWizardStepController ctrl) {
      super(ctrl);
      this.ctrl = ctrl;
    }

    public static Builder create(WizardStep step, Skippable skippable) {
      return new Builder(new BranchingWizardStepController(step, skippable));
    }

    public static Builder create(WizardStep step) {
      return create(step, null);
    }

    public Builder branch(DefaultWizardStepController next, Condition condition) {
      next.setPrevious(ctrl);
      ctrl.addNext(next, condition);
      return this;
    }

    public Builder branch(DefaultWizardStepController next, final HasValue<Boolean> hasValue) {
      return branch(next, new Condition() {
        @Override
        public boolean apply() {
          return hasValue.getValue();
        }
      });
    }
  }

  private final List<Candidate> nextCandidates = new ArrayList<>();

  public BranchingWizardStepController(WizardStep step, Skippable skippable) {
    super(step, skippable);
  }

  @Override
  public WizardStepController getNext() {
    for(Candidate candidate : nextCandidates) {
      if(candidate.condition.apply()) {
        return candidate.ctrl;
      }
    }
    return super.getNext();
  }

  @Override
  public boolean hasNext() {
    return !nextCandidates.isEmpty() || super.hasNext();
  }

  private void addNext(WizardStepController ctrl, Condition condition) {
    nextCandidates.add(new Candidate(condition, ctrl));
  }

  public interface Condition {
    boolean apply();
  }

  private static final class Candidate {

    private final Condition condition;

    private final WizardStepController ctrl;

    private Candidate(Condition condition, WizardStepController ctrl) {
      this.condition = condition;
      this.ctrl = ctrl;
    }
  }
}
