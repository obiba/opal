/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.wizard.derive.view;

import java.util.Set;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 *
 */
public class StatCell extends AbstractCell<ValueMapEntry> {

  interface Template extends SafeHtmlTemplates {

    @com.google.gwt.safehtml.client.SafeHtmlTemplates.Template("<div class='progress-bar' style='width: {0}px'><div class='inner'>{1}</div></div>")
    SafeHtml stat(int width, int value);
  }

  protected static Template template = GWT.create(Template.class);

  private final double maxFrequency;

  public StatCell(double maxFrequency, Set<String> consumedEvents) {
    super(consumedEvents);
    this.maxFrequency = maxFrequency;
  }

  public StatCell(double maxFrequency, String... consumedEvents) {
    super(consumedEvents);
    this.maxFrequency = maxFrequency;
  }

  @Override
  public void render(com.google.gwt.cell.client.Cell.Context context, ValueMapEntry entry, SafeHtmlBuilder sb) {
    if(entry != null) {
      double width = maxFrequency == 0 ? 0 : entry.getCount() * (100 / maxFrequency);
      sb.append(template.stat(Double.valueOf(width).intValue(), Double.valueOf(entry.getCount()).intValue()));
    }
  }
}
