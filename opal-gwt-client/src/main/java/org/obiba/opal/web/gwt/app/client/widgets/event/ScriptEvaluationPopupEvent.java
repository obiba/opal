/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.widgets.event;

import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

public class ScriptEvaluationPopupEvent extends GwtEvent<ScriptEvaluationPopupEvent.Handler> {

  private static Type<Handler> TYPE = new Type<Handler>();

  private String script;

  private VariableDto variable;

  private TableDto table;

  public ScriptEvaluationPopupEvent(VariableDto variable, TableDto table) {
    this.variable = variable;
    this.table = table;
  }

  public static Type<Handler> getType() {
    return TYPE;
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onScriptEvaluation(this);
  }

  public interface Handler extends EventHandler {
    public void onScriptEvaluation(ScriptEvaluationPopupEvent scriptEvaluationEvent);
  }

  public String getScript() {
    return script;
  }

  public TableDto getTable() {
    return table;
  }

  public VariableDto getVariable() {
    return variable;
  }

}
