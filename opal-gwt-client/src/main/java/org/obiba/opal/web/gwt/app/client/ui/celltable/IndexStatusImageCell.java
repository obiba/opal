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

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.model.client.opal.TableIndexStatusDto;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

import static org.obiba.opal.web.model.client.opal.ScheduleType.NOT_SCHEDULED;
import static org.obiba.opal.web.model.client.opal.TableIndexationStatus.IN_PROGRESS;
import static org.obiba.opal.web.model.client.opal.TableIndexationStatus.OUTDATED;
import static org.obiba.opal.web.model.client.opal.TableIndexationStatus.UPTODATE;

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
public class IndexStatusImageCell extends AbstractCell<String> {

  private final static String BULLET_GREEN = "status-success";

  private final static String BULLET_ORANGE = "status-warning";

  private final static String BULLET_RED = "status-error";

  private final static String BULLET_BLACK = "status-default";

  private static final Translations translations = GWT.create(Translations.class);

  public static String getSrc(TableIndexStatusDto tableIndexStatusDto) {
    // In progress
    if(tableIndexStatusDto.getStatus().getName().equals(IN_PROGRESS.getName())) {
      return (int) (tableIndexStatusDto.getProgress() * 100) + "%";
    }
    // Up to date
    if(tableIndexStatusDto.getStatus().getName().equals(UPTODATE.getName())) {
      return BULLET_GREEN;
    }
    // Out dated but scheduled
    if(tableIndexStatusDto.getStatus().getName().equals(OUTDATED.getName()) &&
        !tableIndexStatusDto.getSchedule().getType().isScheduleType(NOT_SCHEDULED)) {
      return BULLET_ORANGE;
    }
    // Out dated but not scheduled
    if(tableIndexStatusDto.getStatus().getName().equals(OUTDATED.getName()) &&
        tableIndexStatusDto.getSchedule().getType().isScheduleType(NOT_SCHEDULED)) {
      return BULLET_RED;
    }
    // Unknown status
    return BULLET_BLACK;
  }

  public static String forSrc(String src) {
    if(src.equals(BULLET_GREEN)) return translations.indexUpToDate();

    if(src.equals(BULLET_ORANGE)) return translations.indexOutdatedScheduled();

    if(src.equals(BULLET_RED)) return translations.indexOutdatedNotScheduled();

    if(src.equals(BULLET_BLACK)) return translations.indexNotScheduled();

    return translations.indexInProgress();
  }

  interface Template extends SafeHtmlTemplates {
    @Template("<i class=\"icon-circle {0}\" title=\"{1}\"></i>")
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
  public IndexStatusImageCell() {
    if(template == null) {
      template = GWT.create(Template.class);
    }
  }

  @Override
  public void render(Context context, String value, SafeHtmlBuilder sb) {
    if(value != null) {
      // The template will sanitize the URI.
      if(value.endsWith("%")) {
        sb.append(template.progress(translations.indexInProgress(), value));
      } else {
        sb.append(template.img(value, forSrc(value)));
      }
    }
  }
}
