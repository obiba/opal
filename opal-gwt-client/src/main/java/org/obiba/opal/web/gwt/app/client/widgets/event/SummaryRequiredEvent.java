/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.widgets.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 *
 */
public class SummaryRequiredEvent extends GwtEvent<SummaryRequiredEvent.Handler> {

  public interface Handler extends EventHandler {

    void onSummaryRequest(SummaryRequiredEvent event);

  }

  private static final Type<Handler> TYPE = new Type<Handler>();

  private final String resourceUri;

  private Integer max = null;

  public SummaryRequiredEvent(String resourceUri) {
    this.resourceUri = resourceUri;
  }

  public SummaryRequiredEvent(String resourceUri, Integer max) {
    this.resourceUri = resourceUri;
    this.max = max;
  }

  public String getResourceUri() {
    return resourceUri;
  }

  public Integer getMax() {
    return max;
  }

  public static Type<Handler> getType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onSummaryRequest(this);
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return getType();
  }
}
