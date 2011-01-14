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

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 * Signals that a report template list has been received from the Opal server (i.e., in response to a client request).
 */
public class ReportTemplateListReceivedEvent extends GwtEvent<ReportTemplateListReceivedEvent.Handler> {
  //
  // Instance Variables
  //

  private static Type<Handler> TYPE;

  private final JsArray<ReportTemplateDto> reportTemplates;

  //
  // Constructors
  //

  public ReportTemplateListReceivedEvent(JsArray<ReportTemplateDto> reportTemplates) {
    this.reportTemplates = copyReportTemplates(reportTemplates);
  }

  //
  // GwtEvent Methods
  //

  @Override
  public com.google.gwt.event.shared.GwtEvent.Type<Handler> getAssociatedType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onReportTemplateListReceived(this);
  }

  //
  // Methods
  //

  public static Type<Handler> getType() {
    return TYPE != null ? TYPE : (TYPE = new Type<Handler>());
  }

  public JsArray<ReportTemplateDto> getReportTemplates() {
    return copyReportTemplates(reportTemplates);
  }

  @SuppressWarnings("unchecked")
  private JsArray<ReportTemplateDto> copyReportTemplates(JsArray<ReportTemplateDto> reportTemplates) {
    JsArray<ReportTemplateDto> copyOfReportTemplates = (JsArray<ReportTemplateDto>) JsArray.createArray();
    for(int i = 0; i < reportTemplates.length(); i++) {
      copyOfReportTemplates.push(reportTemplates.get(i));
    }
    return copyOfReportTemplates;
  }

  //
  // Inner Classes / Interfaces
  //

  public interface Handler extends EventHandler {
    void onReportTemplateListReceived(ReportTemplateListReceivedEvent event);
  }
}
