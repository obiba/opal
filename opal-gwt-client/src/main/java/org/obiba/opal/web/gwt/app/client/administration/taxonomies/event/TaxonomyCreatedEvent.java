package org.obiba.opal.web.gwt.app.client.administration.taxonomies.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

public class TaxonomyCreatedEvent extends GwtEvent<TaxonomyCreatedEvent.Handler> {

  public interface Handler extends EventHandler {
    void onTaxonomyCreated(TaxonomyCreatedEvent event);
  }

  private static final Type<Handler> TYPE = new Type<Handler>();

  public TaxonomyCreatedEvent() {
  }

  public static Type<Handler> getType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onTaxonomyCreated(this);
  }

  @Override
  public GwtEvent.Type<Handler> getAssociatedType() {
    return getType();
  }
}
