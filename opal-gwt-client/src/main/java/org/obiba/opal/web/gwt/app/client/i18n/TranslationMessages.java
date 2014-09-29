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

  @Description("Confirm remove category label")
  @DefaultMessage("Please confirm that you want to remove this category:<br />{0}")
  String confirmDeleteCategory(String name);

  @Description("Unknown Response")
  @DefaultMessage("[{0}] {1}")
  String unknownResponse(String statusText, String text);

  @Description("Edit User label")
  @DefaultMessage("Edit user {0}")
  String editUserLabel(String name);

  @Description("Clear Jobs List label")
  @DefaultMessage("Clear Jobs List")
  String clearJobsList();

  @Description("Cancel Job label")
  @DefaultMessage("Cancel Job")
  String cancelJob();

  @Description("Replace File label")
  @DefaultMessage("Replace File")
  String replaceExistingFile();

  @Description("Remove File label")
  @DefaultMessage("Remove File")
  String removeFile();

  @Description("Remove Key Pair label")
  @DefaultMessage("Remove Key Pair")
  String removeKeyPair();

  @Description("Remove Project label")
  @DefaultMessage("Remove Project")
  String removeProject();

  @Description("Archive Project label")
  @DefaultMessage("Archive Project")
  String archiveProject();

  @Description("Remove Datasource label")
  @DefaultMessage("Remove Datasource")
  String removeDatasource();

  @Description("Remove Report Template label")
  @DefaultMessage("Remove Report Template")
  String removeReportTemplate();

  @Description("Overwrite Variable label")
  @DefaultMessage("Overwrite Variable")
  String overwriteVariable();

  @Description("Overwrite View label")
  @DefaultMessage("Overwrite View")
  String overwriteView();

  @Description("Create View label")
  @DefaultMessage("Create View")
  String createView();

  @Description("Remove View label")
  @DefaultMessage("Remove View")
  String removeView();

  @Description("Remove Table label")
  @DefaultMessage("Remove Table")
  String removeTable();

  @Description("Remove Identifiers Table label")
  @DefaultMessage("Remove Identifiers Table")
  String removeIdentifiersTable();

  @Description("Remove Identifiers Mapping label")
  @DefaultMessage("Remove Identifiers Mapping")
  String removeIdentifiersMapping();

  @Description("Remove Derived Variable label")
  @DefaultMessage("Remove Derived Variable")
  String removeDerivedVariable();

  @Description("Remove Variable label")
  @DefaultMessage("Remove Variable")
  String removeVariable();

  @Description("Remove Aggregating Method label")
  @DefaultMessage("Remove Aggregating Method")
  String removeDataShieldAggregateMethod();

  @Description("Remove Assigning Method label")
  @DefaultMessage("Remove Assigning Method")
  String removeDataShieldAssignMethod();

  @Description("Remove Package label")
  @DefaultMessage("Remove Package")
  String removeDataShieldPackage();

  @Description("Publish Package Methods label")
  @DefaultMessage("Publish Package Methods")
  String publishDataShieldMethods();

  @Description("Unregister Database label")
  @DefaultMessage("Unregister Database")
  String unregisterDatabase();

  @Description("Remove Taxonomy label")
  @DefaultMessage("Remove Taxonomy")
  String removeTaxonomy();

  @Description("Remove Variables label")
  @DefaultMessage("Remove Variables")
  String removeVariables();

  @Description("Remove Tables label")
  @DefaultMessage("Remove Tables")
  String removeTables();

  @Description("Confirm remove group label")
  @DefaultMessage("Please confirm that you want to remove the group {0}.")
  String confirmRemoveGroup(String name);

  @Description("Confirm remove group with users label")
  @DefaultMessage(
      "Please confirm that you want to remove the group {0}. " +
          "The group {0} will be removed for users belonging to this group.")
  String confirmRemoveGroupWithUsers(String name);

  @Description("Confirm remove user label")
  @DefaultMessage("Please confirm that you want to remove the user {0}.")
  String confirmRemoveUser(String name);

  @Description("Confirm remove user profile label")
  @DefaultMessage("Please confirm that you want to remove the profile of user {0}. All its preferences will be lost.")
  String confirmRemoveUserProfile(String name);

  @Description("Vocabulary count label")
  @DefaultMessage("{0} vocabularies")
  @AlternateMessage({ "=0", "No vocabularies", "one", "1 vocabulary" })
  String vocabularyCount(@PluralCount int count);

  @Description("Term count label")
  @DefaultMessage("{0} terms")
  @AlternateMessage({ "=0", "No terms", "one", "1 term" })
  String termCount(@PluralCount int count);

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

  @Description("Not editable account text")
  @DefaultMessage("Your account was defined in the user directory \"{0}\". " +
      "Please contact your system administrator to change the password in this directory.")
  String accountNotEditable(String realm);

  @Description("Editable account text")
  @DefaultMessage("Your account password can be updated.")
  String accountEditable();

  @Description("Last Update Ago label")
  @DefaultMessage("Last update {0}")
  String lastUpdateAgoLabel(String date);

  @Description("You must select a file folder message")
  @DefaultMessage("You must select a {0}.")
  String mustSelectFileFolder(String name);

  @Description("You must select at least one file folder message")
  @DefaultMessage("You must select at least one {0}.")
  String mustSelectAtLeastFileFolder(String name);

  @Description("CellTable total count message")
  @DefaultMessage("Total {0}")
  String cellTableTotalCount(int count);

  @Description("Remove the currently displayed variable message")
  @DefaultMessage("Remove the currently displayed variable?")
  String confirmVariableDelete();

  @Description("Removing tables from the view will have an impact on which Variables can be defined message.")
  @DefaultMessage("Removing tables from the view will have an impact on which variables can be defined.")
  String removingTablesFromViewMayAffectVariables();

  @Description(
      "All the completed jobs(succeeded, failed or cancelled )will be removed from the jobs list. Currently running jobs will be unaffected.<br/><br/>Please confirm that you want to clear the jobs list message")
  @DefaultMessage(
      "All the completed jobs(succeeded, failed or cancelled) will be removed from the jobs list. Currently running jobs will be unaffected.<br/><br/>Please confirm that you want to clear the jobs list")
  String confirmClearJobsList();

  @Description(
      "The task will be cancelled. Changes will be rolled back as much as possible:although cancelled, a task might be partially completed.<br/><br/>Please confirm that you want cancel this task message")
  @DefaultMessage(
      "The task will be cancelled. Changes will be rolled back as much as possible:although cancelled, a task might be partially completed.<br/><br/>Please confirm that you want cancel this task")
  String confirmCancelJob();

  @Description(
      "The file that you are uploading already exist in the file system.<br/><br/>Please confirm that you want to replace the existing file message")
  @DefaultMessage(
      "The file that you are uploading already exist in the file system.<br/><br/>Please confirm that you want to replace the existing file")
  String confirmReplaceExistingFile();

  @Description("The file(s) will be removed from the file system. Please confirm message.")
  @DefaultMessage("The file(s) will be removed from the file system. Please confirm.")
  String confirmDeleteFile();

  @Description(
      "Please confirm that you want to remove the Key Pair. All associated encrypted material will not be accessible anymore message")
  @DefaultMessage(
      "Please confirm that you want to remove the Key Pair. All associated encrypted material will not be accessible anymore")
  String confirmDeleteKeyPair();

  @Description(
      "Please confirm that you want to remove permanently the current project and all associated data message.")
  @DefaultMessage("Please confirm that you want to remove permanently the current project and all associated data.")
  String confirmRemoveProject();

  @Description(
      "Please confirm that you want to remove the current project and keep all associated data message.")
  @DefaultMessage("Please confirm that you want to remove the current project and keep all associated data.")
  String confirmArchiveProject();

  @Description("Please confirm that you want to remove the current datasource from Opal configuration message")
  @DefaultMessage("Please confirm that you want to remove the current datasource from Opal configuration")
  String confirmRemoveDatasource();

  @Description(
      "Please confirm that you want to remove the current Report Template from Opal configuration (report design and generated reports will not be affected) message")
  @DefaultMessage(
      "Please confirm that you want to remove the current report template from Opal configuration (report design and generated reports will not be affected).")
  String confirmDeleteReportTemplate();

  @Description(
      "Please confirm that you want to remove the current Taxonomy from Opal configuration message")
  @DefaultMessage(
      "Please confirm that you want to remove the current taxonomy from Opal configuration (variable attributes will not be affected).")
  String confirmDeleteTaxonomy();

  @Description(
      "A variable with the same name already exists. Please confirm that you want to overwrite this variable message")
  @DefaultMessage(
      "A variable with the same name already exists. Please confirm that you want to overwrite this variable.")
  String confirmOverwriteVariable();

  @Description("A view with the same name already exists. Please confirm that you want to overwrite this view message")
  @DefaultMessage("A view with the same name already exists. Please confirm that you want to overwrite this view.")
  String confirmOverwriteView();

  @Description("Please confirm that you want to create a new view message")
  @DefaultMessage("Please confirm that you want to create a new view.")
  String confirmCreateView();

  @Description("Please confirm that you want to remove the current view message")
  @DefaultMessage("Please confirm that you want to remove the current view.")
  String confirmRemoveView();

  @Description(
      "Please confirm that you want to remove the current table. This cannot be undone and all data associated with " +
          "this table will be lost message")
  @DefaultMessage(
      "Please confirm that you want to remove the current table. This cannot be undone and all data associated with " +
          "this table will be lost.")
  String confirmRemoveTable();

  @Description(
      "Please confirm that you want to remove the current identifiers table. This cannot be undone and all identifiers " +
          "of this table will be lost message")
  @DefaultMessage(
      "Please confirm that you want to remove the current identifiers table. This cannot be undone and all identifiers " +
          "of this table will be lost.")
  String confirmRemoveIdentifiersTable();

  @Description(
      "Please confirm that you want to remove this identifiers mapping. This cannot be undone and all identifiers of " +
          "this mapping will be lost message")
  @DefaultMessage(
      "Please confirm that you want to remove this identifiers mapping. This cannot be undone and all identifiers of " +
          "this mapping will be lost.")
  String confirmRemoveIdentifiersMapping();

  @Description("Please confirm that you want to remove the current derived variable message")
  @DefaultMessage("Please confirm that you want to remove the current derived variable.")
  String confirmRemoveDerivedVariable();

  @Description(
      "Please confirm that you want to remove the current variable. This cannot be undone and all data associated with " +
          "this variable will be lost message")
  @DefaultMessage(
      "Please confirm that you want to remove the current variable. This cannot be undone and all data associated with " +
          "this variable will be lost.")
  String confirmRemoveVariable();

  @Description("Please confirm that you want to remove this assigning method message")
  @DefaultMessage("Please confirm that you want to remove this assigning method.")
  String confirmDeleteDataShieldAssignMethod();

  @Description("Please confirm that you want to remove this aggregating method message")
  @DefaultMessage("Please confirm that you want to remove this aggregating method.")
  String confirmDeleteDataShieldAggregateMethod();

  @Description("Please confirm that you want to remove this package and all its methods message")
  @DefaultMessage("Please confirm that you want to remove this package and all its methods.")
  String confirmDeleteDataShieldPackage();

  @Description("Please confirm that you want to publish this package methods message")
  @DefaultMessage("Please confirm that you want to publish this package methods.")
  String confirmPublishDataShieldMethods();

  @Description("Please confirm that you want to unregister this database message")
  @DefaultMessage("Please confirm that you want to unregister this database.")
  String confirmDeleteDatabase();

  @Description("Please confirm that you want to remove the taxonomy {0) message")
  @DefaultMessage("Please confirm that you want to remove the taxonomy {0}.")
  String confirmRemoveTaxonomy(String name);

  @Description("Please confirm that you want to remove {0} variables message")
  @DefaultMessage("Please confirm that you want to remove {0} variables.")
  @AlternateMessage({ "one", "Please confirm that you want to remove {0} variable." })
  String confirmRemoveVariables(@PluralCount int nb);

  @Description("Please confirm that you want to remove {0} tables message")
  @DefaultMessage("Please confirm that you want to remove {0} tables.")
  @AlternateMessage({ "one", "Please confirm that you want to remove {0} table." })
  String confirmRemoveTables(@PluralCount int nb);

  @Description("N Categories label")
  @DefaultMessage("{0} Categories")
  @AlternateMessage({ "one", "1 Category" })
  String nCategoriesLabel(@PluralCount int nb);

  @Description("N Attributes label")
  @DefaultMessage("{0} Attributes")
  @AlternateMessage({ "one", "1 Attribute" })
  String nAttributesLabel(@PluralCount int nb);

  @Description("N Tables label")
  @DefaultMessage("{0} Tables")
  @AlternateMessage({ "one", "1 Table" })
  String nTablesLabel(@PluralCount int nb);

  @Description("N Indices label")
  @DefaultMessage("{0} Indices")
  @AlternateMessage({ "one", "1 Index" })
  String nIndicesLabel(@PluralCount int nb);

  @Description("N Variables label")
  @DefaultMessage("{0} Variables")
  @AlternateMessage({ "one", "1 Variable" })
  String nVariablesLabel(@PluralCount int nb);

  @Description("Errors remaining message")
  @DefaultMessage("There are still {0} errors remaining. Please contact your administrator for further help.")
  String errorsRemainingMessage(int nb);

  @Description("/{0} entities label")
  @DefaultMessage("/{0} entities")
  @AlternateMessage({ "one", "/1 entity" })
  String summaryTotalEntitiesLabel(@PluralCount int nb);

  @Description("X tables are selected for being for being exported.")
  @DefaultMessage("{0} tables are selected for being exported.")
  @AlternateMessage({ "one", "1 table is selected for being exported." })
  String exportNTables(@PluralCount int nb);

  @Description("X tables are selected for being for being copied.")
  @DefaultMessage("{0} tables are selected for being copied.")
  @AlternateMessage({ "one", "1 table is selected for being copied." })
  String copyNTables(@PluralCount int nb);

  @Description("N Bookmarks label")
  @DefaultMessage("{0} Bookmarks")
  @AlternateMessage({ "one", "1 Bookmark" })
  String nBookmarksLabel(@PluralCount int nb);
}
