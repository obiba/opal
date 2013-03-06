/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.widgets.celltable;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.text.shared.AbstractSafeHtmlRenderer;

public class MultilineTextRenderer extends AbstractSafeHtmlRenderer<String> {

  @Override
  public SafeHtml render(String object) {
    if(object == null || object.trim().isEmpty()) return new SafeHtmlBuilder().toSafeHtml();
    return new SafeHtmlBuilder().appendEscapedLines(object).toSafeHtml();
  }
}