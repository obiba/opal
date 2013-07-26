package org.obiba.opal.web.gwt.app.client.report.event;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

public class ReportTemplateCanceledEvent extends GwtEvent<ReportTemplateCanceledEvent.Handler> {
  public interface Handler extends EventHandler {
    void onReportTemplateCanceled(ReportTemplateCanceledEvent event);
  }

  private static final GwtEvent.Type<Handler> TYPE = new GwtEvent.Type<Handler>();

  public ReportTemplateCanceledEvent() {
  }

  public static GwtEvent.Type<Handler> getType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onReportTemplateCanceled(this);
  }

  @Override
  public GwtEvent.Type<Handler> getAssociatedType() {
    return getType();
  }
}
