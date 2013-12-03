/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.validator;

/**
 * Interface to represent the constants contained in resource bundle:
 * 'validation/ValidationMessages.properties'.
 */
public interface ValidationMessages extends org.hibernate.validator.ValidationMessages {

  @DefaultStringValue("must be unique")
  @Key("org.obiba.opal.core.validator.Unique.message")
  String org_obiba_opal_core_validator_Unique_message();

}
