/*******************************************************************************
 * Copyright (c) 2011 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.i18n;

import com.google.gwt.i18n.client.LocalizableResource;
import com.google.gwt.i18n.client.Messages;

/**
 * Programmatically available localised text strings. This interface will be bound to localised properties files found
 * in the {@code com.google.gwt.i18n.client} package.
 */
@LocalizableResource.GenerateKeys
@LocalizableResource.Generate(format = "com.google.gwt.i18n.rebind.format.PropertiesFormat", locales = {"default"})
public interface TranslationMessages extends Messages {

  @Description("Existing derived category not mapped label")
  @DefaultMessage("Category {0} from existing derived variable is not mapped.")
  String destinationCategoryNotMapped(String categoryName);

  @Description("Error at line label")
  @DefaultMessage("Error at line {0}, column {1}: {2}.")
  String errorAt(int lineNumber, int columnNumber, String message);

}
