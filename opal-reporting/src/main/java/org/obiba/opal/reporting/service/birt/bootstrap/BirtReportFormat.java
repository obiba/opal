/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.reporting.service.birt.bootstrap;

import org.eclipse.birt.report.engine.api.EXCELRenderOption;
import org.eclipse.birt.report.engine.api.HTMLRenderOption;
import org.eclipse.birt.report.engine.api.PDFRenderOption;
import org.eclipse.birt.report.engine.api.RenderOption;

/**
 *
 */
public enum BirtReportFormat {
  HTML("html") {
    @Override
    protected RenderOption newRenderOption() {
      HTMLRenderOption html = new HTMLRenderOption();
      // Setting this to true removes html and body tags
      html.setEmbeddable(false);
      return html;
    }

  },

  PDF("pdf") {
    @Override
    protected RenderOption newRenderOption() {
      return new PDFRenderOption();
    }
  },

  EXCEL("xls") {
    @Override
    protected RenderOption newRenderOption() {
      return new EXCELRenderOption();
    }
  };

  private final String birtFormatName;

  BirtReportFormat(String birtFormatName) {
    this.birtFormatName = birtFormatName;
  }

  public RenderOption createRenderOption() {
    RenderOption options = newRenderOption();
    options.setOutputFormat(birtFormatName);
    return options;
  }

  protected abstract RenderOption newRenderOption();
}
