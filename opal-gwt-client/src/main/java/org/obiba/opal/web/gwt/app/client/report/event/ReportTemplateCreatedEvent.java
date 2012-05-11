/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.report.event;

import org.obiba.opal.web.model.client.opal.ReportTemplateDto;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

public class ReportTemplateCreatedEvent extends GwtEvent<ReportTemplateCreatedEvent.Handler> {

  public interface Handler extends EventHandler {
    void onReportTemplateCreated(ReportTemplateCreatedEvent event);
  }

  private static final Type<Handler> TYPE = new Type<Handler>();

  private final ReportTemplateDto reportTemplate;

  public ReportTemplateCreatedEvent(ReportTemplateDto reportTemplate) {
    this.reportTemplate = reportTemplate;
  }

  public ReportTemplateDto getReportTemplate() {
    return reportTemplate;
  }

  public static Type<Handler> getType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onReportTemplateCreated(this);
  }

  @Override
  public GwtEvent.Type<Handler> getAssociatedType() {
    return getType();
  }
}
