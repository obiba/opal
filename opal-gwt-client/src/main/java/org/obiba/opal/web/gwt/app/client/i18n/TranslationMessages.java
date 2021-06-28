/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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

  @Description("Confirm remove category label")
  @DefaultMessage("Please confirm that you want to remove this category:<br />{0}")
  String confirmDeleteCategory(String name);

  @Description("Unknown Response")
  @DefaultMessage("[{0}] {1}")
  String unknownResponse(String statusText, String text);

  @Description("Edit User label")
  @DefaultMessage("Edit user {0}")
  String editUserLabel(String name);

  @Description("Edit ID Provider label")
  @DefaultMessage("Edit ID Provider {0}")
  String editIDProviderLabel(String name);

  @Description("Clear Jobs List label")
  @DefaultMessage("Clear Jobs List")
  String clearJobsList();

  @Description("Cancel Job label")
  @DefaultMessage("Cancel Job")
  String cancelJob();

  @Description("Terminate Session label")
  @DefaultMessage("Terminate Session")
  String terminateSession();

  @Description("Remove Workspace label")
  @DefaultMessage("Remove Workspace")
  String removeWorkspace();

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

  @Description("Reload Project label")
  @DefaultMessage("Reload Project")
  String reloadProject();

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

  @Description("DS profile cluster label")
  @DefaultMessage("The R server cluster associated to this profile is: {0}")
  String dataShieldProfileClusterInfo(String name);

  @Description("DS profile cluster missing label")
  @DefaultMessage("The R server cluster associated to this profile is missing: {0}")
  String dataShieldProfileClusterMissing(String name);

  @Description("DS profile added label")
  @DefaultMessage("DataSHIELD profile {0} added, initialized with {1} profile settings.")
  String dataShieldProfileAddedInfo(String name, String pName);

  @Description("DS profile R parser label")
  @DefaultMessage("The R parser version is: {0} ({1})")
  String dataShieldRParserInfo(String name, String description);

  @Description("Remove Aggregate Method label")
  @DefaultMessage("Remove Aggregate Method")
  String removeDataShieldAggregateMethod();

  @Description("Remove Aggregate Methods label")
  @DefaultMessage("Remove Aggregate Methods")
  String removeDataShieldAggregateMethods();

  @Description("Remove Assign Method label")
  @DefaultMessage("Remove Assign Method")
  String removeDataShieldAssignMethod();

  @Description("Remove Profile label")
  @DefaultMessage("Remove Profile")
  String removeDataShieldProfile();

  @Description("Remove DS Option label")
  @DefaultMessage("Remove Option")
  String removeDataShieldOption();

  @Description("Remove DS Options label")
  @DefaultMessage("Remove Options")
  String removeDataShieldOptions();

  @Description("Remove Assign Method label")
  @DefaultMessage("Remove Assign Methods")
  String removeDataShieldAssignMethods();

  @Description("Remove Package label")
  @DefaultMessage("Remove Package")
  String removeDataShieldPackage();

  @Description("Remove all DS Packages label")
  @DefaultMessage("Remove all Packages")
  String removeAllDataShieldPackages();

  @Description("Publish Package Settings label")
  @DefaultMessage("Publish Package Settings")
  String publishDataShieldSettings();

  @Description("UnPublish Package Settings label")
  @DefaultMessage("Unpublish Package Settings")
  String unPublishDataShieldSettings();

  @Description("Unregister Database label")
  @DefaultMessage("Unregister Database")
  String unregisterDatabase();

  @Description("Remove Taxonomy label")
  @DefaultMessage("Remove Taxonomy")
  String removeTaxonomy();

  @Description("Remove VCF Store Mapping Table label")
  @DefaultMessage("Remove Sample-Participant Mapping")
  String removeVCFStoreMappingTable();

  @Description("Remove VCF Files label")
  @DefaultMessage("Remove VCF Files")
  String removeVCFFile();

  @Description("Remove All Genotypes Data label")
  @DefaultMessage("Remove All Genotypes Data")
  String removeAllGenotypesData();

  @Description("Remove Vocabulary label")
  @DefaultMessage("Remove Vocabulary")
  String removeVocabulary();

  @Description("Remove Term label")
  @DefaultMessage("Remove Term")
  String removeTerm();

  @Description("Remove Variables label")
  @DefaultMessage("Remove Variables")
  String removeVariables();

  @Description("Delete Analysis label")
  @DefaultMessage("Delete Analysis")
  String deleteAnalysis();

  @Description("Remove Tables label")
  @DefaultMessage("Remove Tables")
  String removeTables();

  @Description("Remove Resources label")
  @DefaultMessage("Remove Resources")
  String removeResources();

  @Description("Remove Resource label")
  @DefaultMessage("Remove Resource")
  String removeResource();

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
  @DefaultMessage("Please confirm that you want to remove the profile of user {0}. All its settings will be lost: user permissions, personal access tokens and bookmarks.")
  String confirmRemoveUserProfile(String name);

  @Description("Confirm remove user profiles label")
  @DefaultMessage("Please confirm that you want to remove the profiles of the selected users. All their settings will be lost: user permissions, personal access tokens and bookmarks.")
  String confirmRemoveUserProfiles();

  @Description("Confirm remove id provider label")
  @DefaultMessage("Please confirm that you want to remove the identity provider {0}. None of its users will be able to login anymore.")
  String confirmRemoveIDProvider(String name);

  @Description("Confirm remove R package label")
  @DefaultMessage("Please confirm that you want to remove the R package {0}. Depending on its installation path (LibPath) it may not succeed.")
  String confirmRemoveRPackage(String name);

  @Description("Confirm update R packages label")
  @DefaultMessage("Please confirm that you want to update all the R packages to their latest version. It may take some time. If any error happens, look for possible reasons (system dependency missing, network connection failure etc.) by downloading the Rserve.log")
  String confirmUpdateRPackages();

  @Description("Confirm unregister app label")
  @DefaultMessage("Please confirm that you want to unregister the application {0}. Some system functionalities may be degraded.")
  String confirmUnregisterApp(String name);

  @Description("Confirm remove rock discovery label")
  @DefaultMessage("Please confirm that you want to remove the discovery of the Rock R server located at &lt;{0}&gt;. Note that this will not affect any corresponding app that would have been already registered.")
  String confirmRockConfigRemoval(String name);

  @Description("Vocabulary count label")
  @DefaultMessage("{0} vocabularies")
  @AlternateMessage({"=0", "No vocabularies", "one", "1 vocabulary"})
  String vocabularyCount(@PluralCount int count);

  @Description("Term count label")
  @DefaultMessage("{0} terms")
  @AlternateMessage({"=0", "No terms", "one", "1 term"})
  String termCount(@PluralCount int count);

  @Description("Table count label")
  @DefaultMessage("{0} tables")
  @AlternateMessage({"=0", "No tables", "one", "1 table"})
  String tableCount(@PluralCount int count);

  @Description("Table count label")
  @DefaultMessage("{0} VCFs")
  @AlternateMessage({"=0", "No VCF file", "one", "1 VCF file"})
  String vcfFilesCount(@PluralCount int count);

  @Description("Variable count label")
  @DefaultMessage("{0} variables")
  @AlternateMessage({"=0", "No variables", "one", "1 variable"})
  String variableCount(@PluralCount int count);

  @Description("Analysis count label")
  @DefaultMessage("{0} analyses")
  @AlternateMessage({"=0", "No analysis", "one", "1 analysis"})
  String analysisCount(@PluralCount int count);

  @Description("Analysis Result count label")
  @DefaultMessage("{0} results")
  @AlternateMessage({"=0", "No result", "one", "1 result"})
  String analysisResultCount(@PluralCount int count);

  @Description("Entities count label")
  @DefaultMessage("{0} entities")
  @AlternateMessage({"=0", "No entities", "one", "1 entity"})
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

  @Description("Delegated account text")
  @DefaultMessage("Your account password can be updated on the identity provider site.")
  String accountDelegated();

  @Description("Account groups text")
  @DefaultMessage("Your account belongs to the group(s): {0}.")
  String accountGroups(String groups);

  @Description("Account no group text")
  @DefaultMessage("Your account does not belong to any group.")
  String accountNoGroup();

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
      "Clear task list confirm message")
  @DefaultMessage(
      "All the completed jobs(succeeded, failed or cancelled) will be removed from the jobs list. Currently running jobs will be unaffected.<br/><br/>Please confirm that you want to clear the jobs list.")
  String confirmClearJobsList();

  @Description(
      "Cancel task confirm message")
  @DefaultMessage(
      "The task will be cancelled. Changes will be rolled back as much as possible: although cancelled, a task might be partially completed.<br/><br/>Please confirm that you want to cancel this task.")
  String confirmCancelJob();

  @Description(
      "Terminate R session confirm message")
  @DefaultMessage(
      "The R session will be terminated. Any pending computation will be stopped. Any data structure in R memory will be lost.<br/><br/>Please confirm that you want to terminate this R session.")
  String confirmTerminateRSession();

  @Description(
      "Remove R workspace confirm message")
  @DefaultMessage(
      "The R workspace will be removed. Any data files stored in this workspace will be lost.<br/><br/>Please confirm that you want to remove this R workspace.")
  String confirmRemoveRWorkspace();

  @Description(
      "Confirm file replacement")
  @DefaultMessage(
      "Some file that you are uploading already exist in the file system: {0}.<br/><br/>Please confirm that you want to replace the existing files.")
  String confirmReplaceExistingFile(String names);

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

  @Description(
      "Please confirm that you want to reload the database of the current project message.")
  @DefaultMessage("Please confirm that you want to reload the database of the current project.")
  String confirmReloadProject();

  @Description("Please confirm that you want to remove the current datasource from Opal configuration message")
  @DefaultMessage("Please confirm that you want to remove the current datasource from Opal configuration")
  String confirmRemoveDatasource();

  @Description(
      "Please confirm that you want to remove the current Report Template from Opal configuration (report design and generated reports will not be affected) message")
  @DefaultMessage(
      "Please confirm that you want to remove the current report template from Opal configuration (report design and generated reports will not be affected).")
  String confirmDeleteReportTemplate();

  @Description("Confirm remove a personal access token")
  @DefaultMessage("Please confirm that you want to remove the personal access token \"{0}\".")
  String confirmRemoveToken(String name);

  @Description(
      "Please confirm that you want to remove the current Taxonomy from Opal configuration message")
  @DefaultMessage(
      "Please confirm that you want to remove the current taxonomy from configuration (variable attributes will not be affected).")
  String confirmDeleteTaxonomy();

  @Description(
      "Please confirm that you want to remove the selected VCF files message")
  @DefaultMessage(
      "Please confirm that you want to remove the selected VCF files.")
  String confirmDeleteVCFFile();

  @Description(
      "Please confirm that you want to remove the Sample-Participant mapping message")
  @DefaultMessage(
      "Please confirm that you want to remove this sample-participant mapping. Note that the corresponding project''s table won''t be deleted.")
  String confirmDeleteVCFStoreMappingTable();

  @Description(
      "Please confirm that you want to remove the mapping table and all imported VCF files message")
  @DefaultMessage(
      "Please confirm that you want to remove the mapping table and all imported VCF files.")
  String confirmRemoveAllGenotypesData();

  @Description(
      "Please confirm that you want to remove the current Vocabulary message")
  @DefaultMessage(
      "Please confirm that you want to remove the current vocabulary (variable attributes will not be affected).")
  String confirmDeleteVocabulary();

  @Description(
      "Please confirm that you want to remove the current Term message")
  @DefaultMessage(
      "Please confirm that you want to remove the current term (variable attributes will not be affected).")
  String confirmDeleteTerm();

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

  @Description("Please confirm that you want to delete the current analysis and " +
      "its associated results. This cannot be undone and all data will be lost message")
  @DefaultMessage("Please confirm that you want to delete the current analysis and " +
      "its associated results. This cannot be undone and all data will be lost.")
  String confirmDeleteAnalysis();

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

  @Description("Please confirm that you want to remove the profile message")
  @DefaultMessage("Please confirm that you want to remove the profile <b>{0}</b>.")
  String confirmDeleteDataShieldProfile(String name);

  @Description("Please confirm that you want to remove this assign method message")
  @DefaultMessage("Please confirm that you want to remove the assign method <b>{0}</b>.")
  String confirmDeleteDataShieldAssignMethod(String name);

  @Description("Please confirm that you want to remove these assign methods message")
  @DefaultMessage("Please confirm that you want to remove the selected assign methods.")
  String confirmDeleteDataShieldAssignMethods();

  @Description("Please confirm that you want to remove this aggregate method message")
  @DefaultMessage("Please confirm that you want to remove the aggregate method <b>{0}</b>.")
  String confirmDeleteDataShieldAggregateMethod(String name);

  @Description("Please confirm that you want to remove these aggregate methods message")
  @DefaultMessage("Please confirm that you want to remove the selected aggregate methods.")
  String confirmDeleteDataShieldAggregateMethods();

  @Description("Please confirm that you want to remove these options message")
  @DefaultMessage("Please confirm that you want to remove these selected options.")
  String confirmDeleteDataShieldOptions();

  @Description("Please confirm that you want to remove these option message")
  @DefaultMessage("Please confirm that you want to remove the option <b>{0}</b>.")
  String confirmDeleteDataShieldOption(String name);

  @Description("Please confirm that you want to remove this package and all its methods and options message")
  @DefaultMessage("Please confirm that you want to remove this package and all its methods and options.")
  String confirmDeleteDataShieldPackage();

  @Description("Please confirm that you want to remove all the packages and all their methods and options message")
  @DefaultMessage("Please confirm that you want to remove all the packages and the associated methods and options.")
  String confirmDeleteAllDataShieldPackages();

  @Description("Please confirm that you want to publish this package methods and options message")
  @DefaultMessage("Please confirm that you want to publish <b>{0}</b> package declared methods and options in the profiles based on the <b>{1}</b> R servers.")
  String confirmPublishDataShieldSettings(String name, String profile);

  @Description("DS profile init message")
  @DefaultMessage("Initialize the settings of profile {0} with the selected packages declared methods and options.")
  String selectPackagesToInitProfile(String profile);

  @Description("Please confirm that you want to unpublish this package methods and options message")
  @DefaultMessage("Please confirm that you want to unpublish <b>{0}</b> package declared methods and options from the profiles based on the <b>{1}</b> R servers.")
  String confirmUnPublishDataShieldSettings(String name, String profile);

  @Description("Please confirm that you want to unregister this database message")
  @DefaultMessage("Please confirm that you want to unregister this database.")
  String confirmDeleteDatabase();

  @Description("Please confirm that you want to remove the taxonomy {0) message")
  @DefaultMessage("Please confirm that you want to remove the taxonomy {0}.")
  String confirmRemoveTaxonomy(String name);

  @Description("Please confirm that you want to remove the resource {0) message")
  @DefaultMessage("Please confirm that you want to remove the resource {0}.")
  String confirmRemoveResource(String name);

  @Description("Please confirm that you want to remove the resource {0) message")
  @DefaultMessage("Please confirm that you want to remove the resources: {0}.")
  String confirmRemoveResources(String names);

  @Description("Please confirm that you want to remove {0} variables message")
  @DefaultMessage("Please confirm that you want to remove {0} variables.")
  @AlternateMessage({"one", "Please confirm that you want to remove {0} variable."})
  String confirmRemoveVariables(@PluralCount int nb);

  @Description("Please confirm that you want to remove {0} tables message")
  @DefaultMessage("Please confirm that you want to remove {0} tables.")
  @AlternateMessage({"one", "Please confirm that you want to remove {0} table."})
  String confirmRemoveTables(@PluralCount int nb);

  @Description("N Categories label")
  @DefaultMessage("{0} Categories")
  @AlternateMessage({"one", "1 Category"})
  String nCategoriesLabel(@PluralCount int nb);

  @Description("N VCFs label")
  @DefaultMessage("{0} VCF files")
  @AlternateMessage({"one", "1 VCF file"})
  String nVCFsLabel(@PluralCount int nb);

  @Description("N Attributes label")
  @DefaultMessage("{0} Attributes")
  @AlternateMessage({"one", "1 Attribute"})
  String nAttributesLabel(@PluralCount int nb);

  @Description("N Tables label")
  @DefaultMessage("{0} Tables")
  @AlternateMessage({"one", "1 Table"})
  String nTablesLabel(@PluralCount int nb);

  @Description("N view count label")
  @DefaultMessage("({0} views)")
  @AlternateMessage({"one", "(1 view)"})
  String nViewCountLabel(@PluralCount int nb);

  @Description("N derived variable count label")
  @DefaultMessage("({0} derived variables)")
  @AlternateMessage({"one", "(1 derived variable)"})
  String nDerivedVariableCountLabel(@PluralCount int nb);

  @Description("N Resources label")
  @DefaultMessage("{0} Resources")
  @AlternateMessage({"one", "1 Resource"})
  String nResourcesLabel(@PluralCount int nb);

  @Description("N DS methods label")
  @DefaultMessage("{0} Methods")
  @AlternateMessage({"one", "1 Method"})
  String nDataShieldMethodsLabel(@PluralCount int nb);

  @Description("N Subject profiles label")
  @DefaultMessage("{0} Profiles")
  @AlternateMessage({"one", "1 Profile"})
  String nSubjectProfilesLabel(@PluralCount int nb);

  @Description("N Indices label")
  @DefaultMessage("{0} Indices")
  @AlternateMessage({"one", "1 Index"})
  String nIndicesLabel(@PluralCount int nb);

  @Description("N Variables label")
  @DefaultMessage("{0} Variables")
  @AlternateMessage({"one", "1 Variable"})
  String nVariablesLabel(@PluralCount int nb);

  @Description("Errors remaining message")
  @DefaultMessage("There are still {0} errors remaining. <a href=\"{1}\" target=\"_blank\">Click to download the list of errors</a>. Please contact your administrator for further help. ")
  String errorsRemainingMessage(int nb, String downloadUrl);

  @Description("/{0} entities label")
  @DefaultMessage("/{0} entities")
  @AlternateMessage({"one", "/1 entity"})
  String summaryTotalEntitiesLabel(@PluralCount int nb);

  @Description("X tables are selected for being for being exported.")
  @DefaultMessage("{0} tables are selected for being exported.")
  @AlternateMessage({"one", "1 table is selected for being exported."})
  String exportNTables(@PluralCount int nb);

  @Description("X VCF files are selected for being for being exported.")
  @DefaultMessage("{0} VCF files are selected for being exported.")
  @AlternateMessage({"one", "1 VCF file is selected for being exported."})
  String exportNVCFs(@PluralCount int nb);

  @Description("X tables are selected for being for being copied.")
  @DefaultMessage("{0} tables are selected for being copied.")
  @AlternateMessage({"one", "1 table is selected for being copied."})
  String copyNTables(@PluralCount int nb);

  @Description("N Bookmarks label")
  @DefaultMessage("{0} Bookmarks")
  @AlternateMessage({"one", "1 Bookmark"})
  String nBookmarksLabel(@PluralCount int nb);
}
