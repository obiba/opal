/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.ui;

import com.google.common.base.Strings;

/**
 * Single RQL query simple parser: expected statement is one that can be produced by criterion dropdowns.
 */
public class RQLIdentifierCriterionParser extends RQLCriterionParser {

  public RQLIdentifierCriterionParser() {
  }

  public RQLIdentifierCriterionParser(String query) {
    super(query);
  }

  @Override
  public boolean isValid() {
    return !Strings.isNullOrEmpty(fieldName) && "identifier".equals(fieldName);
  }
}
