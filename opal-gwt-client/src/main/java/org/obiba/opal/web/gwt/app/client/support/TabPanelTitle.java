/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.support;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;

import com.google.gwt.core.client.GWT;

public abstract class TabPanelTitle {

  protected Translations translations = GWT.create(Translations.class);

  protected final String title;

  protected TabPanelTitle(String title) {
    this.title = title;
  }

}
