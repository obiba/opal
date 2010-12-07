/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.i18n;

import java.util.Map;

import com.google.gwt.i18n.client.Constants;
import com.google.gwt.i18n.client.LocalizableResource.Generate;
import com.google.gwt.i18n.client.LocalizableResource.GenerateKeys;

/**
 * Programmatically available localised text strings. This interface will be bound to localised properties files found
 * in the {@code com.google.gwt.i18n.client} package.
 */
@GenerateKeys
@Generate(format = "com.google.gwt.i18n.rebind.format.PropertiesFormat", locales = { "default" })
public interface Translations extends Constants {

  @Description("Error dialog title")
  @DefaultStringValue("Errors")
  String errorDialogTitle();

  @Description("Error dialog title when used to display warnings")
  @DefaultStringValue("Warnings")
  String warningDialogTitle();

  @Description("Error dialog title when used to display information")
  @DefaultStringValue("Information")
  String infoDialogTitle();

  @Description("Name label")
  @DefaultStringValue("Name")
  String nameLabel();

  @Description("Value Type label")
  @DefaultStringValue("Value Type")
  String valueTypeLabel();

  @Description("Label label")
  @DefaultStringValue("Label")
  String labelLabel();

  @Description("ID label")
  @DefaultStringValue("ID")
  String idLabel();

  @Description("Type label")
  @DefaultStringValue("Type")
  String typeLabel();

  @Description("User label")
  @DefaultStringValue("User")
  String userLabel();

  @Description("Start label")
  @DefaultStringValue("Start")
  String startLabel();

  @Description("End label")
  @DefaultStringValue("End")
  String endLabel();

  @Description("Status label")
  @DefaultStringValue("Status")
  String statusLabel();

  @Description("Status map")
  @DefaultStringMapValue( { "NOT_STARTED", "Not Started", //
  "IN_PROGRESS", "In Progress", //
  "SUCCEEDED", "Succeeded", //
  "FAILED", "Failed", //
  "CANCEL_PENDING", "Cancel Pending", //
  "CANCELED", "Cancelled", //
  "DatasourceCreationFailed", "The datasource creation has failed." })
  Map<String, String> statusMap();

  @Description("Actions label")
  @DefaultStringValue("Actions")
  String actionsLabel();

  @Description("Action map")
  @DefaultStringMapValue( { "Log", "Log", //
  "Cancel", "Cancel", //
  "Delete", "Delete", //
  "Edit", "Edit", //
  "Download", "Download" })
  Map<String, String> actionMap();

  @Description("Size label")
  @DefaultStringValue("Size")
  String sizeLabel();

  @Description("Last modified label")
  @DefaultStringValue("Last Modified")
  String lastModifiedLabel();

  @Description("Date label")
  @DefaultStringValue("Date")
  String dateLabel();

  @Description("Message label")
  @DefaultStringValue("Message")
  String messageLabel();

  @Description("Job label")
  @DefaultStringValue("Job")
  String jobLabel();

  @Description("Jobs menu item")
  @DefaultStringValue("Jobs")
  String jobsLabel();

  @Description("File system label")
  @DefaultStringValue("File System")
  String fileSystemLabel();

  @Description("Entity type label")
  @DefaultStringValue("Entity Type")
  String entityTypeLabel();

  @Description("Tables label")
  @DefaultStringValue("Tables")
  String tablesLabel();

  @Description("Table label")
  @DefaultStringValue("Table")
  String tableLabel();

  @Description("Variables label")
  @DefaultStringValue("Variables")
  String variablesLabel();

  @Description("Variable label")
  @DefaultStringValue("Variable")
  String variableLabel();

  @Description("Unit label")
  @DefaultStringValue("Unit")
  String unitLabel();

  @Description("User message map")
  @DefaultStringMapValue( { //
  "VariableNameNotUnique", "The specified variable name already exists.", //
  "CategoryDialogNameRequired", "A category name is required.", //
  "CategoryNameAlreadyExists", "The specified category name already exists.", //
  "AttributeNameRequired", "An attribute name is required.", //
  "AttributeNameAlreadyExists", "The specified attribute name already exists.", //
  "BaseLanguageLabelRequired", "The base language field (marked with *) requires a value.", //
  "jobCancelled", "Job cancelled.", //
  "jobDeleted", "Job deleted.", //
  "completedJobsDeleted", "All completed jobs deleted.", //
  "SetCommandStatus_NotFound", "Job could not be cancelled (not found).", //
  "SetCommandStatus_BadRequest_IllegalStatus", "Job status cannot be set to the specified value.", //
  "SetCommandStatus_BadRequest_NotCancellable", "Job has completed and has already been cancelled.", //
  "DeleteCommand_NotFound", "Job could not be deleted (not found).", //
  "DeleteCommand_BadRequest_NotDeletable", "Job is currently running and therefore cannot be deleted at this time.", //
  "cannotCreateFolderPathAlreadyExist", "Could not create the folder, a folder or a file exist with that name at the specified path.", //
  "cannotCreateFolderParentIsReadOnly", "Could create the following folder because its parent folder is read-only.", //
  "cannotCreatefolderUnexpectedError", "There was an unexpected error while creating the folder.", //
  "cannotDeleteNotEmptyFolder", "This folder contains one or many file(s) and as a result cannot be deleted.", //
  "cannotDeleteReadOnlyFile", "Could delete the  file or folder because it is read-only.", //
  "couldNotDeleteFileError", "There was an error while deleting the file or folder.", //
  "datasourceMustBeSelected", "You must select a datasource.", //
  "fileReadError", "The file could not be read.", //
  "ViewNameRequired", "You must provide a name for the view.", //
  "ViewAlreadyExists", "A view with the same name already exists.", //
  "TableSelectionRequired", "You must select at least one table.", //
  "TableEntityTypesDoNotMatch", "The selected tables must all have the same entity type.", //
  "VariableDefinitionMethodRequired", "You must indicate how the view's variables are to be defined.", //
  "DatasourceNameRequired", "You must provide a name for the datasource.", //
  "DatasourceAlreadyExistsWithThisName", "A datasource already exists with this name.", //
  "ExcelFileRequired", "An Excel file is required.", "ExcelFileSuffixInvalid", //
  "Invalid Excel file suffix: .xls or .xlsx are expected.", //
  "ViewMustBeAttachedToExistingOrNewDatasource", "The view must be attached to either an existing datasource or a new one.", //
  "DuplicateDatasourceName", "The datasource name is already in use. Please choose another.", //
  "UnknownError", "An unknown error has occurred.", //
  "InternalError", "An internal error has occurred. Please contact technical support.", //
  "DatasourceNameDisallowedChars", "Datasource names cannot contain colon or period characters.", //
  "ViewNameDisallowedChars", "View names cannot contain colon or period characters.", //
  "CSVFileRequired", "A CSV file is required.", //
  "XMLFileRequired", "An XML file is required.", //
  "XMLFileSuffixInvalid", "Invalid XML file suffix: .xml is expected.", //
  "ZipFileRequired", "A Zip file is required.", //
  "ZipFileSuffixInvalid", "Invalid Zip file suffix: .zip is expected.",//
  "ReportTemplateWasNotFound", "The specified report template could not be found.",//
  "ReportJobStarted", "Report job has been launched.  You can follow its progress in the job list.",//
  "ReportTemplateAlreadyExistForTheSpecifiedName", "A report template already exist with the specified name.",//
  "BirtReportDesignFileIsRequired", "A BIRT Design File must be selected.",//
  "CronExpressionIsRequired", "A schedule expression must be specified.",//
  "NotificationEmailsAreInvalid", "One or more of the notifications emails specified are invalid.",//
  "ReportTemplateNameIsRequired", "A name is required for the report template.",//
  "OccurrenceGroupIsRequired", "An Occurence Group must be specified for Repeatable variables.",//
  "NewVariableNameIsRequired", "A name is required for the new variable to be created.",//
  "ScriptIsRequired", "A script is required.",//
  "CopyFromVariableNameIsRequired", "You must enter the name of a variable from which the new variable will be created from.",//
  "cannotSwitchTabBecauseOfUnsavedChanges", "You have unsaved changes. You need to press Save Changes before you can select another tab.",//
  "UrlRequired", "You must provide the database's URL.",//
  "UsernameRequired", "You must indicate the user name to be used for the database connection.",//
  "PasswordRequired", "You must indicate the password to be used for the database connection.", //
  "MustIndicateWhetherJdbcDatasourceShouldUseMetadataTables", "You must indicate whether meta-data tables are to be used or not.",//
  "RowMustBePositiveInteger", "Row must be a positive integer (greater than or equal to 1).",//
  "SpecificCharsetNotIndicated", "You have selected to use a specific character set but have not indicated which one.",//
  "NoDataFileSelected", "You must select a data file.",//
  "CharsetNotAvailable", "The character set you have specified is not available.",//
  "NotIgnoredConlicts", "Some conflicts were detected. Ignore modifications before applying changes.",//
  "NoVariablesToBeImported", "No variables are to be imported.",//
  "FunctionalUnitAlreadyExistWithTheSpecifiedName", "A unit with the same name already exists.",//
  "FunctionalUnitNameIsRequired", "Unit name is required.",//
  "KeyPairAliasIsRequired", "Alias is required",//
  "KeyPairAlgorithmIsRequired", "Algorithm is required.",//
  "KeyPairKeySizeIsRequired", "Size is required.",//
  "KeyPairPrivateKeyPEMIsRequired", "Private Key in PEM format is required.", //
  "KeyPairFirstAndLastNameIsRequired", "First and Last Name is required.",//
  "KeyPairOrganizationalUnitIsRequired", "Organizational Unit is required.",//
  "KeyPairOrganizationNameIsRequired", "Organization Name is required.",//
  "KeyPairCityNameIsRequired", "City or Locality Name is required.",//
  "KeyPairStateNameIsRequired", "State or Province Name is required.",//
  "KeyPairCountryCodeIsRequired", "Country Code is required.",//
  "KeyPairPublicKeyPEMIsRequired", "Public Key in PEM format is required."//
  })
  Map<String, String> userMessageMap();

  @Description("You must select a file message")
  @DefaultStringValue("You must select a file.")
  String fileMustBeSelected();

  @Description("Yes label")
  @DefaultStringValue("Yes")
  String yesLabel();

  @Description("No label")
  @DefaultStringValue("No")
  String noLabel();

  @Description("Missing label")
  @DefaultStringValue("Missing")
  String missingLabel();

  @Description("Categories label")
  @DefaultStringValue("Categories")
  String categoriesLabel();

  @Description("Attributes label")
  @DefaultStringValue("Attributes")
  String attributesLabel();

  @Description("Language label")
  @DefaultStringValue("Language")
  String languageLabel();

  @Description("Value label")
  @DefaultStringValue("Value")
  String valueLabel();

  @Description("Code label")
  @DefaultStringValue("Code")
  String codeLabel();

  @Description("Mime Type label")
  @DefaultStringValue("Mime Type")
  String mimeTypeLabel();

  @Description("Repeatable label")
  @DefaultStringValue("Repeatable")
  String repeatableLabel();

  @Description("Occurrence Group label")
  @DefaultStringValue("Occurrence Group")
  String occurrenceGroupLabel();

  @Description("Multiple table selection instructions")
  @DefaultStringValue("Select one or more tables:")
  String multipleTableSelectionInstructionsLabel();

  @Description("Single table selection instructions")
  @DefaultStringValue("Select one table:")
  String singleTableSelectionInstructionsLabel();

  @Description("Datasource label")
  @DefaultStringValue("Datasource")
  String datasourceLabel();

  @Description("Table selector title")
  @DefaultStringValue("Table selector")
  String tableSelectorTitle();

  @Description("Select all label")
  @DefaultStringValue("select all")
  String selectAllLabel();

  @Description("File Selector title")
  @DefaultStringValue("File Selector")
  String fileSelectorTitle();

  @Description("Log label")
  @DefaultStringValue("Log")
  String logLabel();

  @Description("Confirmation title map")
  @DefaultStringMapValue( { //
  "deleteVariableTitle", "Delete Variable", //
  "deleteTable", "Delete Table", //
  "clearJobsList", "Clear Jobs List", //
  "cancelJob", "Cancel Job", //
  "replaceExistingFile", "Replace File", //
  "deleteFile", "Delete File", //
  "deleteKeyPair", "Delete Key Pair", //
  "removeDatasource", "Remove Datasource",//
  "removeReportTemplate", "Remove Report Template",//
  "removeFunctionalUnit", "Remove Unit",//
  "deleteCategory", "Delete Category",//
  "deleteAttribute", "Delete Attribute",//
  "removeView", "Remove View" })
  Map<String, String> confirmationTitleMap();

  @Description("Confirmation message map")
  @DefaultStringMapValue( { //
  "confirmVariableDelete", "Delete the currently displayed variable?", //
  "removingTablesFromViewMayAffectVariables", "Removing tables from the view will have an impact on which Variables can be defined.", //
  "confirmClearJobsList", "All the completed jobs (succeeded, failed or cancelled) will be removed from the jobs list. Currently running jobs will be unaffected.<br /><br />Please confirm that you want to clear the jobs list.", //
  "confirmCancelJob", "The job will be cancelled. Changes will be rolled back as much as possible: although cancelled, a job might be partially completed.<br /><br />Please confirm that you want cancel this job.", //
  "confirmReplaceExistingFile", "The file that you are uploading already exist in the file system.<br /><br />Please confirm that you want to replace the existing file.", //
  "confirmDeleteFile", "The file will be removed from the file system.<br /><br />Please confirm that you want to delete this file.", //
  "confirmDeleteKeyPair", "Please confirm that you want to remove the Key Pair. All associated encrypted material will not be accessible anymore",//
  "confirmRemoveDatasource", "Please confirm that you want to remove the current datasource from Opal configuration (datasource content will not be affected).",//
  "confirmDeleteReportTemplate", "Please confirm that you want to remove the current Report Template from Opal configuration (report design and generated reports will not be affected).",//
  "confirmDeleteFunctionalUnit", "Please confirm that you want to remove the current Unit from Opal configuration. All encrypted material will not be accessible anymore",//
  "confirmDeleteCategory", "Please confirm that you want to remove this category.",//
  "confirmDeleteAttribute", "Please confirm that you want to remove this attribute.",//
  "confirmRemoveView", "Please confirm that you want to remove the current view." })
  Map<String, String> confirmationMessageMap();

  @Description("A name is required when creating a new folder")
  @DefaultStringValue("You must specify a folder name")
  String folderNameIsRequired();

  @Description("Dot names are not permitted")
  @DefaultStringValue("The names '.' and '..' are not permitted.")
  String dotNamesAreInvalid();

  @Description("Data export instructions")
  @DefaultStringValue("Select the tables to be exported.")
  String dataExportInstructions();

  @Description("Data export pending conclusion")
  @DefaultStringValue("Data export job is being launched.")
  String dataExportPendingConclusion();

  @Description("Data export completed conclusion")
  @DefaultStringValue("Data export job was successfully launched.")
  String dataExportCompletedConclusion();

  @Description("Data export failed conclusion")
  @DefaultStringValue("Data export job launch failed.")
  String dataExportFailedConclusion();

  @Description("Data export options")
  @DefaultStringValue("Select the export options.")
  String dataExportOptions();

  @Description("Data export destination")
  @DefaultStringValue("Select the destination of the exportation.")
  String dataExportDestination();

  @Description("Data export unit")
  @DefaultStringValue("Select the participant identifiers to be exported.")
  String dataExportUnit();

  @Description("Data import instructions")
  @DefaultStringValue("Select the file to be imported and the destination datasource.")
  String dataImportInstructions();

  @Description("Data import instructions conclusion")
  @DefaultStringValue("Data import job is launched.")
  String dataImportInstructionsConclusion();

  @Description("Export to Excel icon title")
  @DefaultStringValue("Export to Excel file")
  String exportToExcelTitle();

  @Description("Download View XML menu item")
  @DefaultStringValue("Download View XML")
  String downloadViewXML();

  @Description("Csv label")
  @DefaultStringValue("CSV")
  String csvLabel();

  @Description("Opal XML label")
  @DefaultStringValue("Opal XML")
  String opalXmlLabel();

  @Description("Row must be integer message")
  @DefaultStringValue("Row must be an integer.")
  String rowMustBeIntegerMessage();

  @Description("Row must be positive message")
  @DefaultStringValue("Row must must be a positive value.")
  String rowMustBePositiveMessage();

  @Description("Charset must not be null message")
  @DefaultStringValue("The character set must not be empty.")
  String charsetMustNotBeNullMessage();

  @Description("Charset does not exist message")
  @DefaultStringValue("The specified character set could not be found.")
  String charsetDoesNotExistMessage();

  @Description("Sheet label")
  @DefaultStringValue("Sheet")
  String sheetLabel();

  @Description("Row number label")
  @DefaultStringValue("Row Number")
  String rowNumberLabel();

  @Description("Error label")
  @DefaultStringValue("Error")
  String errorLabel();

  @Description("Datasource parsing error map")
  @DefaultStringMapValue( { "CategoryNameRequired", "Category name required", //
  "CategoryVariableNameRequired", "Category variable name required", //
  "DuplicateCategoryName", "Duplicate category name", //
  "DuplicateColumns", "Duplicate columns", //
  "DuplicateVariableName", "Duplicate variable name", //
  "TableDefinitionErrors", "Table definition errors", //
  "UnexpectedErrorInCategory", "Unexpected error in category", //
  "UnexpectedErrorInVariable", "Unexpected error in variable", //
  "UnidentifiedVariableName", "Unidentified variable name", //
  "UnknownValueType", "Unknown value type", //
  "VariableCategoriesDefinitionErrors", "Variable categories definition errors", //
  "VariableNameCannotContainColon", "Variable name cannot contain colon", //
  "VariableNameRequired", "Variable name required", //
  "CsvInitialisationError", "Error occurred initialising csv datasource", //
  "CsvVariablesHeaderMustContainName", "The variables.csv header must contain 'name'", //
  "CsvVariablesHeaderMustContainValueType", "The variables.csv header must contain 'valueType'.", //
  "CsvVariablesHeaderMustContainEntityType", "The variables.csv header must contain 'entityType'.", //
  "CsvCannotCreateWriter", "Cannot create writer", //
  "CsvCannotSetVariableHeader", "Cannot set variables header", //
  "CsvCannotObtainWriter", "Can not get csv writer", //
  "CsvCannotObtainReader", "Can not get csv reader" })
  Map<String, String> datasourceParsingErrorMap();

  @Description("Datasource comparison error map")
  @DefaultStringMapValue( { "IncompatibleValueType", "Incompatible value type", //
  "IncompatibleEntityType", "Incompatible entity type", //
  "CsvVariableMissing", "Variable name exists in csv data file, but no Variable associated with this name exists in the destination table", //
  "VariablePresentInSourceButNotDestination", "Variable exists in source but not in destination" })
  Map<String, String> datasourceComparisonErrorMap();

  @Description("New variables label")
  @DefaultStringValue("New Variables")
  String newVariablesLabel();

  @Description("Modified variables label")
  @DefaultStringValue("Modified Variables")
  String modifiedVariablesLabel();

  @Description("Conflicted variables label")
  @DefaultStringValue("Conflicts")
  String conflictedVariablesLabel();

  @Description("No data available label")
  @DefaultStringValue("No data available")
  String noDataAvailableLabel();

  @Description("Remove label")
  @DefaultStringValue("Remove")
  String removeLabel();

  @Description("View label")
  @DefaultStringValue("View")
  String viewLabel();

  @Description("Create Datasource Completed summary")
  @DefaultStringValue("The datasource was successfully created.")
  String datasourceCreationCompleted();

  @Description("Create Datasource Failed summary")
  @DefaultStringValue("The datasource creation has failed.")
  String datasourceCreationFailed();

  @Description("Item label")
  @DefaultStringValue("Item")
  String itemLabel();

  @Description("Script label")
  @DefaultStringValue("Script")
  String scriptLabel();

  @Description("Line label")
  @DefaultStringValue("Line")
  String lineLabel();

  @Description("Add new category title")
  @DefaultStringValue("Add New Category")
  String addNewCategory();

  @Description("Edit category title")
  @DefaultStringValue("Edit Category")
  String editCategory();

  @Description("Add new attribute title")
  @DefaultStringValue("Add New Attribute")
  String addNewAttribute();

  @Description("Edit attribute title")
  @DefaultStringValue("Edit Attribute")
  String editAttribute();

  @Description("Report produced date")
  @DefaultStringValue("Produced Date")
  String producedDate();

  @Description("Run label")
  @DefaultStringValue("Run")
  String runLabel();

  @Description("Paging of label")
  @DefaultStringValue("to")
  String toLabel();

  @Description("Values label")
  @DefaultStringValue("Values")
  String valuesLabel();

  @Description("Copy of label")
  @DefaultStringValue("Copy_of_")
  String copyOf();

  @Description("Script contains errors and was not saved")
  @DefaultStringValue("The script contains errors and was not saved. Click 'Test' to execute the script and see a detailed report of the errors.")
  String scriptContainsErrorsAndWasNotSaved();

  @Description("Create Datasource Step summary")
  @DefaultStringValue("Select the type of datasource to be created with all the corresponding settings.")
  String createDatasourceStepSummary();

  @Description("Datasource Options label")
  @DefaultStringValue("Datasource type specific options")
  String datasourceOptionsLabel();

  @Description("Create Datasource Process summary")
  @DefaultStringValue("The datasource is in the process of being created.")
  String createDatasourceProcessSummary();

  @Description("Edit View Type Step")
  @DefaultStringValue("Select the datasource to which the view will be attached.")
  String editViewTypeStep();

  @Description("Edit View Tables Step")
  @DefaultStringValue("Select the tables to be included in the view.")
  String editViewTablesStep();

  @Description("Edit View Variables Step")
  @DefaultStringValue("Select the variables that will be viewed.")
  String editViewVariablesStep();

  @Description("Edit View Entities Step")
  @DefaultStringValue("Select the entities that will be viewed.")
  String editViewEntitiesStep();

  @Description("No locale label")
  @DefaultStringValue("no locale")
  String noLocale();

  @Description("Add Unit label")
  @DefaultStringValue("Add Unit")
  String addUnit();

  @Description("Edit Unit label")
  @DefaultStringValue("Edit Unit")
  String editUnit();

}
