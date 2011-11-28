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

import org.obiba.opal.web.gwt.app.client.widgets.event.ScriptEvaluationEvent;

import com.google.inject.Inject;

public class ScriptEvaluationPopupPresenter extends WidgetPresenter<ScriptEvaluationPopupPresenter.Display> {

  @Inject
  public ScriptEvaluationPopupPresenter(Display display, EventBus eventBus) {
    super(display, eventBus);
  }

  @Override
  public void refreshDisplay() {

  }

  @Override
  public void revealDisplay() {
    display.showDialog();
  }

  @Override
  protected void onBind() {
    addHandler();
  }

  @Override
  protected void onUnbind() {
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  private void addHandler() {
    super.registerHandler(eventBus.addHandler(ScriptEvaluationEvent.getType(), new EvaluationHandler()));
  }

  class EvaluationHandler implements ScriptEvaluationEvent.Handler {

    @Override
    public void onScriptEvaluation(ScriptEvaluationEvent scriptEvaluationEvent) {
      revealDisplay();
    }
  }

  public interface Display extends WidgetDisplay {

    void showDialog();

  }
}
