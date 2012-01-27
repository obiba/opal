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

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.model.client.magma.ValueSetsDto.ValueDto;

import com.google.gwt.core.client.GWT;

enum ValueRenderer {

  DATE {
    @Override
    public String render(ValueDto value) {
      String valueStr = super.render(value);
      return valueStr.isEmpty() ? valueStr : valueStr.substring(0, 10);
    }
  },
  DATETIME {
    @Override
    public String render(ValueDto value) {
      String valueStr = super.render(value);
      return valueStr.isEmpty() ? valueStr : valueStr.substring(0, 19).replace('T', ' ');
    }
  },
  BINARY {

    @Override
    public String render(ValueDto value) {
      return translations.downloadLabel();
    }
  },
  INTEGER, DECIMAL, BOOLEAN, LOCALE, TEXT;

  private static Translations translations = GWT.create(Translations.class);

  public String render(ValueDto value) {
    return value.getValue();
  }

}