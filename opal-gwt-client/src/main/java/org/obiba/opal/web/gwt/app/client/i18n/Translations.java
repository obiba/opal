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

  @Description("Original Label label")
  @DefaultStringValue("Original Label")
  String originalLabelLabel();

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
  @DefaultStringMapValue({ "NOT_STARTED", "Not Started", //
  "IN_PROGRESS", "In Progress", //
  "SUCCEEDED", "Succeeded", //
  "FAILED", "Failed", //
  "CANCEL_PENDING", "Cancel Pending", //
  "CANCELED", "Cancelled" //
  })
  Map<String, String> statusMap();

  @Description("Actions label")
  @DefaultStringValue("Actions")
  String actionsLabel();

  @Description("Action map")
  @DefaultStringMapValue({ "Log", "Log", //
  "Cancel", "Cancel", //
  "Delete", "Delete", //
  "Edit", "Edit", //
  "Copy", "Copy", //
  "Test", "Test", //
  "Download", "Download", //
  "DownloadCertificate", "Download Certificate" })
  Map<String, String> actionMap();

  @Description("Permission map")
  @DefaultStringMapValue({ "Use", "Use", //
  "Administrate", "Administrate", //
  "View", "View", //
  "Summary", "Summary" })
  Map<String, String> permissionMap();

  @Description("Permission explanation map")
  @DefaultStringMapValue({ //
      "datasource", "Specify the access rights to the datasource and its content.", //
      "table", "Specify the access rights to the table and its content. Induces the visibility of the parent datasource.", //
      "variable", "Specify the access rights to the variable. Induces the visibility of the parent table and datasource.", //
      "datashield", "Specify the access rights to the DataShield services.",//
      "r", "Specify the access rights to the R services."//
      })
      Map<String, String> permissionExplanationMap();

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

  @Description("Entity type column label")
  @DefaultStringValue("Entity Type column")
  String entityTypeColumnLabel();

  @Description("Tables label")
  @DefaultStringValue("Tables")
  String tablesLabel();

  @Description("Table label")
  @DefaultStringValue("Table")
  String tableLabel();

  @Description("Variables label")
  @DefaultStringValue("Variables")
  String variablesLabel();

  @Description("Entities Count label")
  @DefaultStringValue("#Entities")
  String entitiesCountLabel();

  @Description("Variable label")
  @DefaultStringValue("Variable")
  String variableLabel();

  @Description("Unit label")
  @DefaultStringValue("Unit")
  String unitLabel();

  @Description("User message map")
  @DefaultStringMapValue({ //
      "VariableNameNotUnique", "The specified variable name already exists.", //
      "CategoryDialogNameRequired", "A category name is required.", //
      "CategoryNameAlreadyExists", "The specified category name already exists.", //
      "AttributeNameRequired", "An attribute name is required.", //
      "AttributeNameAlreadyExists", "The specified attribute name already exists.", //
      "AttributeValueRequired", "Provide a value for the attribute (either localised, or not localised).", //
      "CategoryLabelRequired", "Provide a label for the this category.", //
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
      "NoDataToCopy", "No data to copy to the current destination.",//
      "NoFileSelected", "You must select a file.",//
      "CharsetNotAvailable", "The character set you have specified is not available.",//
      "FieldSeparatorRequired", "The field separator is required.",//
      "QuoteSeparatorRequired", "The quote separator is required.",//
      "NotIgnoredConlicts", "Some conflicts were detected. Ignore modifications before applying changes.",//
      "NoVariablesToBeImported", "No variables are to be imported.",//
      "FunctionalUnitAlreadyExistWithTheSpecifiedName", "A unit with the same name already exists.",//
      "FunctionalUnitNameIsRequired", "Unit name is required.",//
      "DuplicateFunctionalUnitNames", "Duplicate Unit names.",//
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
      "KeyPairPublicKeyPEMIsRequired", "Public Key in PEM format is required.",//
      "DestinationFileIsMissing", "Destination File is required.", //
      "ExportDataMissingTables", "At least one table is required.",//
      "IdentifiersGenerationCompleted", "Identifiers generation completed.",//
      "NoIdentifiersGenerated", "No Identifiers generated.",//
      "IdentifiersGenerationFailed", "Identifiers generation has failed.",//
      "IdentifiersGenerationPending", "An Identifiers generation task is currently running.",//
      "MappedUnitsCannotBeIdentified", "Units to be mapped cannot be identified.",//
      "TwoMappedUnitsExpected", "Exactly two Units to be mapped are expected.",//
      "DataShieldMethodAlreadyExistWithTheSpecifiedName", "A method already exists with the specified name.",//
      "DataShieldMethodNameIsRequired", "A name is required.",//
      "DataShieldRScriptIsRequired", "A R script is required.",//
      "DataShieldRFunctionIsRequired", "A R function is required.",//
      "RIsAlive", "R server is alive.",//
      "RConnectionFailed", "Connection with R server failed.", //
      "UnauthorizedOperation", "You are not allowed to perform this operation.",//
      "CannotWriteToView", "Cannot modify a View using this operation. Use the View editor.",//
      "DatesRangeInvalid", "The range of dates is invalid.",//
      "CouldNotCreateReportTemplate", "Could not create the Report Template.",//
      "ReportTemplateCannotBeFound", "The Report Template cannot be found.",//
      "DatasourceCreationFailed", "The datasource creation has failed: {0}",//
      "DestinationTableRequired", "The destination table is required.",//
      "DestinationTableNameInvalid", "The destination table name is not valid (must not contain '.' or ':').",//
      "DestinationTableEntityTypeRequired", "The destination table entity type is required.",//
      "DestinationTableCannotBeView", "The destination table cannot be a view.",//
      "DataImportationProcessLaunched", "The data importation process can be followed using the Job ID: {0}",//
      "DataExportationProcessLaunched", "The data exportation process can be followed using the Job ID: {0}",//
      "DatabaseAlreadyExists", "A database with this name already exists.",//
      "DatabaseConnectionOk", "Connection successful.",//
      "DatabaseConnectionFailed", "Failed to connect: {0}.",//
      "NameIsRequired", "A name is required.",//
      "DriverIsRequired", "A driver is required.",//
      "UrlIsRequired", "A url is required.",//
      "UsernameIsRequired", "A username is required."//
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

  @Description("Original Value label")
  @DefaultStringValue("Original Value")
  String originalValueLabel();

  @Description("New Value label")
  @DefaultStringValue("New Value")
  String newValueLabel();

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
  @DefaultStringMapValue({ //
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
      "generateFunctionalUnitIdentifiers", "Generate Unit Identifiers",//
      "deleteCategory", "Delete Category",//
      "deleteAttribute", "Delete Attribute",//
      "overwriteVariable", "Overwrite Variable",//
      "createView", "Create View",//
      "removeView", "Remove View",//
      "removeTable", "Remove Table",//
      "deleteDataShieldMethod", "Delete Aggregating Method",//
      "deleteDatabase", "Delete Database" })
      Map<String, String> confirmationTitleMap();

  @Description("Confirmation message map")
  @DefaultStringMapValue({ //
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
      "confirmOverwriteVariable", "A variable with the same name already exists. Please confirm that you want to overwrite this variable.",//
      "confirmCreateView", "Please confirm that you want to create a new view.",//
      "confirmRemoveView", "Please confirm that you want to remove the current view.",//
      "confirmRemoveTable", "Please confirm that you want to remove the current table. This cannot be undone and all data associated with this table will be lost.",//
      "confirmGenerateFunctionalUnitIdentifiers", "Please confirm that you want to generate an identifier for each existing participant without identifier in the current unit.",//
      "confirmDeleteDataShieldMethod", "Please confirm that you want to remove this aggregating method.",//
      "confirmDeleteDatabase", "Please confirm that you want to remove this database." })
      Map<String, String> confirmationMessageMap();

  @Description("A name is required when creating a new folder")
  @DefaultStringValue("You must specify a folder name")
  String folderNameIsRequired();

  @Description("Dot names are not permitted")
  @DefaultStringValue("The names '.' and '..' are not permitted.")
  String dotNamesAreInvalid();

  @Description("Data copy instructions")
  @DefaultStringValue("Select the tables to be copied.")
  String dataCopyInstructions();

  @Description("Data copy pending conclusion")
  @DefaultStringValue("Data copy job is being launched.")
  String dataCopyPendingConclusion();

  @Description("Data copy completed conclusion")
  @DefaultStringValue("Data copy job was successfully launched.")
  String dataCopyCompletedConclusion();

  @Description("Data copy failed conclusion")
  @DefaultStringValue("Data copy job launch failed.")
  String dataCopyFailedConclusion();

  @Description("Data copy destination")
  @DefaultStringValue("Select the destination of the copy.")
  String dataCopyDestination();

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

  @Description("Data import Compared Datasources Report instructions")
  @DefaultStringValue("Review the data dictionary that will be imported.")
  String dataImportComparedDatasourcesReportStep();

  @Description("Data import Values instructions")
  @DefaultStringValue("Review the data that will be imported.")
  String dataImportValuesStep();

  @Description("Data import instructions conclusion")
  @DefaultStringValue("Data import job is launched.")
  String dataImportInstructionsConclusion();

  @Description("Identifiers Map File Step")
  @DefaultStringValue("Select the file of identifiers to be mapped.")
  String identifiersMapFileStep();

  @Description("Identifiers Map Unit Step")
  @DefaultStringValue("Select which unit is to be used for retrieving the participants to be mapped.")
  String identifiersMapUnitStep();

  @Description("Identifier map pending conclusion")
  @DefaultStringValue("Identifier mapping job is being launched.")
  String identifierMapPendingConclusion();

  @Description("Identifier map completed conclusion")
  @DefaultStringValue("Identifier mapping job completed successfully.")
  String identifierMapCompletedConclusion();

  @Description("Identifier map update count")
  @DefaultStringValue("Number of Participants updated")
  String identifierMapUpdateCount();

  @Description("Identifier map failed conclusion")
  @DefaultStringValue("Identifier mapping job failed.")
  String identifierMapFailedConclusion();

  @Description("Identifiers Import File Step")
  @DefaultStringValue("Select the file of identifiers to be imported.")
  String identifiersImportFileStep();

  @Description("Identifier import pending conclusion")
  @DefaultStringValue("Identifier import job is being launched.")
  String identifierImportPendingConclusion();

  @Description("Identifier import completed conclusion")
  @DefaultStringValue("Identifier import job completed successfully.")
  String identifierImportCompletedConclusion();

  @Description("Identifier import failed conclusion")
  @DefaultStringValue("Identifier import job failed.")
  String identifierImportFailedConclusion();

  @Description("Export to Excel icon title")
  @DefaultStringValue("Export to Excel file")
  String exportToExcelTitle();

  @Description("Download View XML menu item")
  @DefaultStringValue("Download View XML")
  String downloadViewXML();

  @Description("Csv label")
  @DefaultStringValue("CSV")
  String csvLabel();

  @Description("Excel label")
  @DefaultStringValue("Excel")
  String excelLabel();

  @Description("Opal XML label")
  @DefaultStringValue("Opal XML")
  String opalXmlLabel();

  @Description("Select file and data format label")
  @DefaultStringValue("Select a file and data format")
  String selectFileAndDataFormatLabel();

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
  @DefaultStringMapValue({ "CategoryNameRequired", "Category name required", //
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
  @DefaultStringMapValue({ "IncompatibleValueType", "Incompatible value type", //
  "IncompatibleEntityType", "Incompatible entity type", //
  "CsvVariableMissing", "Variable name exists in csv data file, but no Variable associated with this name exists in the destination table", //
  "VariablePresentInSourceButNotDestination", "Variable exists in source but not in destination" })
      Map<String, String> datasourceComparisonErrorMap();

  @Description("New variables label")
  @DefaultStringValue("New Variables")
  String newVariablesLabel();

  @Description("Unmodified variables label")
  @DefaultStringValue("Unmodified Variables")
  String unmodifiedVariablesLabel();

  @Description("Modified variables label")
  @DefaultStringValue("Modified Variables")
  String modifiedVariablesLabel();

  @Description("Conflicted variables label")
  @DefaultStringValue("Conflicts")
  String conflictedVariablesLabel();

  @Description("No data available label")
  @DefaultStringValue("No data available")
  String noDataAvailableLabel();

  @Description("No summary data available label")
  @DefaultStringValue("No summary data available")
  String noSummaryDataAvailableLabel();

  @Description("Remove label")
  @DefaultStringValue("Remove")
  String removeLabel();

  @Description("View label")
  @DefaultStringValue("View")
  String viewLabel();

  @Description("Add View label")
  @DefaultStringValue("Add View")
  String addViewLabel();

  @Description("Add Update Tables label")
  @DefaultStringValue("Add/Update Tables")
  String addUpdateTablesLabel();

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

  @Description("Script Evaluation label")
  @DefaultStringValue("Script Evaluation")
  String scriptEvaluationLabel();

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

  @Description("Download Report Design label")
  @DefaultStringValue("Download Report Design")
  String downloadReportDesignLabel();

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
  @DefaultStringValue("Select the type of datasource to be created.")
  String createDatasourceStepSummary();

  @Description("Datasource Options label")
  @DefaultStringValue("Provide datasource type specific options.")
  String datasourceOptionsLabel();

  @Description("Create Datasource Process summary")
  @DefaultStringValue("The datasource is in the process of being created.")
  String createDatasourceProcessSummary();

  @Description("Edit View Type Step")
  @DefaultStringValue("Define the type of view to be added.")
  String editViewTypeStep();

  @Description("Edit View Tables Step")
  @DefaultStringValue("Select the tables to be included in the view.")
  String editViewTablesStep();

  @Description("No locale label")
  @DefaultStringValue("no locale")
  String noLocale();

  @Description("Add Unit label")
  @DefaultStringValue("Add Unit")
  String addUnit();

  @Description("Edit Unit label")
  @DefaultStringValue("Edit Unit")
  String editUnit();

  @Description("Download Identifiers label")
  @DefaultStringValue("Export Identifiers")
  String downloadUnitIdentifiers();

  @Description("Export Identifiers to Excel label")
  @DefaultStringValue("Export Identifiers Mapping")
  String exportUnitIdentifiersToExcel();

  @Description("Add Cryptographic Key label")
  @DefaultStringValue("Add Cryptographic Key")
  String addAddCryptographicKey();

  @Description("Add Cryptographic Key label")
  @DefaultStringValue("Add Cryptographic Key")
  String addCryptoKey();

  @Description("Generate Identifiers label")
  @DefaultStringValue("Generate Identifiers")
  String generateUnitIdentifiers();

  @Description("Import Unit Identifiers From Data label")
  @DefaultStringValue("Import Identifiers from Data File")
  String importUnitIdentifiersFromData();

  @Description("Import Mapped Unit Identifiers label")
  @DefaultStringValue("Add/Update Identifiers Mapping")
  String importMappedUnitIdentifiers();

  @Description("Alias label")
  @DefaultStringValue("Name")
  String aliasLabel();

  @Description("Select Key Type Step label")
  @DefaultStringValue("Provide a name and a type for the cyrptographic key to add to this Unit.")
  String keyTypeStep();

  @Description("Import Certificate label")
  @DefaultStringValue("Provide the Public Certificate by pasting it here (PEM format).")
  String importCertificateStep();

  @Description("Private Key Step label")
  @DefaultStringValue("Select how to add the private key of the key pair (create a new one or import an existing one)")
  String privateKeyStep();

  @Description("Public Key Step label")
  @DefaultStringValue("Provide the Public Certificate definition.")
  String publicKeyStep();

  @Description("Cancel label")
  @DefaultStringValue("Cancel")
  String cancelLabel();

  @Description("Close label")
  @DefaultStringValue("Close")
  String closeLabel();

  @Description("Finish label")
  @DefaultStringValue("Finish")
  String finishLabel();

  @Description("Next label")
  @DefaultStringValue("Next >")
  String nextLabel();

  @Description("Previous label")
  @DefaultStringValue("< Previous")
  String previousLabel();

  @Description("Help label")
  @DefaultStringValue("Help")
  String helpLabel();

  @Description("KeyType map")
  @DefaultStringMapValue({ "KEY_PAIR", "Key Pair", "CERTIFICATE", "Certificate" })
  Map<String, String> keyTypeMap();

  @Description("Paste Private Key PEM label")
  @DefaultStringValue("(paste private key in PEM format)")
  String pastePrivateKeyPEM();

  @Description("Paste Public Key PEM label")
  @DefaultStringValue("(paste public certificate in PEM format)")
  String pastePublicKeyPEM();

  @Description("Data export title")
  @DefaultStringValue("Export Data")
  String exportData();

  @Description("Data copy title")
  @DefaultStringValue("Copy Data")
  String copyData();

  @Description("Variables Import File Selection Step")
  @DefaultStringValue("Select the variables file for batch edition of tables and variables.")
  String variablesImportFileSelectionStep();

  @Description("Variables Import Compare Step")
  @DefaultStringValue("Review the modifications before applying them.")
  String variablesImportCompareStep();

  @Description("Variables Import Pending")
  @DefaultStringValue("Importing Variables...")
  String variablesImportPending();

  @Description("Add View Pending")
  @DefaultStringValue("View is being created ...")
  String addViewPending();

  @Description("Add View Success")
  @DefaultStringValue("View successfully created.")
  String addViewSuccess();

  @Description("Add View Failed")
  @DefaultStringValue("View creation failed.")
  String addViewFailed();

  @Description("No format options step")
  @DefaultStringValue("No format options are available for the selected file format")
  String noFormatOptionsStep();

  @Description("CSV format options step")
  @DefaultStringValue("CSV format options step")
  String csvFormatOptionsStep();

  @Description("Datasource Type map")
  @DefaultStringMapValue({ "hibernate", "Opal SQL", //
  "jdbc", "Custom SQL", //
  "fs", "XML", //
  "csv", "CSV", //
  "excel", "Excel" })
  Map<String, String> datasourceTypeMap();

  @Description("Data Import Format Step")
  @DefaultStringValue("Select the format of data you wish to import.")
  String dataImportFormatStep();

  @Description("Data Import File Step")
  @DefaultStringValue("Select the file to be imported.")
  String dataImportFileStep();

  @Description("Data Import Unit Step")
  @DefaultStringValue("Specify how the participant are identified.")
  String dataImportUnitStep();

  @Description("Data Import Archive Step")
  @DefaultStringValue("Specify whether the data file is to be archived.")
  String dataImportArchiveStep();

  @Description("Data Import Destination Step")
  @DefaultStringValue("Select the destination of the import.")
  String dataImportDestinationStep();

  @Description("Data Import Pending Validation")
  @DefaultStringValue("Data to import are being validated...")
  String dataImportPendingValidation();

  @Description("Data Import Completed Validation")
  @DefaultStringValue("Data import validation completed.")
  String dataImportCompletedValidation();

  @Description("Data Import Failed Validation")
  @DefaultStringValue("Data import validation failed.")
  String dataImportFailedValidation();

  @Description("Import Unit Identifiers Instructions")
  @DefaultStringValue("First column MUST be identifiers from unit")
  String importUnitIdentifiersInstructions();

  @Description("Import Opal Identifiers Instructions")
  @DefaultStringValue("First column MUST be Opal identifiers.")
  String importOpalIdentifiersInstructions();

  @Description("Add DataShield method label")
  @DefaultStringValue("Add Method")
  String addDataShieldMethod();

  @Description("Edit DataShield method label")
  @DefaultStringValue("Edit Method")
  String editDataShieldMethod();

  @Description("R Script label")
  @DefaultStringValue("R Script")
  String rScriptLabel();

  @Description("R Function label")
  @DefaultStringValue("R Function")
  String rFunctionLabel();

  @Description("Who label")
  @DefaultStringValue("Who")
  String whoLabel();

  @Description("Subject Type map")
  @DefaultStringMapValue({ "USER", "User Name", //
  "GROUP", "Group Name" })
  Map<String, String> subjectTypeMap();

  @Description("Derive label")
  @DefaultStringValue("Derive")
  String deriveLabel();

  @Description("Derive Categorize label")
  @DefaultStringValue("Categorize")
  String deriveCategorizeLabel();

  @Description("Derive Custom label")
  @DefaultStringValue("Custom")
  String deriveCustomLabel();

  @Description("Invalid Destination View label")
  @DefaultStringValue("Not a valid destination view.")
  String invalidDestinationView();

  @Description("Derived Variable Name Required label")
  @DefaultStringValue("Derived variable name is required.")
  String derivedVariableNameRequired();

  @Description("Destination View Name Required label")
  @DefaultStringValue("Destination view name is required.")
  String destinationViewNameRequired();

  @Description("Add Derived Variable To View Only label")
  @DefaultStringValue("A derived variable can only be added to a view.")
  String addDerivedVariableToViewOnly();

  @Description("Derived Variable Evaluation label")
  @DefaultStringValue("Review the summary and the values of the derived variable.")
  String derivedVariableEvaluation();

  @Description("Save Derived Variable label")
  @DefaultStringValue("Name the derived variable and select the view in which it will appear.")
  String saveDerivedVariable();

  @Description("Recode Categorie Step title")
  @DefaultStringValue("Recode categories and observed distinct values to new values.")
  String recodeCategoriesStepTitle();

  @Description("Recode Boolean Step title")
  @DefaultStringValue("Recode logical values to new values.")
  String recodeBooleanStepTitle();

  @Description("Empty Value label")
  @DefaultStringValue("Empty value")
  String emptyValuesLabel();

  @Description("Other Value label")
  @DefaultStringValue("Other value")
  String otherValuesLabel();

  @Description("True label")
  @DefaultStringValue("True")
  String trueLabel();

  @Description("False label")
  @DefaultStringValue("False")
  String falseLabel();

  @Description("Recode Temporal Method Step title")
  @DefaultStringValue("Dates and times can be grouped together using the following methods:")
  String recodeTemporalMethodStepTitle();

  @Description("Recode Temporal Map Step title")
  @DefaultStringValue("Map each time range to a new value.")
  String recodeTemporalMapStepTitle();

  @Description("Recode Numerical Method Step title")
  @DefaultStringValue("Numerical values can be grouped together using the following methods:")
  String recodeNumericalMethodStepTitle();

  @Description("Recode Numerical Map Step title")
  @DefaultStringValue("Map each range or discrete value to a new value.")
  String recodeNumericalMapStepTitle();

  @Description("Recode Open Textual Method Step Title")
  @DefaultStringValue("Group the values using the following method.")
  String recodeOpenTextualMethodStepTitle();

  @Description("Recode Open Textual Map Step Title")
  @DefaultStringValue("Map original values to new values.")
  String recodeOpenTextualMapStepTitle();

  @Description("Recode Custom Step Title")
  @DefaultStringValue("Specify the custom derivation script.")
  String recodeCustomDeriveStepTitle();

  @Description("Time map")
  @DefaultStringMapValue({ //
      "Hour", "Hour", //
      "Monday", "Monday", //
      "Tuesday", "Tuesday", //
      "Wednesday", "Wednesday", //
      "Thursday", "Thursday", //
      "Friday", "Friday", //
      "Saturday", "Saturday", //
      "Sunday", "Sunday", //
      "January", "January", //
      "February", "February", //
      "March", "March", //
      "April", "April", //
      "May", "May", //
      "June", "June", //
      "July", "July", //
      "August", "August", //
      "September", "September", //
      "October", "October", //
      "November", "November", //
      "December", "December", //
      "Second", "Second", //
      "Minute", "Minute", //
      "Hour", "Hour", //
      "Day", "Day", //
      "Week", "Week", //
      "Month", "Month", //
      "Quarter", "Quarter", //
      "Semester", "Semester", //
      "Year", "Year", //
      "Lustrum", "Lustrum", //
      "Decade", "Decade", //
      "Century", "Century", //
      "Millenium", "Millenium", //
      "Era", "Era" //
      })
      Map<String, String> timeMap();

  @Description("Time Group map")
  @DefaultStringMapValue({ //
      "HOUR_OF_DAY", "Hour of Day", //
      "DAY_OF_WEEK", "Day of Week", //
      "DAY_OF_MONTH", "Day of Month", //
      "DAY_OF_YEAR", "Day of Year", //
      "WEEK_OF_MONTH", "Week of Month", //
      "WEEK_OF_YEAR", "Week of Year", //
      "MONTH", "Month", //
      "MONTH_OF_YEAR", "Month of Year", //
      "QUARTER_OF_YEAR", "Quarter of Year", //
      "QUARTER", "Quarter", //
      "SEMESTER_OF_YEAR", "Semester of Year", //
      "SEMESTER", "Semester", //
      "YEAR", "Year", //
      "LUSTRUM", "Lustrum (5 years period)", //
      "DECADE", "Decade (10 years period)", //
      "CENTURY", "Century" //
      })
      Map<String, String> timeGroupMap();

  @Description("DataSHIELD Labels")
  @DefaultStringMapValue({ //
      "Aggregate", "Aggregate",//
      "Assign", "Assign" //
      })
      Map<String, String> dataShieldLabelsMap();

  @Description("Lower Value Limit Required label")
  @DefaultStringValue("Lower value limit is required.")
  String lowerValueLimitRequired();

  @Description("Upper Value Limit Required label")
  @DefaultStringValue("Upper value limit is required.")
  String upperValueLimitRequired();

  @Description("Lower Limit Greater Than Upper Limit label")
  @DefaultStringValue("Lower value limit cannot be greater than upper value limit.")
  String lowerLimitGreaterThanUpperLimit();

  @Description("Ranges Length Required label")
  @DefaultStringValue("Ranges Length is required.")
  String rangesLengthRequired();

  @Description("Ranges Count Required label")
  @DefaultStringValue("Number of ranges is required.")
  String rangesCountRequired();

  @Description("Value is not a category label")
  @DefaultStringValue("This value does not correspond to any category")
  String valueIsNotACategory();

  @Description("Ranges Overlap label")
  @DefaultStringValue("Range is overlapping another range.")
  String rangeOverlap();

  @Description("Value Map Already Added label")
  @DefaultStringValue("This value is already mapped.")
  String valueMapAlreadyAdded();

  @Description("Frequency label")
  @DefaultStringValue("Frequency")
  String frequency();

  @Description("Create a Coding View")
  @DefaultStringValue("Create a Coding View")
  String createCodingView();

  @Description("Script Evaluation Failed Label")
  @DefaultStringValue("Script evaluation failed: check if value type is correct.")
  String scriptEvaluationFailed();

  @Description("Property label")
  @DefaultStringValue("Property")
  String property();

  @Description("Value label")
  @DefaultStringValue("Value")
  String value();

  @Description("Participant label")
  @DefaultStringValue("Participant")
  String participant();

  @Description("Download label")
  @DefaultStringValue("Download")
  String downloadLabel();

  @Description("Show label")
  @DefaultStringValue("Show")
  String showLabel();

  @Description("Hide label")
  @DefaultStringValue("Hide")
  String hideLabel();

  @Description("Add Database")
  @DefaultStringValue("Add Database")
  String addDatabase();

  @Description("Edit Database")
  @DefaultStringValue("Edit Database")
  String editDatabase();

  @Description("Username label")
  @DefaultStringValue("Username")
  String usernameLabel();

  @Description("Driver label")
  @DefaultStringValue("Driver")
  String driverLabel();

  @Description("Url label")
  @DefaultStringValue("URL")
  String urlLabel();

  @Description("Key/Value label")
  @DefaultStringValue("key=value")
  String keyValueLabel();

}
