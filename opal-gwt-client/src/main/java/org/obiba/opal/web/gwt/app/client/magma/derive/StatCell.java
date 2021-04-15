/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.magma.derive;

import java.util.Set;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 *
 */
public class StatCell extends AbstractCell<ValueMapEntry> {

  interface Template extends SafeHtmlTemplates {

    @SafeHtmlTemplates.Template("<div>{0}</div>")
    SafeHtml stat(int value);
  }

  private final static Template template = GWT.create(Template.class);

  public StatCell(Set<String> consumedEvents) {
    super(consumedEvents);
  }

  public StatCell(String... consumedEvents) {
    super(consumedEvents);
  }

  @Override
  public void render(Cell.Context context, ValueMapEntry entry, SafeHtmlBuilder sb) {
    if(entry != null) {
      sb.append(template.stat(Double.valueOf(entry.getCount()).intValue()));
    }
  }
}
