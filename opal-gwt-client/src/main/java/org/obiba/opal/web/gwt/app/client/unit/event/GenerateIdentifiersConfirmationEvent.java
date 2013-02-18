/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.unit.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

public class GenerateIdentifiersConfirmationEvent extends GwtEvent<GenerateIdentifiersConfirmationEvent.Handler> {

  private static final Type<Handler> TYPE = new Type<Handler>();

  private final Number size;

  private final boolean allowZeros;

  private final String prefix;

  public interface Handler extends EventHandler {
    void onGenerateIdentifiersConfirmation(GenerateIdentifiersConfirmationEvent event);
  }

  public GenerateIdentifiersConfirmationEvent(Number size, boolean allowZeros, String prefix) {
    this.size = size;
    this.allowZeros = allowZeros;
    this.prefix = prefix;
  }

  public static Type<Handler> getType() {
    return TYPE;
  }


  public Number getSize() {
    return size;
  }

  public boolean isAllowZeros() {
    return allowZeros;
  }

  public String getPrefix() {
    return prefix;
  }

  @Override
  public GwtEvent.Type<Handler> getAssociatedType() {
    return getType();
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onGenerateIdentifiersConfirmation(this);
  }
}
