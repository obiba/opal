/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.event;

import org.obiba.opal.web.gwt.rest.client.UriBuilder;

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

  private final UriBuilder uri;

  private Integer max = null;

  private String datasource;

  private String table;

  private String variable;

  public SummaryRequiredEvent(UriBuilder uri, Integer max, String datasource, String table, String variable) {
    this.uri = uri;
    this.max = max;
    this.datasource = datasource;
    this.table = table;
    this.variable = variable;
  }

  public UriBuilder getResourceUri() {
    return uri;
  }

  public Integer getMax() {
    return max;
  }

  public static Type<Handler> getType() {
    return TYPE;
  }

  public String getDatasource() {
    return datasource;
  }

  public String getTable() {
    return table;
  }

  public String getVariable() {
    return variable;
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
