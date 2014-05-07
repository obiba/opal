/*******************************************************************************
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package org.obiba.opal.web.gwt.app.client.ui.celltable;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * <p>
 * An {@link com.google.gwt.cell.client.AbstractCell} used to render an image. The String value is the url
 * of the image.
 * </p>
 * <p>
 * If the images being displayed are static or available at compile time, using
 * {@link com.google.gwt.cell.client.ImageResourceCell} will usually be more efficient.
 * </p>
 *
 * @see com.google.gwt.cell.client.ImageResourceCell
 */
public class StatusImageCell extends AbstractCell<String> {

  public final static String BULLET_GREEN = "status-success";

  public final static String BULLET_ORANGE = "status-warning";

  public final static String BULLET_RED = "status-error";

  public final static String BULLET_BLACK = "status-default";

  protected interface Template extends SafeHtmlTemplates {
    @Template("<i class=\"icon-circle {1}\" title=\"{0}\"></i>")
    SafeHtml img(String cssClass, String title);

    @Template(
        "<div class=\"progress progress-striped active\" title=\"{0} ({1})\"><div class=\"bar\" style=\"width: {1};\"></div></div>")
    SafeHtml progress(String status, String percent);
  }

  @SuppressWarnings("StaticNonFinalField")
  private static Template template;

  /**
   * Construct a new ImageCell.
   */
  @SuppressWarnings("AssignmentToStaticFieldFromInstanceMethod")
  public StatusImageCell() {
    if(template == null) {
      template = GWT.create(Template.class);
    }
  }

  /**
   * The value is expected to be encoded as TITLE:[STATUS|PROGRESS].
   * @param context
   * @param value
   * @param sb
   */
  @Override
  public void render(Context context, String value, SafeHtmlBuilder sb) {
    if(value != null) {
      String[] values = value.split(":");
      // The template will sanitize the URI.
      if(value.endsWith("%")) {
        sb.append(template.progress(values[0], values[1]));
      } else {
        sb.append(template.img(values[0], values[1]));
      }
    }
  }

}
