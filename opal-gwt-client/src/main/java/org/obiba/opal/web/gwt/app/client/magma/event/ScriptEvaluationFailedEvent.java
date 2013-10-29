/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.magma.event;

import java.util.Arrays;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.i18n.TranslationsUtils;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

public class ScriptEvaluationFailedEvent extends GwtEvent<ScriptEvaluationFailedEvent.Handler> {

  public interface Handler extends EventHandler {
    void onEvaluationFailed(ScriptEvaluationFailedEvent event);
  }

  private ScriptEvaluationFailedEvent() {
  }

  private static final Type<Handler> TYPE = new Type<Handler>();

  private String errorMessage;

  public static Type<Handler> getType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onEvaluationFailed(this);
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return getType();
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {

    private final ScriptEvaluationFailedEvent event = new ScriptEvaluationFailedEvent();
    private final StringBuilder messageBuilder = new StringBuilder();

    private Builder() {
    }

    public Builder error(String message, List<String> args) {
      messageBuilder.append(TranslationsUtils.replaceArguments(message, args));
      return this;
    }

    public Builder error(String message, String... args) {
      return error(message, Arrays.asList(args));
    }

    public ScriptEvaluationFailedEvent build() {
      event.errorMessage = messageBuilder.toString();
      return event;
    }

  }

}
