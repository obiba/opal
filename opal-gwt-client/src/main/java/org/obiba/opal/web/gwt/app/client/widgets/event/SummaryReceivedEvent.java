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

import org.obiba.opal.web.model.client.math.SummaryStatisticsDto;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 *
 */
public class SummaryReceivedEvent extends GwtEvent<SummaryReceivedEvent.Handler> {

  public interface Handler extends EventHandler {

    void onSummaryReceived(SummaryReceivedEvent event);

  }

  private static Type<Handler> TYPE;

  private final String resourceUri;

  private final SummaryStatisticsDto summary;

  public SummaryReceivedEvent(String resourceUri, SummaryStatisticsDto summary) {
    this.resourceUri = resourceUri;
    this.summary = summary;
  }

  public String getResourceUri() {
    return resourceUri;
  }

  public SummaryStatisticsDto getSummary() {
    return summary;
  }

  public static Type<Handler> getType() {
    return TYPE != null ? TYPE : (TYPE = new Type<Handler>());
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onSummaryReceived(this);
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }
}
