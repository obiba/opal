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

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.widgets.event.ScriptEvaluationPopupEvent;
import org.obiba.opal.web.gwt.app.client.wizard.derive.presenter.ScriptEvaluationPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.derive.util.Variables.ValueType;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class ScriptEvaluationPopupPresenter extends WidgetPresenter<ScriptEvaluationPopupPresenter.Display> {

  @Inject
  private ScriptEvaluationPresenter scriptEvaluationPresenter;

  @Inject
  public ScriptEvaluationPopupPresenter(Display display, EventBus eventBus) {
    super(display, eventBus);
  }

  @Override
  public void refreshDisplay() {
    scriptEvaluationPresenter.refreshDisplay();
  }

  @Override
  public void revealDisplay() {
    display.showDialog();
    scriptEvaluationPresenter.refreshDisplay();
  }

  class CloseHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      display.closeDialog();
    }
  }

  @Override
  protected void onBind() {
    scriptEvaluationPresenter.bind();
    display.getButton().addClickHandler(new CloseHandler());
    display.addScriptEvaluationWidget(scriptEvaluationPresenter.getDisplay().asWidget());
    addHandler();
  }

  @Override
  protected void onUnbind() {
    scriptEvaluationPresenter.unbind();
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  private void addHandler() {
    super.registerHandler(eventBus.addHandler(ScriptEvaluationPopupEvent.getType(), new EvaluationHandler()));
  }

  class EvaluationHandler implements ScriptEvaluationPopupEvent.Handler {

    @Override
    public void onScriptEvaluation(ScriptEvaluationPopupEvent scriptEvaluationEvent) {
      scriptEvaluationPresenter.setTable(scriptEvaluationEvent.getTable());
      if(scriptEvaluationEvent.isScriptMode()) {
        scriptEvaluationPresenter.setScript(ValueType.TEXT.getLabel(), scriptEvaluationEvent.getScript(), false);
      } else {
        scriptEvaluationPresenter.setVariable(scriptEvaluationEvent.getVariable());
      }
      revealDisplay();
    }
  }

  public interface Display extends WidgetDisplay {

    void showDialog();

    void addScriptEvaluationWidget(Widget display);

    HasClickHandlers getButton();

    void closeDialog();

  }
}
