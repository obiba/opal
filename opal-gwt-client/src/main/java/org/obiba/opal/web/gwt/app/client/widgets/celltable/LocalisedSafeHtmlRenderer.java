/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.widgets.celltable;

import java.util.Map;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.text.shared.SafeHtmlRenderer;

/**
 *
 */
public class LocalisedSafeHtmlRenderer implements SafeHtmlRenderer<String> {

  private final Map<String, String> localisations;

  public LocalisedSafeHtmlRenderer(Map<String, String> localisation) {
    localisations = localisation;
  }

  @Override
  public void render(String object, SafeHtmlBuilder builder) {
    builder.append(localisedHtml(object));
  }

  @Override
  public SafeHtml render(String object) {
    return localisedHtml(object);
  }

  protected String localise(String key) {
    return localisations.get(key);
  }

  protected SafeHtml localisedHtml(String key) {
    return SafeHtmlUtils.fromTrustedString(localise(key));
  }

}
