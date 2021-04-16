/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.system.news;

import com.google.common.base.Strings;

public class ObibaNewsDatum {
  private String date;
  private String title;
  private String summary;
  private String url;

  public ObibaNewsDatum() {
  }

  public void setDate(String date) {
    this.date = date;
  }

  public String getDate() {
    return date;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getTitle() {
    return title;
  }

  public void setSummary(String summary) {
    this.summary = summary;
  }

  public String getSummary() {
    return summary;
  }

  public boolean hasSummary() {
    return !Strings.isNullOrEmpty(summary);
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getUrl() {
    return url;
  }

  public String getLink() {
    return "https://www.obiba.org/pages/news/" + getUrl();
  }
}
