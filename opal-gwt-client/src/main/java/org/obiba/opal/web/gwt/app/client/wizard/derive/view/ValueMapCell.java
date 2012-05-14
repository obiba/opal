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

import org.obiba.opal.web.gwt.app.client.wizard.derive.view.ValueMapEntry.ValueMapEntryType;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;

/**
 *
 */
public abstract class ValueMapCell extends AbstractCell<ValueMapEntry> {

  interface Template extends SafeHtmlTemplates {

    @SafeHtmlTemplates.Template("<span class=\"{0}\">{1}</span>")
    SafeHtml span(String cssClass, SafeHtml cellContent);

  }

  protected static final Template template = GWT.create(Template.class);

  /**
   * @param consumedEvents
   */
  public ValueMapCell(Set<String> consumedEvents) {
    super(consumedEvents);
  }

  /**
   * @param consumedEvents
   */
  public ValueMapCell(String... consumedEvents) {
    super(consumedEvents);
  }

  @Override
  public void render(Cell.Context context, ValueMapEntry entry, SafeHtmlBuilder sb) {
    if(entry != null) {
      sb.append(template.span(getCssClasses(entry.getType()), SafeHtmlUtils.fromString(getText(entry))));
    }
  }

  protected abstract String getText(ValueMapEntry entry);

  protected String getCssClasses(ValueMapEntryType type) {
    switch(type) {
    case CATEGORY_NAME:
      return "category";
    case DISTINCT_VALUE:
      return "distinct";
    case RANGE:
      return "range";
    case OTHER_VALUES:
      return "special others";
    case EMPTY_VALUES:
      return "special empties";
    default:
      return "";
    }
  }

}
