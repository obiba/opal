/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma;

import javax.validation.constraints.NotNull;

import org.obiba.opal.web.gwt.app.client.magma.derive.ScriptEvaluationPresenter;
import org.obiba.opal.web.gwt.app.client.magma.event.ScriptEvaluationFailedEvent;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.app.client.ui.ModalUiHandlers;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;

public class ScriptEvaluationPopupPresenter extends ModalPresenterWidget<ScriptEvaluationPopupPresenter.Display> {

  private final ScriptEvaluationPresenter scriptEvaluationPresenter;

  @Inject
  public ScriptEvaluationPopupPresenter(EventBus eventBus, Display view,
      ScriptEvaluationPresenter scriptEvaluationPresenter) {
    super(eventBus, view);
    this.scriptEvaluationPresenter = scriptEvaluationPresenter;
    this.scriptEvaluationPresenter.getView().setCommentVisible(false);
    getView().setUiHandlers(this);
  }

  public void initialize(TableDto table, VariableDto variable) {
    scriptEvaluationPresenter.setOriginalTable(table);
    scriptEvaluationPresenter.setOriginalVariable(variable);
  }

  @Override
  protected void onBind() {
    super.onBind();
    setInSlot(Display.Slots.Evaluation, scriptEvaluationPresenter);
    registerHandler(getEventBus().addHandler(ScriptEvaluationFailedEvent.getType(),
        new ScriptEvaluationFailedEvent.ScriptEvaluationFailedHandler() {
          @Override
          public void onScriptEvaluationFailed(ScriptEvaluationFailedEvent event) {
            getView().showError(event.getErrorMessage());
          }
        }));
  }

  public interface Display extends PopupView, HasUiHandlers<ModalUiHandlers> {
    enum Slots {
      Evaluation
    }

    void showError(@NotNull String error);
  }

}
