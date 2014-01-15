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
@LocalizableResource.Generate(format = "com.google.gwt.i18n.rebind.format.PropertiesFormat", locales = { "default" })
public interface TranslationMessages extends Messages {

  @Description("Existing derived category not mapped label")
  @DefaultMessage("Category {0} from existing derived variable is not mapped.")
  String destinationCategoryNotMapped(String categoryName);

  @Description("Error at line label")
  @DefaultMessage("Error at line {0}, column {1}: {2}.")
  String errorAt(int lineNumber, int columnNumber, String message);

  @Description("Confirm delete category label")
  @DefaultMessage("Please confirm that you want to remove this category:<br />{0}")
  String confirmDeleteCategory(String name);

  @Description("Confirm delete attribute label")
  @DefaultMessage("Please confirm that you want to remove this attribute:<br />{0}")
  String confirmDeleteAttribute(String name);

  @Description("Unknown Response")
  @DefaultMessage("[{0}] {1}")
  String unknownResponse(String statusText, String text);

  @Description("Edit User label")
  @DefaultMessage("Edit user {0}")
  String editUserLabel(String name);

  @Description("Confirm remove group label")
  @DefaultMessage("Please confirm that you want to remove the group {0}.")
  String confirmRemoveGroup(String name);

  @Description("Confirm remove group with users label")
  @DefaultMessage(
      "Please confirm that you want to remove the group {0}.<br />" +
          "The group {0} will be removed for users belonging to this group.")
  String confirmRemoveGroupWithUsers(String name);

  @Description("Confirm remove user label")
  @DefaultMessage("Please confirm that you want to remove the user {0}.")
  String confirmRemoveUser(String name);

  @Description("Table count label")
  @DefaultMessage("{0} tables")
  @AlternateMessage({ "=0", "No tables", "one", "1 table" })
  String tableCount(@PluralCount int count);

  @Description("Variable count label")
  @DefaultMessage("{0} variables")
  @AlternateMessage({ "=0", "No variables", "one", "1 variable" })
  String variableCount(@PluralCount int count);

  @Description("Entities count label")
  @DefaultMessage("{0} entities")
  @AlternateMessage({ "=0", "No entities", "one", "1 entity" })
  String entityCount(@PluralCount int count);

  @Description("Category name duplicated label")
  @DefaultMessage("Duplicated category name {0}.")
  String categoryNameDuplicated(String name);

  @Description("Update {0} categories label")
  @DefaultMessage("Update {0} categories")
  String updateVariableCategories(String name);
}
