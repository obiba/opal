/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.widgets.presenter;

import org.obiba.opal.web.gwt.app.client.wizard.derive.presenter.ScriptEvaluationPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.derive.presenter.ScriptEvaluationPresenter.ScriptEvaluationCallback;
import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.PopupView;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.proxy.RevealRootPopupContentEvent;

public class ScriptEvaluationPopupPresenter extends PresenterWidget<ScriptEvaluationPopupPresenter.Display> {

  private ScriptEvaluationPresenter scriptEvaluationPresenter;

  @Inject
  public ScriptEvaluationPopupPresenter(EventBus eventBus, Display view,
      ScriptEvaluationPresenter scriptEvaluationPresenter) {
    super(eventBus, view);
    this.scriptEvaluationPresenter = scriptEvaluationPresenter;
    this.scriptEvaluationPresenter.getView().setCommentVisible(false);
  }

  public void initialize(TableDto table, VariableDto variable) {
    scriptEvaluationPresenter.setOriginalTable(table);
    scriptEvaluationPresenter.setOriginalVariable(variable);
  }

  @Override
  protected void onBind() {
    super.onBind();
    setInSlot(Display.Slots.Evaluation, scriptEvaluationPresenter);
    scriptEvaluationPresenter.setScriptEvaluationCallback(new ScriptEvaluationCallback() {

      @Override
      public void onSuccess(VariableDto variable) {
        RevealRootPopupContentEvent.fire(getEventBus(), ScriptEvaluationPopupPresenter.this);
      }

      @Override
      public void onFailure(VariableDto variable) {
      }
    });
    addHandler();
  }

  private void addHandler() {
    registerHandler(getView().getButton().addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        getView().hide();
      }
    }));
  }

  public interface Display extends PopupView {

    enum Slots {
      Evaluation
    }

    HasClickHandlers getButton();
  }

}
