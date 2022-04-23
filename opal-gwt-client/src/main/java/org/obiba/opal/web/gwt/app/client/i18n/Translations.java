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

import java.util.Map;

import com.google.gwt.i18n.client.Constants;
import com.google.gwt.i18n.client.LocalizableResource.Generate;
import com.google.gwt.i18n.client.LocalizableResource.GenerateKeys;

/**
 * Programmatically available localised text strings. This interface will be bound to localised properties files found
 * in the {@code com.google.gwt.i18n.client} package.
 */
@SuppressWarnings("OverlyComplexClass")
@GenerateKeys
@Generate(format = "com.google.gwt.i18n.rebind.format.PropertiesFormat", locales = { "default" })
public interface Translations extends Constants {

  @Description("2FA Enable label")
  @DefaultStringValue("Enable 2FA")
  String otpEnable();

  @Description("2FA Disable label")
  @DefaultStringValue("Disable 2FA")
  String otpDisable();
  @Description("Report template create dialog title")
  @DefaultStringValue("Add Report Template")
  String addReportTemplateDialogTitle();

  @Description("Report template edit dialog title")
  @DefaultStringValue("Edit Report Template")
  String editReportTemplateDialogTitle();

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

  @Description("ID Column label")
  @DefaultStringValue("ID Column")
  String idColumnLabel();

  @Description("All Columns label")
  @DefaultStringValue("All Columns")
  String allColumnsLabel();

  @Description("Context label")
  @DefaultStringValue("Context")
  String contextLabel();

  @Description("Profile label")
  @DefaultStringValue("Profile")
  String profileLabel();

  @Description("Cluster label")
  @DefaultStringValue("Cluster")
  String clusterLabel();

  @Description("Tags label")
  @DefaultStringValue("Tags")
  String tagsLabel();

  @Description("R Server label")
  @DefaultStringValue("R Server")
  String rServerLabel();

  @Description("R Server Profile label")
  @DefaultStringValue("R Server Profile")
  String rServerProfileLabel();

  @Description("Type label")
  @DefaultStringValue("Type")
  String typeLabel();

  @Description("Permission label")
  @DefaultStringValue("Permission")
  String permissionLabel();

  @Description("Resource label")
  @DefaultStringValue("Resource")
  String resourceLabel();

  @Description("User label")
  @DefaultStringValue("User")
  String userLabel();

  @Description("Project label")
  @DefaultStringValue("Project")
  String projectLabel();

  @Description("Scope label")
  @DefaultStringValue("Scope")
  String scopeLabel();

  @Description("Start label")
  @DefaultStringValue("Start")
  String startLabel();

  @Description("Stop label")
  @DefaultStringValue("Stop")
  String stopLabel();

  @Description("Resume label")
  @DefaultStringValue("Resume")
  String resumeLabel();

  @Description("Suspend label")
  @DefaultStringValue("Suspend")
  String suspendLabel();

  @Description("End label")
  @DefaultStringValue("End")
  String endLabel();

  @Description("Status label")
  @DefaultStringValue("Status")
  String statusLabel();

  @Description("Status map")
  @DefaultStringMapValue({ "NOT_STARTED", "Not Started",
      "IN_PROGRESS", "In Progress",
      "SUCCEEDED", "Succeeded",
      "FAILED", "Failed",
      "CANCEL_PENDING", "Cancel Pending",
      "CANCELED", "Cancelled",
      "WAITING", "Waiting",
      "BUSY", "Busy",
      "RUNNING", "Running",
      "STOPPED", "Stopped"
  })
  Map<String, String> statusMap();

  @Description("Actions label")
  @DefaultStringValue("Actions")
  String actionsLabel();

  @Description("Action map")
  @DefaultStringMapValue({ "Log", "Log",
      "Cancel", "Cancel",
      "Remove Index", "Remove Index",
      "Edit", "Edit",
      "Copy", "Copy",
      "Test", "Test",
      "Certificate", "Certificate",
      "Download", "Download",
      "DownloadCertificate", "Download Certificate",
      "Index now", "Index now",
      "Install", "Install",
      "Reinstate", "Reinstate",
      "Clear", "Clear",
      "View", "View",
      "Remove", "Remove",
      "Run", "Run",
      "View", "View",
      "Duplicate", "Duplicate",
      "Delete", "Delete",
      "Publish", "Publish",
      "UnPublish", "Unpublish",
      "Permissions", "Permissions",
      "CommitDiff", "Diff",
      "DiffWithCurrent", "Diff with current",
      "Restore", "Restore",
      "Enable", "Enable",
      "Disable", "Disable",
      "Generate identifiers", "Generate identifiers",
      "MoveUp", "Move up",
      "MoveDown", "Move down",
      "Search", "Search",
      "Download identifiers", "Download identifiers",
      "Unregister", "Unregister",
      "Terminate", "Terminate",
      "Statistics", "Statistics",
      "Restart", "Restart",
      "Configure", "Configure"
  })
  Map<String, String> actionMap();

  @Description("Permission map")
  @DefaultStringMapValue({
      "SYSTEM_ALL", "Administrate",
      "PROJECT_ADD", "Add project",

      "PROJECT_ALL", "Administrate",

      "VCF_STORE_VIEW", "View VCF files statistics",
      "VCF_STORE_VALUES", "View VCF files data and statistics",
      "VCF_STORE_ALL", "Administrate VCF Store",

      "DATASOURCE_ALL", "Administrate",
      "TABLE_ADD", "Add table",
      "DATASOURCE_VIEW", "View dictionary and values of all tables",

      "TABLE_ALL", "Administrate",
      "TABLE_READ", "View dictionary and summary",
      "TABLE_VALUES", "View dictionary and values",
      "TABLE_EDIT", "Edit dictionary and view summary",
      "TABLE_VALUES_EDIT", "Edit dictionary and view values",

      "VARIABLE_READ", "View dictionary and summary",

      "RESOURCES_ALL", "Administrate",
      "RESOURCES_VIEW", "View any resource (no credentials)",

      "RESOURCE_ALL", "Administrate",
      "RESOURCE_VIEW", "View resource (no credentials)",

      "DATABASES_ALL", "Administrate",
      "R_USE", "Use",
      "DATASHIELD_ALL", "Administrate",
      "DATASHIELD_USE", "Use",
      "DATASHIELD_PROFILE_USE", "Use",
      "REPORT_TEMPLATE_ALL", "Administrate",
      "REPORT_TEMPLATE_READ", "View reports" })
  Map<String, String> permissionMap();

  @Description("Permission Node Name format map")
  @Constants.DefaultStringMapValue({
      "PROJECT", "Project",
      "DATASOURCE", "Tables",
      "TABLE", "Table",
      "VARIABLE", "Variable",
      "REPORT_TEMPLATE", "Report",
      "VCF_STORE", "VCF Store",
      "RESOURCE", "Resource",
      "RESOURCES", "Resources"
  })
  Map<String, String> permissionResourceNodeTypeMap();

  @Description("Update Resource Permission User label")
  @DefaultStringValue("Permission of user: ")
  String userResourcePermissionLabel();

  @Description("Permission explanation map")
  @DefaultStringMapValue({
      "SYSTEM_ALL.help", "Full access to the system.",

      "PROJECT_ADD.help", "Add new projects and therefore can import/export data in the context of the project.",
      "PROJECT_ALL.help", "Full access to project settings and data.",

      "VCF_STORE_VIEW.help", "View VCF files and statistics.",
      "VCF_STORE_VALUES.help", "Export VCF files and view statistics.",
      "VCF_STORE_ALL.help", "Import/Export VCF files, view statistics and set samples-participants mapping table.",

      "DATASOURCE_ALL.help", "Full access to project tables, including individual values.",
      "TABLE_ADD.help", "Add tables or views.",
      "DATASOURCE_VIEW.help", "Read access to project tables, including individual values.",

      "TABLE_ALL.help", "Full access to the table, including edition of the dictionary and individual values.",
      "TABLE_READ.help", "View dictionary and summaries (no access to individual values).",
      "TABLE_VALUES.help", "View dictionary with access to individual values.",
      "TABLE_EDIT.help", "Edit dictionary and view values summary (no access to individual values).",
      "TABLE_VALUES_EDIT.help", "Edit dictionary and view individual values.",

      "VARIABLE_READ.help", "View variable description and values summary (no access to individual values).",

      "RESOURCES_ALL.help", "Full access to project resources.",
      "RESOURCES_VIEW.help", "View any resource without having access to the associated credentials (DataSHIELD compliant permission).",

      "RESOURCE_ALL.help", "Full access to the resource.",
      "RESOURCE_VIEW.help", "View resource without having access to the associated credentials (DataSHIELD compliant permission).",

      "DATABASES_ALL.help", "Administrate databases.",
      "R_USE.help", "Use R services: can push accessible data to R and can execute ANY R commands on them.",
      "DATASHIELD_ALL.help", "Administrate DataSHIELD settings.",
      "DATASHIELD_USE.help",
      "Use DataSHIELD services: can push accessible data to R and can execute limited R commands on them.",
      "DATASHIELD_PROFILE_USE.help",
      "Use DataSHIELD services with this profile: can push accessible data to R and can execute limited R commands on them.",
      "REPORT_TEMPLATE_ALL.help", "Full access to the report template (settings and reports).",
      "REPORT_TEMPLATE_READ.help", "View reports only." })
  Map<String, String> permissionExplanationMap();

  @Description("Table Comparison Result map")
  @DefaultStringMapValue({ "CREATION", "Table to be created",
      "MODIFICATION", "Table to be modified",
      "CONFLICT", "Conflicting table modifications",
      "SAME", "No table modifications",
      "FORBIDDEN", "Table modifications not permitted"
  })
  Map<String, String> comparisonResultMap();

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

  @Description("Task label")
  @DefaultStringValue("Task")
  String taskLabel();

  @Description("Entity type label")
  @DefaultStringValue("Entity Type")
  String entityTypeLabel();

  @Description("Referenced Entity type label")
  @DefaultStringValue("Referenced Entity Type")
  String referencedEntityTypeLabel();

  @Description("Entity type column label")
  @DefaultStringValue("Entity Type column")
  String entityTypeColumnLabel();

  @Description("Tables label")
  @DefaultStringValue("Tables")
  String tablesLabel();

  @Description("Datasource label")
  @DefaultStringValue("Datasource")
  String datasourceLabel();

  @Description("Table label")
  @DefaultStringValue("Table")
  String tableLabel();

  @Description("Entities label")
  @DefaultStringValue("Entities")
  String entitiesLabel();

  @Description("Variables label")
  @DefaultStringValue("Variables")
  String variablesLabel();

  @Description("Variable label")
  @DefaultStringValue("Variable")
  String variableLabel();

  @Description("Query label")
  @DefaultStringValue("Query")
  String queryLabel();

  @Description("Time label")
  @DefaultStringValue("Time")
  String timeLabel();

  @Description("Count label")
  @DefaultStringValue("Count")
  String countLabel();

  @Description("All label")
  @DefaultStringValue("All")
  String allLabel();

  @Description("Unit label")
  @DefaultStringValue("Unit")
  String unitLabel();

  @Description("Table name already exists label")
  @DefaultStringValue("A table already exists with this name.")
  String tableNameAlreadyExists();

  @Description("Variable name already exists label")
  @DefaultStringValue("A variable already exists with this name.")
  String variableNameAlreadyExists();

  @Description("Identifiers mapping name already exists label")
  @DefaultStringValue("An identifiers mapping already exists with this name.")
  String identifiersMappingNameAlreadyExists();

  @Description("Identifiers mappings Empty label")
  @DefaultStringValue("No identifiers mappings.")
  String noIdentifiersMappings();

  @Description("Identifiers mappingsMappings Column label")
  @DefaultStringValue("Identifiers Mapping")
  String identifiersMappings();

  @Description("Category name already exists label")
  @DefaultStringValue("The specified category name already exists.")
  String categoryNameAlreadyExists();

  @Description("Provide a name for this category label")
  @DefaultStringValue("Provide a name for this category.")
  String categoryNameRequired();

  @Description("You must select a file message")
  @DefaultStringValue("You must select a file.")
  String fileMustBeSelected();

  @Description("Yes label")
  @DefaultStringValue("Yes")
  String yesLabel();

  @Description("No label")
  @DefaultStringValue("No")
  String noLabel();

  @Description("No Label label")
  @DefaultStringValue("No label.")
  String noLabelInfo();

  @Description("No Description label")
  @DefaultStringValue("No description.")
  String noDescriptionInfo();

  @Description("User message map")
  @DefaultStringMapValue({
      "VariableNameNotUnique", "The specified variable name already exists.",
      "jobCancelled", "Job cancelled.",
      "jobDeleted", "Job removed.",
      "rSessionTerminated", "R session terminated.",
      "rWorkspaceRemoved", "R workspace removed.",
      "completedJobsDeleted", "All completed jobs removed.",
      "SetCommandStatus_NotFound", "Job could not be cancelled (not found).",
      "SetCommandStatus_BadRequest_IllegalStatus", "Job status cannot be set to the specified value.",
      "SetCommandStatus_BadRequest_NotCancellable", "Job has completed and has already been cancelled.",
      "DeleteCommand_NotFound", "Job could not be removed (not found).",
      "DeleteCommand_BadRequest_NotDeletable", "Job is currently running and therefore cannot be removed at this time.",
      //
      "cannotCreateFolderPathAlreadyExist",
      "Could not create the folder, a folder or a file exist with that name at the specified path.",
      "cannotCreateFolderParentIsReadOnly", "Could create the following folder because its parent folder is read-only.",
      //
      "cannotCreateFolderUnexpectedError", "There was an unexpected error while creating the folder.",
      "cannotDeleteNotEmptyFolder", "This folder contains one or many file(s) and as a result cannot be removed.",
      "cannotDeleteReadOnlyFile", "Could not remove the  file or folder because it is read-only.",
      "couldNotDeleteFileError", "There was an error while deleting the file or folder.",
      "datasourceMustBeSelected", "You must select a datasource.",
      "fileReadError", "The file could not be read.",
      "ViewNameRequired", "You must provide a name for the view.",
      "TableAlreadyExists", "A table with the same name already exists.",
      "ViewAlreadyExists", "A view with the same name already exists.",
      "TableSelectionRequired", "You must select at least one table.",
      "TableEntityTypesDoNotMatch", "The selected tables must all have the same entity type.",
      "VariableDefinitionMethodRequired", "You must indicate how the view's variables are to be defined.",
      "DatasourceNameRequired", "You must provide a name for the project.",
      "DatasourceAlreadyExistsWithThisName", "A project already exists with this name.",
      "ExcelFileRequired", "An Excel file is required.", "ExcelFileSuffixInvalid",
      "Invalid Excel file suffix: .xls or .xlsx are expected.",
      "ViewMustBeAttachedToExistingOrNewDatasource",
      "The view must be attached to either an existing project or a new one.",
      "DuplicateDatasourceName", "The project name is already in use. Please choose another.",
      "UnknownError", "An unknown error has occurred.",
      "InternalError", "An internal error has occurred. Please contact technical support.",
      "UnhandledException",
      "An internal error has occurred. Please contact technical support and provide the following system error:<br /><br /><pre>{0}</pre>",
      "ProjectMomentarilyNotReloadable", "Project [{0}]'s command call is blocked momentarily.",
      //
      "DatasourceNameDisallowedChars", "Project names cannot contain colon or period characters.",
      "ViewNameDisallowedChars", "View names cannot contain colon or period characters.",
      "CSVFileRequired", "A CSV file is required.",
      "VCFFileRequired", "A VCF/BCF (optionally compressed) file is required.",
      "VCFParticipantSampleVariablesIdentical", "Participant ID variable and Sample Role variable cannot have identical names.",
      "XMLFileRequired", "An XML file is required.",
      "XMLFileSuffixInvalid", "Invalid XML file suffix: .xml is expected.",
      "ZipFileRequired", "A Zip file is required.",
      "SpssFileRequired", "A SPSS file is required.",
      "RSPSSFileRequired", "A SPSS file is required.",
      "RSASFileRequired", "A SAS file is required.",
      "RXPTFileRequired", "A SAS Transport file is required.",
      "RStataFileRequired", "A Stata file is required.",
      "ZipFileSuffixInvalid", "Invalid Zip file suffix: .zip is expected.",
      "InvalidFileType", "Invalid file type. Supported file types are: xls, xlsx, and xml.",
      "ReportTemplateWasNotFound", "The specified report template could not be found.",
      "ReportJobStarted", "Report task has been launched.  You can follow its progress in the task list.",
      "ReportTemplateAlreadyExistForTheSpecifiedName", "A report template already exist with the specified name.",
      "RReportDesignFileIsRequired", "A R Markdown file must be selected (.Rmd).",
      "CronExpressionIsRequired", "A schedule expression must be specified.",
      "NotificationEmailsAreInvalid", "One or more of the notifications emails specified are invalid.",
      "ReportTemplateNameIsRequired", "A name is required for the report template.",
      "OccurrenceGroupIsRequired", "An Occurrence Group must be specified for Repeatable variables.",
      "NewVariableNameIsRequired", "A name is required for the new variable to be created.",
      "ScriptIsRequired", "A script is required.",
      "JavascriptError", "Error in script '{0}': {1} ({2}:{3})",
      "JavaScriptException", "Error in script: {0}",
      "MagmaRuntimeException", "{0}",
      "MagmaRRuntimeException", "R error: {0}",
      "ValuesEvaluationError", "Error when evaluating values: {0}",
      "CopyFromVariableNameIsRequired",
      "You must enter the name of a variable from which the new variable will be created from.",
      "cannotSwitchTabBecauseOfUnsavedChanges",
      "You have unsaved changes. You need to press Save Changes before you can select another tab.",
      "UrlRequired", "You must provide the database's URL.",
      "OpalUsernameRequired", "You must indicate the user name.",
      "OpalPasswordRequired", "You must indicate the password.",
      "OpalTokenRequired", "You must indicate the personal access token.",
      "MustIndicateWhetherJdbcDatasourceShouldUseMetadataTables",
      "You must indicate whether meta-data tables are to be used or not.",
      "RowMustBePositiveInteger", "Row must be a positive integer (greater than or equal to 1).",
      "SpecificCharsetNotIndicated",
      "You have selected to use a specific character set but have not indicated which one.",
      "NoDataFileSelected", "You must select a data file.",
      "NoDataToCopy", "No data to copy to the current destination.",
      "DataCopyNewNameRequired", "New table name is required when copying within same datasource.",
      "DataCopyNewNameAlreadyExists", "Table name already exists.",
      "NoFileSelected", "You must select a file.",
      "CharsetNotAvailable", "The character set you have specified is not available.",
      "FieldSeparatorRequired", "The field separator is required.",
      "QuoteSeparatorRequired", "The quote separator is required.",
      "NotIgnoredConflicts", "Some conflicts were detected. Ignore modifications before applying changes.",
      "NoVariablesToBeImported", "No variables are to be imported.",
      "DataImportFailed", "The data importation has failed: {0}",
      "KeyPairAliasIsRequired", "Name is required",
      "KeyPairAlgorithmIsRequired", "Algorithm is required.",
      "KeyPairKeySizeIsRequired", "Size is required.",
      "KeyPairKeySizeNumeric", "Size must be a valid integer.",
      "KeyPairPrivateKeyPEMIsRequired", "Private Key in PEM format is required.",
      "KeyPairFirstAndLastNameIsRequired", "First and Last Name is required.",
      "KeyPairOrganizationalUnitIsRequired", "Organizational Unit is required.",
      "KeyPairOrganizationNameIsRequired", "Organization Name is required.",
      "KeyPairCityNameIsRequired", "City or Locality Name is required.",
      "KeyPairStateNameIsRequired", "State or Province Name is required.",
      "KeyPairCountryCodeIsRequired", "Country Code is required.",
      "KeyPairPublicKeyPEMIsRequired", "Public Key in PEM format is required.",
      "DestinationFileIsMissing", "Destination File is required.",
      "ExportDataMissingTables", "At least one table is required.",
      "ExportDataDuplicateTableNames", "At least two tables have the name '{0}'. Export cannot be completed.",
      "IdentifiersGenerationCompleted", "Identifiers generation completed.",
      "NoIdentifiersGenerated", "No Identifiers generated.",
      "IdentifiersGenerationFailed", "Identifiers generation has failed.",
      "IdentifiersGenerationPending", "An Identifiers generation task is currently running.",
      "MappedUnitsCannotBeIdentified", "Units to be mapped cannot be identified.",
      "TwoMappedUnitsExpected", "Exactly two Units to be mapped are expected.",
      "DataShieldMethodAlreadyExistWithTheSpecifiedName", "A method already exists with the specified name.",
      "DataShieldPackageAlreadyExistWithTheSpecifiedName", "A package already exists with the specified name.",
      "DataShieldROptionNameIsRequired", "Option name is required.",
      "DataShieldROptionInvalidName", "Option name is invalid. Options names are composed of one or many words separated by a '.' or a '_' and cannot start with a non-alphabetical character.",
      "DataShieldROptionValueIsRequired", "Option value is required.",
      "DataShieldMethodNameIsRequired", "A name is required.",
      "DataShieldRScriptIsRequired", "A R script is required.",
      "DataShieldRFunctionIsRequired", "A R function is required.",
      "DataShieldPackageNameIsRequired", "A name is required.",
      "RIsAlive", "R server is alive.",
      "RConnectionFailed", "Connection with R server failed.",
      "UnauthorizedOperation", "You are not allowed to perform this operation.",
      "CannotWriteToView", "Cannot modify a View using this operation. Use the View editor.",
      "DatesRangeInvalid", "Date range is invalid. Please make sure 'From' date precedes 'To' date.",
      "SelectDerivationMethod", "Select a derivation method.",
      "ReportTemplateAlreadyExists", "Report Template already exists.",
      "CouldNotCreateReportTemplate", "Could not create the Report Template.",
      "ReportTemplateCannotBeFound", "The Report Template cannot be found.",
      "DatasourceCreationFailed", "The datasource creation has failed: {0}",
      "DatasourceReadFailed", "The datasource cannot be read: {0}.",
      "CircularVariableDependency", "Circular dependency for variable '{0}'.:<br /><br /><pre>{1}</pre>",
      "NoSuchProject", "No such project '{0}'.",
      "NoSuchVCFSamplesMapping", "No such VCF sample mapping '{0}'.",
      "VCFFileImportTask", "Import task has been launched.  You can follow its progress in the task list.",
      "VCFFileExportTask", "Export task has been launched.  You can follow its progress in the task list.",
      "NoSuchDatabase", "No such database '{0}'.",
      "SubjectPrincipalNotFound", "No such subject principal '{0}'.",
      "SubjectProfileNotFound", "No profile for subject '{0}'.",
      "IdentifiersDatabaseNotFound", "Identifiers database not found.",
      "NoSuchIdentifiersMapping", "No such identifiers mapping '{0}'.",
      "NoSuchDatasource", "No such datasource '{0}'.",
      "NoSuchVariable", "No such variable '{0}'.",
      "NoSuchVariableInTable", "No such variable '{0}' in table '{1}'.",
      "NoSuchValueTable", "No such table '{0}'.",
      "NoSuchEntity", "No such entity '{0}' of type '{1}'.",
      "NoSuchEntityInTable", "No such entity '{0}' of type '{1}' in table '{2}'.",
      "NoSuchValueTableInDatasource", "No such table '{0}' in datasource '{1}'.",
      "NoSuchReportTemplate", "No such report template '{0}' in project '{1}'.",
      "InvalidVariableScript", "Invalid variable script:<br /><br /><pre>{0}</pre>",
      "DestinationTableRequired", "The destination table is required.",
      "DestinationTableNameInvalid", "The destination table name is not valid (must not contain '.' or ':').",
      "DestinationTableEntityTypeRequired", "The destination table entity type is required.",
      "DestinationTableCannotBeView", "The destination table cannot be a view.",
      "DataImportationProcessLaunched", "The data importation process can be followed using the Job ID: {0}.",
      "DataExportationProcessLaunched",
      "The data exportation process can be followed using the Job ID: {0}. Files will be exported to: {1}.",
      "DataCopyProcessLaunched", "The data copy process can be followed using the Job ID: {0}.",
      "DatabaseAlreadyExists", "A database with this name already exists.",
      "DatabaseConnectionOk", "{0}: Connection successful.",
      "DatabaseConnectionFailed", "Failed to connect to database.",
      "FailedToConnectToDatabase", "Failed to connect to database '{0}'.",
      "DatabaseIsNotEditable", "Database is used by a Datasource and is not editable.",
      "CannotFindDatabase", "Cannot find database named {0}.",
      "NameIsRequired", "A name is required.",
      "NameIsUnique", "A name must be unique.",
      "NameIsValidFileName", "Name cannot contain the following characters: <code># % &amp; { } \\ &lt; &gt; * ? / $ ! ' \" : @</code>",
      "EntityTypeIsRequired", "An entity type is required.",
      "IdentifiersAreRequired", "Some identifiers are required.",
      "SystemIdentifiersAreRequired", "Some system identifiers are required.",
      "IdentifiersAreMissing", "Some identifiers are missing.",
      "SystemIdentifiersAreMissing", "Some system identifiers are missing.",
      "DriverIsRequired", "A driver is required.",
      "DefaultEntityTypeIsRequired", "Default Entity Type is required for Tabular SQL schema.",
      "DatabaseUsageIsRequired", "Database usage is required.",
      "SQLSchemaIsRequired", "SQL schema is required.",
      "LimeSurveyDatabaseIsRequired", "LimeSurvey database is required.",
      "JdbcDatabaseIsRequired", "Jdbc database is required.",
      "UrlIsRequired", "A url is required.",
      "UsernameIsRequired", "A username is required.",
      "SubjectCredentialNameIsRequired", "A name is required.",
      "TableSelectionIsRequired", "At least one table must be selected.",
      "IdentifiersImportationCompleted", "Identifiers importation completed.",
      "IdentifiersImportationFailed", "Identifiers importation failed: {0}.",
      "IndexClearSelectAtLeastOne", "Select at least one index to clear.",
      "IndexScheduleSelectAtLeastOne", "Select at least one index to schedule.",
      "OldPasswordIsRequired", "Old password is required.",
      "OldPasswordMismatch", "Your old password is invalid.",
      "PasswordNotChanged", "You must choose a different password than your current password.",
      "NameIsRequired", "A name is required.",
      "PasswordIsRequired", "A password is required.",
      "InvalidCertificate", "The certificate is not valid.",
      "CertificateIsRequired", "A certificate is required.",
      "UsageIsRequired", "Database usage is required.",
      "SqlSchemaIsRequired", "Database SQL schema is required.",
      "JdbcTableSettingsNotValid", "Table SQL name is required.",
      "JdbcTableSettingsFactoriesNotValid", "Table SQL name and partition column are required.",
      "OpalURLIsRequired", "Opal address is required.",
      "RemoteDatasourceIsRequired", "Remote datasource name is required.",
      "TableSelectionIsRequired", "At least one table must be selected.",
      "IdentifiersImportationCompleted", "Identifiers importation completed.",
      "IdentifiersImportationFailed", "Identifiers importation failed: {0}.",
      "DataWriteNotAuthorized", "You are not allowed to write in project: {0}.",
      "TableWriteNotAuthorized", "You are not allowed to write in table: {0}.{1}.",
      "AccessDeniedToTableValues", "You are not allowed to view the values of table: {0}.",
      "NoTablesForEntityIdType", "Failed to retrieve tables for entity {0} and type {1}.",
      "NoVariablesFound", "Failed to retrieve the list of variables",
      "NoVariableValuesFound", "Failed to retrieve the list of variable values.",
      "EntityIdentifierNotFound", "{0} identifier {1} could not be found in table {2}.",
      "ParticipantIdentifiersAlreadyGenerated", "Participant identifiers have already been generated for the Unit {0}.",
      "RPackageInstalledButNotFound",
      "Package was probably successfully installed in R but cannot be found. Restarting R server might solve this issue.",
      "InvalidLocaleName", "Invalid locale name '{0}'. Please choose a valid locale name from the list.",
      "InvalidCharacterSetName", "Invalid character set '{0}'. Please choose a valid character set from the list.",
      "CopyVariableSelectAtLeastOne", "Select at least one variable to add.",
      "CopyVariableCurrentView", "The view cannot be the current view. Please select another destination view.",
      "CopyVariableNameRequired", "Variable name cannot be empty.",
      "CopyVariableNameColon", "Variable name '{0}' cannot contain ':'.",
      "CopyVariableNameAlreadyExists", "Duplicate variable name: {0}.",
      "CopyVariableIncompatibleEntityType", "Incompatible entity types: {0} / {1}.",
      "DeleteVariableSelectAtLeastOne", "Select at least one variable to remove.",
      "SearchServiceUnavailable", "Search operation failed. Please make sure the service is started.",
      "NoTableIndexed", "No table values have been indexed.",
      "NotIndexedTable", "The table values need to be indexed: {0}",
      "CriteriaLimitReached", "Search is limited to {0} criterion.",
      "MalformedSearchQuery", "Malformed search query.",
      "CannotMixVariableEntityTypes", "Variables with different entity types cannot be mixed.",
      "UserStatusChangedOk", "User {0} has been successfully {1}.",
      "UserUpdatedOk", "User {0} has been successfully updated.",
      "UserCreatedOk", "User {0} has been successfully added.",
      "UserDeletedOk", "User {0} has been successfully removed.",
      "UserAlreadyExists", "User name already exists.",
      "PasswordLengthMin", "Password must contain at least {0} characters.",
      "PasswordLengthMax", "Password must contain a maximum of {0} characters.",
      "PasswordLengthMax", "Password must contain a maximum of {0} characters.",
      "PasswordTooWeak", "Password is too weak: must contain at least one digit, one upper case alphabet, one lower case alphabet, one special character (which includes @#$%^&+=!) and no white space.",
      "PasswordsMustMatch", "Passwords do not match.",
      "GroupDeletedOk", "Group {0} has been successfully removed.",
      "GroupAlreadyExists", "Group already exists.",
      "ProjectNameRequired", "Project name is required.",
      "ProjectNameMustBeUnique", "A project already exists with this name.",
      "ProjectCreationFailed", "The datasource creation has failed: {0}.",
      "FileNotFound", "File not found: {0}.",
      "InvalidViewBackupFile", "The backup file does not contain a valid view: {0}.",
      "RestoredViewsCount", "{0} view(s) restored.",
      "FileNotAccessible", "File not accessible: {0}.",
      "MultipleIdentifiersDatabase", "Database for identifiers already exists: {0}.",
      "DatabaseAlreadyExists", "Database named {0} already exists.",
      "VcsScriptContentInfo", "The retrieved script content is from '{0}' committed by '{1}'.",
      "VcsOperationFailed", "Failed to retrieve revision information. Please contact technical support.",
      "GeneralConfigSaved", "Opal general configuration was successfully saved.",
      "PostInstallNotCompleted", "Post-install configuration not completed.",
      "TermNameMustBeUnique", "Term name must be unique.",
      "VocabularyNameMustBeUnique", "Vocabulary name must be unique.",
      "DeleteTableSelectAtLeastOne", "Select at least one table to remove.",
      "XMLOrExcelFileRequired", "An Excel or an XML file is required.",
      "CreateViewFailed", "Failed to create view. Make sure the view file is valid.",
      "SPSSOrExcelFileRequired", "An Excel or a SPSS file is required.",
      "ESQueryBadRequest", "Not a valid search query.", "DuplicateUserName", "A user with same name already exists.",
      "DuplicateSubjectProfile", "A user profile is already registered with the same name.",
      "AttributeNameIsRequired", "An attribute name is required.",
      "AttributeValueIsRequired", "At least one value is required.",
      "AttributeAlreadyExists", "An attribute with this namespace and name already exists.",
      "AttributeConflictExists", "Setting to this namespace would conflict with an existing attribute.",
      "PermissionRequired", "You must select a at most one permission type.",
      "DuplicateAclSubjectUser", "User '{0}' already exists, please choose a unique name.",
      "DuplicateAclSubjectGroup", "Group '{0}' already exists, please choose a unique name.",
      "InvalidCertificate", "Invalid certificate. Please Make sure to provide valid certificate keys.",
      "InvalidKeypair", "Failed to create key pair. Please make sure to provide a valid algorithm name and size.",
      "KeyEntryAlreadyExists", "A key with the same name exists, please choose a unique name.",
      "GeneralKeystoreError", "Key pair storage failed. Please make sure to provide valid certificate information.",
      "DerivedVariableNameRequired", "Derived variable name is required.",
      "DestinationViewNameRequired", "Destination View Name Required label.",
      "AddDerivedVariableToViewOnly", "A derived variable can only be added to a view.",
      "NamespaceCannotBeEmptyChars", "A namespace cannot contain only blank characters.",
      "DefaultCharSetIsRequired", "A default character set is required.",
      "LanguageIsRequired", "At least one language is required.",
      "NameHasInvalidCharacters",
      "Name contains invalid characters. Characters must be alphanumeric, space, hyphen or underscore.",
      "NameHasInvalidCharactersNoSpace",
      "Name contains invalid characters. Characters must be alphanumeric, hyphen or underscore.",
      "UnsupportedEncoding",
      "An unsupported encoding other than Opal's current encoding '{0}' was detected. Please contact your administrator for further assistance.",
      "SearchQueryIsInvalid", "Search query is invalid : {0}.",
      "ClusterNameIsRequired", "Cluster name is required.",
      "ShardsIsRequired", "Shards number is required.",
      "ReplicasIsRequired", "Replicas number is required.", "FolderNameIsRequired", "You must specify a folder name.",
      "DotNamesAreInvalid", "The names '.' and '..' are not permitted.",
      "FolderNameInvalidCharacters", "Folder names cannot contains characters '#' or '%'.",
      "CannotCopySelectedTablesWithinProject",
      "You cannot copy multiple tables within the same project. Copy each table individually or create a new project.",
      "CannotCopyAllTablesWithinProject", "This action would be applied to all tables. You cannot copy multiple tables within the same project. Copy each table individually or create a new project.",
      "IdentifierMappingFailed", "Identifiers mapping failed : {0}.",
      "TaxonomyGitUserRequired", "Github user or organization name is required.",
      "TaxonomyGitRepositoryRequired", "Github repository name is required.",
      "TaxonomyGitDownloadKeyRequired", "A download key is required.",
      "TaxonomyImportFailed", "Failed to import taxonomy. Please make sure all import parameters are correct.",
      "TaxonomyTagsImportFailed", "Failed to retrieve taxonomy released versions. Please contact technical support.",
      "TaxonomyNoTagsFound", "There are no released versions available. Please contact your administrator for further assistance.",
      "TaxonomyNotFound", "No such taxonomy with name: {0}.",
      "TaxonomyNameAlreadyExists", "A taxonomy with the same name already exists.",
      "TaxonomyAlreadyExists", "A taxonomy with the name '{0}' already exists. To override, click on 'Override existing taxonomy' option.",
      "VocabularyNotFound", "No such vocabulary in taxonomy '{0}' with name: {1}.",
      "TermNotFound", "No such term in taxonomy '{0}' and vocabulary '{1}' with name: {2}.",
      "IllegalArgument", "{0}",
      "NotFound", "Resource not found.",
      "Forbidden", "Operation not allowed.",
      "FileAlreadyExists", "File '{0}' already exists.",
      "FolderAlreadyExists", "Folder '{0}' already exists.",
      "ForbiddenPage", "You are not authorized to access to the content of this page.",
      "ParticipantIdVariableIsRequired", "Participant ID variable name is required.",
      "SampleRoleVariableIsRequired", "Sample Role variable name is required.",
      "NoVariableAddedToCart", "No variable added to the cart.",
      "VariableAddedToCart", "1 {1} variable added to the cart.",
      "VariablesAddedToCart", "{0} {1} variables added to the cart.",
      "PluginInstalled", "Plugin {0}:{1} installed. System restart is required to finalize installation.",
      "PluginPackageInstalled", "Plugin package {0} installed. System restart is required to finalize installation.",
      "PluginInstallationFailed", "Plugin installation failed.",
      "PluginRemoved", "Plugin {0} removed. System restart is required to finalize removal.",
      "PluginRemovalFailed", "Plugin removal failed.",
      "PluginReinstated", "Plugin {0} reinstated.",
      "PluginReinstateFailed", "Plugin reinstate failed.",
      "PluginRestarted", "Plugin service {0} restarted.",
      "PluginStartFailed", "Plugin service start failed.",
      "PluginStopFailed", "Plugin service stop failed.",
      "PluginsServiceError", "Plugins service error.",
      "PluginUpdateSiteError", "Plugin update site failed to be accessed.",
      "NotPluginArchive", "Not a plugin archive. Expected file suffix is '-dist.zip'",
      "LastUpdate", "Last repository check {0}.",
      "VariablesAnnotationApplied", "Annotation of {0} variables applied in table {1}.",
      "VariablesAnnotationRemoved", "Annotation of {0} variables removed in table {1}.",
      "VariablesAnnotationFailed", "Annotation of {0} variables failed for table {1}.",
      "VariablesAnnotationFailure", "Variables annotation update failed for table {0}.",
      "SearchFailure", "Search failed: {0}",
      "IndicesRemoved", "All indices were removed.",
      "CartLimitExceeded", "Cart is full, no items can be added anymore.",
      "BannedUserMins", "Too many login failures. Please try again in {0}mins.",
      "BannedUserMin", "Too many login failures. Please try again in {0}min.",
      "BannedUserSecs", "Too many login failures. Please try again in {0}secs.",
      "RServerRuntimeError", "An R operation failed. Please contact technical support.",
      "PluginTypeIsRequired", "Plugin type is required",
      "AnalysisTask", "Analysis task has been launched.  You can follow its progress in the task list.",
      "InvalidAnalysisPluginData", "Analysis {0} has invalid plugin data, Please contact technical support.",
      "NoSuchAnalysis", "No such analysis '{0}.",
      "IDProviderNameIsRequired", "A name is required.",
      "IDProviderClientIdIsRequired", "Client ID is required.",
      "IDProviderClientSecretIsRequired", "Client secret is required.",
      "IDProviderDiscoveryUriIsRequired", "Discovery URI is required.",
      "IDProviderDiscoveryUriIsUri", "Discovery URI must start with https (recommended) or http.",
      "IDProviderProviderUrlIsUri", "Account login page address must start with https (recommended) or http.",
      "TokenToCopyReminder", "Make sure to copy your new personal access token now. You won't be able to see it again!",
      "NameMustBeUnique", "Name must be unique.",
      "GHOrganizationIsRequired", "GitHub user or organization name is required",
      "ResourceProviderNotFound", "Resource provider cannot be found. The R package is missing or is not functional: {0}",
      "ResourceAssignSuccess", "Resource assignment in R was successful.",
      "ResourceAssignFailed", "{0}",
      "ResourceAssignOtherFailure", "Resource assignment in R has failed: {0}",
      "SQLError", "{0}",
      "FolderCreated", "Folder created: {0}",
      "DataShieldProfileInit", "DataSHIELD profile {0} initialized.",
      "ResourceViewReconnectSuccess", "Connection to resource succeeded.",
      "ResourceViewReconnectFailed", "Connection to resource failed. Please verify that the resource is well described.",
      "TaxonomyFileIsRequired", "A valid taxonomy file is required (.yml).",
  })
  Map<String, String> userMessageMap();

  @Description("No Tables label")
  @DefaultStringValue("No Tables")
  String noTablesLabel();

  @Description("Inner Join label")
  @DefaultStringValue("Inner")
  String innerJoinLabel();

  @Description("Missing label")
  @DefaultStringValue("Missing")
  String missingLabel();

  @Description("Categories label")
  @DefaultStringValue("Categories")
  String categoriesLabel();

  @Description("No Categories label")
  @DefaultStringValue("No Categories")
  String noCategoriesLabel();

  @Description("No Values label")
  @DefaultStringValue("No Values")
  String noValuesLabel();

  @Description("No Variables label")
  @DefaultStringValue("No Variables")
  String noVariablesLabel();

  @Description("No Plugins label")
  @DefaultStringValue("No Plugins")
  String noPluginsLabel();

  @Description("Attributes label")
  @DefaultStringValue("Attributes")
  String attributesLabel();

  @Description("Annotations label")
  @DefaultStringValue("Annotations")
  String annotationsLabel();

  @Description("No Attributes label")
  @DefaultStringValue("No Attributes")
  String noAttributesLabel();

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

  @Description("Mime Type label")
  @DefaultStringValue("Mime Type")
  String mimeTypeLabel();

  @Description("Repeatable label")
  @DefaultStringValue("Repeatable")
  String repeatableLabel();

  @Description("Occurrence Group label")
  @DefaultStringValue("Occurrence Group")
  String occurrenceGroupLabel();

  @Description("Nature label")
  @DefaultStringValue("Nature")
  String natureLabel();

  @Description("File Selector title")
  @DefaultStringValue("File Selector")
  String fileSelectorTitle();

  @Description("Data import Compared Datasources Report instructions")
  @DefaultStringValue(
      "Review and select the data dictionaries that you wish to import.")
  String dataImportComparedDatasourcesReportStep();

  @Description("Data import Values instructions")
  @DefaultStringValue("Review the data that will be imported.")
  String dataImportValuesStep();

  @Description("Csv label")
  @DefaultStringValue("CSV")
  String csvLabel();

  @Description("Opal XML label")
  @DefaultStringValue("Opal Archive")
  String opalXmlLabel();

  @Description("SPSS label")
  @DefaultStringValue("SPSS")
  String spssLabel();

  @Description("RSPSS label")
  @DefaultStringValue("SPSS (R)")
  String rSPSSLabel();

  @Description("RSPSS help")
  @DefaultStringValue("Data in SPSS format are packaged in a file with suffix .sav or .zsav (when compressed).")
  String rSPSSHelp();

  @Description("RZSPSS label")
  @DefaultStringValue("SPSS Compressed (R)")
  String rZSPSSLabel();

  @Description("RSPSS help")
  @DefaultStringValue("Data in SPSS compressed format are packaged in a file with suffix .zsav.")
  String rZSPSSHelp();

  @Description("RSAS label")
  @DefaultStringValue("SAS (R)")
  String rSASLabel();

  @Description("RSAS help")
  @DefaultStringValue("Data in SAS format are packaged in a file with suffix .sas7bdat. If there is a file in the parent folder with same base name and suffix .sas7bcat, it will be automatically used as the catalog file.")
  String rSASHelp();

  @Description("RXPT label")
  @DefaultStringValue("SAS Transport (R)")
  String rXPTLabel();

  @Description("RSAS help")
  @DefaultStringValue("Data in SAS Transport format are packaged in a file with suffix .xpt.")
  String rXPTHelp();

  @Description("RStata label")
  @DefaultStringValue("Stata (R)")
  String rStataLabel();

  @Description("RStata help")
  @DefaultStringValue("Data in Stata format are packaged in a file with suffix .dta.")
  String rStataHelp();

  @Description("RDS label")
  @DefaultStringValue("RDS (R)")
  String rDSLabel();

  @Description("RDS help")
  @DefaultStringValue("Data in RDS format (serialized single R object) are packaged in a file with suffix .rds.")
  String rDSHelp();

  @Description("Opal REST label")
  @DefaultStringValue("Opal")
  String opalRestLabel();

  @Description("Error label")
  @DefaultStringValue("Error")
  String errorLabel();

  @Description("Datasource parsing error map")
  @DefaultStringMapValue({
      "CategoryNameRequired", "[{0}:{1}] Category name required: table '{2}', variable '{3}'",
      "CategoryVariableNameRequired", "[{0}:{1}] Category variable name required: table '{2}'",
      "DuplicateCategoryName", "[{0}:{1}] Duplicate category name: table '{2}', variable '{3}', category '{4}'",
      "DuplicateColumns", "[{0}:{1}] Duplicate columns: table '{2}', column '{3}'",
      "DuplicateVariableName", "[{0}:{1}] Duplicate variable name: table '{2}', variable '{3}'",
      "TableDefinitionErrors", "Table definition errors",
      "UnexpectedErrorInCategory", "[{0}:{1}] Unexpected error in category definition: table '{2}', variable '{3}'",
      "UnexpectedErrorInVariable", "[{0}:{1}] Unexpected error in variable definition: table '{2}'",
      "UnidentifiedVariableName", "[{0}:{1}] Unidentified variable name: table '{2}', variable '{3}'",
      "UnknownValueType", "[{0}:{1}] Unknown value type: table '{2}', variable '{3}', type '{4}'",
      "VariableCategoriesDefinitionErrors", "Variable categories definition errors",
      "VariableNameCannotContainColon", "[{0}:{1}] Variable name cannot contain colon: table '{2}', variable '{3}'",
      "VariableNameRequired", "[{0}:{1}] Variable name required: table '{2}'",
      "CsvInitialisationError", "Error occurred initialising csv datasource",
      "CsvVariablesHeaderMustContainName", "The variables.csv header must contain 'name'",
      "CsvVariablesHeaderMustContainValueType", "The variables.csv header must contain 'valueType'.",
      "CsvVariablesHeaderMustContainEntityType", "The variables.csv header must contain 'entityType'.",
      "CsvUnableToGetVariableValueForEntity", "Unable to get value for entity {0} and variable {1}",
      "CsvCannotCreateWriter", "Cannot create writer",
      "CsvCannotSetVariableHeader", "Cannot set variables header",
      "CsvCannotObtainWriter", "Cannot get csv writer",
      "CsvCannotObtainReader", "Cannot get csv reader",
      "LimeDuplicateVariableName", "[{0}] Survey contains duplicated variable name: {1}",
      "InvalidCharsetCharacter",
      "File contains invalid characters at row '{0}' in string '{1}'. Please make sure the file is a valid SPSS file and that you have chosen the correct character set.",
//
      "InvalidCategoryCharsetCharacter",
      "File contains invalid characters at variable row '{0}' for category '{1}' in string '{2}'. Please make sure the file is a valid SPSS file and that you have chosen the correct character set.",
//
      "SpssFailedToLoadMetadata",
      "Failed to load metadata from file '{0}'. Please make sure you have chosen the correct character set. ",
      "SpssFailedToLoadData",
      "Failed to load data from file '{0}'. Please make sure you have chosen the correct character set.",
      "SpssDuplicateEntity",
      "Duplicate entity identifier '{0}' at data row '{1}'. Please make sure that the variable '{2}' representing entities has unique values.",
      "SpssFailedToCreateVariable",
      "Failed to create variablle '{0}' at data row '{1}'. Please make sure that the file is a valid SPSS file.",
//
      "FailedToOpenFile", "Failed to open file '{0}'. Please make sure you have chosen the correct character set.",
      "LimeUnknownQuestionType",
      "Unknown type for Limesurvey question '{0}'. Contact your administrator to find out whether this question type is handled by Opal." })
  Map<String, String> datasourceParsingErrorMap();

  @Description("Datasource comparison error map")
  @DefaultStringMapValue({ "IncompatibleValueType", "Incompatible value types: {0} / {1}",
      "IncompatibleEntityType", "Incompatible entity types: {0} / {1}",
      "CsvVariableMissing",
      "Variable name exists in csv data file, but no Variable associated with this name exists in the destination table" })
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

  @Description("No Options label")
  @DefaultStringValue("No options")
  String noOptionsLabel();

  @Description("No Tokens label")
  @DefaultStringValue("No tokens")
  String noTokensLabel();

  @Description("Summary preview pending on label")
  @DefaultStringValue("Summary preview pending on")
  String summaryPreviewPendingLabel();

  @Description("Full summary pending... label")
  @DefaultStringValue("Full summary pending...")
  String summaryFullPendingLabel();

  @Description("Variable summary on label")
  @DefaultStringValue("Variable summary on")
  String summaryOnLabel();

  @Description("Fetch variable summary on label")
  @DefaultStringValue("Fetch variable summary on")
  String summaryFetchSummaryLabel();

  @Description("Remove label")
  @DefaultStringValue("Remove")
  String removeLabel();

  @Description("View label")
  @DefaultStringValue("View")
  String viewLabel();

  @Description("Add View title")
  @DefaultStringValue("Add View")
  String addViewTitle();

  @Description("Add Table title")
  @DefaultStringValue("Add Table")
  String addTableTitle();

  @Description("Add Identifiers Table title")
  @DefaultStringValue("Add Identifiers Table")
  String addIdentifiersTableTitle();

  @Description("Import System Identifiers title")
  @DefaultStringValue("Import System Identifiers")
  String importSystemIdentifiersTitle();

  @Description("Import Identifiers Mapping title")
  @DefaultStringValue("Import Identifiers Mapping")
  String importIdentifiersMappingTitle();

  @Description("Identifiers Tables title")
  @DefaultStringValue("Identifiers Tables")
  String identifiersTablesTitle();

  @Description("Plugin Service Configuration title")
  @DefaultStringValue("Plugin Service Configuration")
  String pluginServiceConfigurationTitle();

  @Description("Add Table label")
  @DefaultStringValue("Add table...")
  String addTableLabel();

  @Description("Add Table no storage label")
  @DefaultStringValue("Add table (no storage)")
  String addTableNoStorageLabel();

  @Description("Add Update Tables label")
  @DefaultStringValue("Add/update tables from dictionary...")
  String addUpdateTablesLabel();

  @Description("Add Update Tables no storage label")
  @DefaultStringValue("Add/update tables from dictionary (no storage)")
  String addUpdateTablesNoStorageLabel();

  @Description("Script label")
  @DefaultStringValue("Script")
  String scriptLabel();

  @Description("Script Evaluation label")
  @DefaultStringValue("Script Evaluation")
  String scriptEvaluationLabel();

  @Description("Apply annotation title")
  @DefaultStringValue("Apply Annotation")
  String applyAnnotation();

  @Description("Apply annotation help")
  @DefaultStringValue("Annotate each selected variable with a controlled vocabulary.")
  String applyAnnotationHelp();

  @Description("Remove annotation title")
  @DefaultStringValue("Remove Annotation")
  String removeAnnotation();

  @Description("Remove annotations help")
  @DefaultStringValue("Remove controlled vocabulary annotation from each selected variable.")
  String removeAnnotationsHelp();

  @Description("Remove annotation help")
  @DefaultStringValue("Remove controlled vocabulary annotation from variable.")
  String removeAnnotationHelp();

  @Description("Apply attribute title")
  @DefaultStringValue("Apply Attribute")
  String applyAttribute();

  @Description("Apply attribute help")
  @DefaultStringValue("Apply attribute to each selected variable.")
  String applyAttributeHelp();

  @Description("Add attribute title")
  @DefaultStringValue("Add Attribute")
  String addAttribute();

  @Description("Rock Default Credentials help")
  @DefaultStringValue("Rock credentials are the default ones, as defined in the main Opal configuration file.")
  String rockDefaultCredentialsHelp();

  @Description("Rock Administrator Credentials help")
  @DefaultStringValue("Rock credentials are the ones of a user with administrator role. It will be used both for managing the server (start/stop, R packages etc.) and for creating new R sessions.")
  String rockAdministratorCredentialsHelp();

  @Description("Rock Manager User Credentials help")
  @DefaultStringValue("One Rock credentials for managing the server (start/stop, R packages etc.) and another one for creating new R sessions. Having both different user roles allows to restrict user privilegies in the R sessions.")
  String rockManagerUserCredentialsHelp();

  @Description("Add Annotation help")
  @DefaultStringValue("Apply annotation to the variable by selecting a controlled vocabulary.")
  String addAnnotationHelp();

  @Description("Edit annotation title")
  @DefaultStringValue("Edit Annotation")
  String editAnnotation();

  @Description("Edit annotation help")
  @DefaultStringValue("Edit annotation of the variable by selecting a controlled vocabulary.")
  String editAnnotationHelp();

  @Description("Edit annotations title")
  @DefaultStringValue("Annotations")
  String editAnnotations();

  @Description("Edit attribute title")
  @DefaultStringValue("Edit Attribute")
  String editAttribute();

  @Description("Edit specific attribute title")
  @DefaultStringValue("Edit {0}")
  String editSpecificAttribute();

  @Description("Edit attributes title")
  @DefaultStringValue("Edit Attributes")
  String editAttributes();

  @Description("Edit attributes title")
  @DefaultStringValue("Edit the namespace for each selected attribute.")
  String editAttributesHelp();

  @Description("Remove attribute title")
  @DefaultStringValue("Remove Attribute")
  String removeAttribute();

  @Description("Remove attribute help")
  @DefaultStringValue("Remove attribute from each selected variable.")
  String removeAttributeHelp();

  @Description("Generate Identifiers label")
  @DefaultStringValue("Generate Identifiers")
  String generateIdentifiers();

  @Description("Identifiers are generated with a checksum digit")
  @DefaultStringValue("With Checksum Digit")
  String geValidWithChecksum();

  @Description("Checksum help")
  @DefaultStringValue("Generate identifiers with its Luhn checksum.")
  String geValidWithChecksumHelp();

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

  @Description("Basic label")
  @DefaultStringValue("Basic")
  String basicLabel();

  @Description("Advanced label")
  @DefaultStringValue("Advanced")
  String advancedLabel();

  @Description("Previous label")
  @DefaultStringValue("< Previous")
  String previousLabel();

  @Description("KeyType map")
  @DefaultStringMapValue({ "KEY_PAIR", "Key Pair", "CERTIFICATE", "Certificate" })
  Map<String, String> keyTypeMap();

  @Description("Import Key Pair title")
  @DefaultStringValue("Import Key Pair")
  String importKeyPairTitle();

  @Description("Import Certificate title")
  @DefaultStringValue("Import Certificate Pair")
  String importCertificateTitle();

  @Description("Data export title")
  @DefaultStringValue("Export Data")
  String exportData();

  @Description("Data copy title")
  @DefaultStringValue("Copy Data")
  String copyData();

  @Description("View copy title")
  @DefaultStringValue("Copy View")
  String copyView();

  @Description("Variables Import File Selection Step")
  @DefaultStringValue(
      "Select an Excel variables file or a View XML file for batch edition of tables and variables.")
  String variablesImportFileSelectionStep();

  @Description("Variables Import Compare Step")
  @DefaultStringValue("Review the modifications before applying them.")
  String variablesImportCompareStep();

  @Description("Variables Import Pending")
  @DefaultStringValue("Importing Variables...")
  String variablesImportPending();

  @Description("Add View Success")
  @DefaultStringValue("View successfully created.")
  String addViewSuccess();

  @Description("Update View Success")
  @DefaultStringValue("View successfully updated.")
  String updateViewSuccess();

  @Description("Datasource Type map")
  @DefaultStringMapValue({ "mongodb", "MongoDB",
      "hibernate", "Opal SQL",
      "jdbc", "Tabular SQL",
      "fs", "XML",
      "csv", "CSV",
      "excel", "Excel",
      "null", "None" })
  Map<String, String> datasourceTypeMap();

  @Description("Data Import Format Step")
  @DefaultStringValue("Select the format of data you wish to import.")
  String dataImportFormatStep();

  @Description("Data Import File Step")
  @DefaultStringValue("Select the source to be imported.")
  String dataImportFileStep();

  @Description("Data Import Configuration Step")
  @DefaultStringValue("Configure data import")
  String configureDataImport();

  @Description("Package description label")
  @DefaultStringValue("Package Description")
  String dataShieldPackageDescription();

  @Description("DS Profile init title")
  @DefaultStringValue("Profile Initialization")
  String dataShieldProfileInitTitle();

  @Description("Add DataShield package label")
  @DefaultStringValue("Add DataSHIELD Package")
  String addDataShieldPackage();

  @Description("DS profile enabled label")
  @DefaultStringValue("Profile is ready for use.")
  String dataShieldProfileEnabledLabel();

  @Description("DS profile disabled label")
  @DefaultStringValue("Profile is not available for use.")
  String dataShieldProfileDisabledLabel();

  @Description("DS profile restricted label")
  @DefaultStringValue("Only DataSHIELD users with proper permissions can access this profile.")
  String dataShieldProfileRestrictedLabel();

  @Description("DS profile unrestricted label")
  @DefaultStringValue("Any DataSHIELD user can access this profile.")
  String dataShieldProfileUnrestrictedLabel();

  @Description("Add DataShield profile label")
  @DefaultStringValue("Add Profile")
  String addDataShieldProfile();

  @Description("Add DataShield method label")
  @DefaultStringValue("Add Method")
  String addDataShieldMethod();

  @Description("Edit DataShield method label")
  @DefaultStringValue("Edit Method")
  String editDataShieldMethod();

  @Description("Add R Option title")
  @DefaultStringValue("Add R Option")
  String addROptionTitle();

  @Description("Edit R Option title")
  @DefaultStringValue("Edit R Option")
  String editROptionTitle();

  @Description("R Script label")
  @DefaultStringValue("R Script")
  String rScriptLabel();

  @Description("R Function label")
  @DefaultStringValue("R Function")
  String rFunctionLabel();

  @Description("Install R package label")
  @DefaultStringValue("Install R Package")
  String installRPackage();

  @Description("Value Type map")
  @DefaultStringMapValue({ "TEXTUAL", "Textual",
      "text.type", "text",
      "NUMERICAL", "Numerical",
      "integer.type", "integer",
      "decimal.type", "decimal",
      "TEMPORAL", "Temporal",
      "date.type", "date",
      "datetime.type", "datetime",
      "GEOSPATIAL", "Geospatial",
      "point.type", "point",
      "linestring.type", "linestring",
      "polygon.type", "polygon",
      "OTHER", "Other",
      "binary.type", "binary",
      "boolean.type", "boolean",
      "locale.type", "locale"
  })
  Map<String, String> valueTypeMap();

  @Description("Token Data Access map")
  @DefaultStringMapValue({
      "READ", "Read only",
      "READ_NO_VALUES", "Read only, without individual-level data"
  })
  Map<String, String> tokenAccessMap();

  @Description("Token Tasks map")
  @DefaultStringMapValue({
      "import", "Import",
      "export", "Export",
      "copy", "Copy",
      "report", "Report",
      "analyse", "Analyse",
      "import_vcf", "Import VCF",
      "export_vcf", "Export VCF",
      "backup", "Backup",
      "restore", "Restore"
  })
  Map<String, String> tokenTasksMap();

  @Description("Subject Type map")
  @DefaultStringMapValue({ "USER", "User",
      "GROUP", "Group" })
  Map<String, String> shortSubjectTypeMap();

  @Description("Add Subject Permission map")
  @DefaultStringMapValue({ "USER.title", "Add User Permission",
      "GROUP.title", "Add Group Permission" })
  Map<String, String> addSubjectPermissionMap();

  @Description("Subject Type Users label")
  @DefaultStringValue("Users")
  String subjectTypeUsers();

  @Description("Subject Type Groups label")
  @DefaultStringValue("Groups")
  String subjectTypeGroups();

  @Description("Invalid Destination View label")
  @DefaultStringValue("Not a valid destination view.")
  String invalidDestinationView();

  @Description("Derived Variable Evaluation label")
  @DefaultStringValue(
      "Review the summary and the values of the derived variable.")
  String derivedVariableEvaluation();

  @Description("Save Derived Variable label")
  @DefaultStringValue(
      "Name the derived variable and select the view in which it will appear.")
  String saveDerivedVariable();

  @Description("Recode Categories Step title")
  @DefaultStringValue(
      "Recode categories and observed distinct values to new values.")
  String recodeCategoriesStepTitle();

  @Description("Recode Boolean Step title")
  @DefaultStringValue("Recode logical values to new values.")
  String recodeBooleanStepTitle();

  @Description("Derive From Variable Step title")
  @DefaultStringValue(
      "Select from which variable derivation should be performed")
  String deriveFromVariableStepTitle();

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
  @DefaultStringValue(
      "Dates and times can be grouped together using the following methods:")
  String recodeTemporalMethodStepTitle();

  @Description("Recode Temporal Map Step title")
  @DefaultStringValue("Map each time range to a new value.")
  String recodeTemporalMapStepTitle();

  @Description("Recode Numerical Method Step title")
  @DefaultStringValue(
      "Numerical values can be grouped together using the following methods:")
  String recodeNumericalMethodStepTitle();

  @Description("Recode Numerical Map Step title")
  @DefaultStringValue("Map each range or discrete value to a new value.")
  String recodeNumericalMapStepTitle();

  @Description("Recode Open Textual Method Step Title")
  @DefaultStringValue(
      "Group the values using the following method.")
  String recodeOpenTextualMethodStepTitle();

  @Description("Recode Open Textual Map Step Title")
  @DefaultStringValue("Map original values to new values.")
  String recodeOpenTextualMapStepTitle();

  @Description("Recode Custom Step Title")
  @DefaultStringValue("Specify the custom derivation script.")
  String recodeCustomDeriveStepTitle();

  @Description("Time map")
  @DefaultStringMapValue({
      "Hour", "Hour",
      "Monday", "Monday",
      "Tuesday", "Tuesday",
      "Wednesday", "Wednesday",
      "Thursday", "Thursday",
      "Friday", "Friday",
      "Saturday", "Saturday",
      "Sunday", "Sunday",
      "MONDAY", "Monday",
      "TUESDAY", "Tuesday",
      "WEDNESDAY", "Wednesday",
      "THURSDAY", "Thursday",
      "FRIDAY", "Friday",
      "SATURDAY", "Saturday",
      "SUNDAY", "Sunday",
      "January", "January",
      "February", "February",
      "March", "March",
      "April", "April",
      "May", "May",
      "June", "June",
      "July", "July",
      "August", "August",
      "September", "September",
      "October", "October",
      "November", "November",
      "December", "December",
      "Second", "Second",
      "Minute", "Minute",
      "Hour", "Hour",
      "Day", "Day",
      "Week", "Week",
      "Month", "Month",
      "Quarter", "Quarter",
      "Semester", "Semester",
      "Year", "Year",
      "Lustrum", "Lustrum",
      "Decade", "Decade",
      "Century", "Century",
      "Millenium", "Millenium",
      "Era", "Era"
  })
  Map<String, String> timeMap();

  @Description("Time Group map")
  @DefaultStringMapValue({
      "HOUR_OF_DAY", "Hour of Day",
      "DAY_OF_WEEK", "Day of Week",
      "DAY_OF_MONTH", "Day of Month",
      "DAY_OF_YEAR", "Day of Year",
      "WEEK_OF_MONTH", "Week of Month",
      "WEEK_OF_YEAR", "Week of Year",
      "MONTH", "Month",
      "MONTH_OF_YEAR", "Month of Year",
      "QUARTER_OF_YEAR", "Quarter of Year",
      "QUARTER", "Quarter",
      "SEMESTER_OF_YEAR", "Semester of Year",
      "SEMESTER", "Semester",
      "YEAR", "Year",
      "LUSTRUM", "Lustrum (5 years period)",
      "DECADE", "Decade (10 years period)",
      "CENTURY", "Century"
  })
  Map<String, String> timeGroupMap();

  @Description("DataSHIELD Labels")
  @DefaultStringMapValue({
      "Aggregate", "Aggregate",
      "Assign", "Assign"
  })
  Map<String, String> dataShieldLabelsMap();

  @Description("Files Folders selection")
  @DefaultStringMapValue({ "FILE", "file",
      "FOLDER", "folder",
      "FILE_OR_FOLDER", "file or folder"
  })
  Map<String, String> fileFolderTypeMap();

  @Description("Project Tab Names")
  @DefaultStringMapValue({ "HOME", "Home",
      "DASHBOARD", "Dashboard",
      "TABLES", "Tables",
      "RESOURCES", "Resources",
      "FILES", "Files",
      "GENOTYPES", "Genotypes",
      "VISUALISATION", "Data visualization",
      "REPORTS", "Reports",
      "TASKS", "Tasks",
      "PERMISSIONS", "Permissions",
      "PERMISSIONS", "Permissions",
      "KEYSTORE", "Data Exchange",
      "ADMINISTRATION", "Administration"
  })
  Map<String, String> projectTabNameMap();

  @Description("Bookmark Type name")
  @DefaultStringMapValue({ "PROJECT", "Project",
      "TABLE", "Table",
      "VARIABLE", "Variable"
  })
  Map<String, String> bookmarkTypeMap();

  @Description("Discrete Value Required label")
  @DefaultStringValue("A valid discrete value is required.")
  String discreteValueRequired();

  @Description("Lower Value Limit Required label")
  @DefaultStringValue("Lower value limit is required.")
  String lowerValueLimitRequired();

  @Description("Upper Value Limit Required label")
  @DefaultStringValue("Upper value limit is required.")
  String upperValueLimitRequired();

  @Description("Lower Limit Greater Than Upper Limit label")
  @DefaultStringValue(
      "Lower value limit cannot be greater than upper value limit.")
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

  @Description("Script Evaluation Failed Label")
  @DefaultStringValue(
      "Script evaluation failed: check if value type is correct.")
  String scriptEvaluationFailed();

  @Description("Property label")
  @DefaultStringValue("Property")
  String property();

  @Description("Value label")
  @DefaultStringValue("Value")
  String value();

  @Description("Values label")
  @DefaultStringValue("Values")
  String values();

  @Description("Analyses label")
  @DefaultStringValue("Analyses")
  String analyses();

  @Description("Analysis label")
  @DefaultStringValue("Analysis")
  String analysis();

  @Description("Analysis Name label")
  @DefaultStringValue("Name")
  String analysisNameLabel();

  @Description("Analysis Type label")
  @DefaultStringValue("Type")
  String analysisTypeLabel();

  @Description("Analysis Add Modal title")
  @DefaultStringValue("Add Analysis")
  String analysisAddModalTitle();

  @Description("Analysis Status Column label")
  @DefaultStringValue("Status")
  String analysisStatusLabel();

  @Description("Analysis Total Column label")
  @DefaultStringValue("Total")
  String analysisTotalLabel();

  @Description("Analysis Message Column label")
  @DefaultStringValue("Message")
  String analysisMessageLabel();

  @Description("Analysis Result Date Column label")
  @DefaultStringValue("Date")
  String analysisResultDateLabel();

  @Description("Analysis Result History label")
  @DefaultStringValue("History")
  String analysisResultHistoryLabel();

  @Description("Analysis Status map")
  @DefaultStringMapValue({ "NOT_STARTED", "Not Started",
    "IN_PROGRESS", "In Progress",
    "PASSED", "Passed",
    "FAILED", "Failed",
    "ERROR", "Error",
    "IGNORED", "Ignored",
  })
  Map<String, String> analysisStatusMap();

  @Description("Filter Analyses placeholder")
  @DefaultStringValue("Filter Analyses...")
  String filterAnalysePlaceholder();

  @Description("Participant label")
  @DefaultStringValue("Participant")
  String participant();

  @Description("Participants label")
  @DefaultStringValue("Participants")
  String participantsLabel();

  @Description("Download label")
  @DefaultStringValue("Download")
  String downloadLabel();

  @Description("Show label")
  @DefaultStringValue("Show")
  String showLabel();

  @Description("Hide label")
  @DefaultStringValue("Hide")
  String hideLabel();

  @Description("Add SQL Database")
  @DefaultStringValue("Register SQL Database")
  String addSQLDatabase();

  @Description("Edit SQL Database")
  @DefaultStringValue("Edit SQL Database")
  String editSQLDatabase();

  @Description("Add MongoDB Database")
  @DefaultStringValue("Register MongoDB Database")
  String addMongoDatabase();

  @Description("Edit MongoDB Database")
  @DefaultStringValue("Edit MongoDB Database")
  String editMongoDatabase();

  @Description("Username label")
  @DefaultStringValue("Username")
  String usernameLabel();

  @Description("Password label")
  @DefaultStringValue("Password")
  String passwordLabel();

  @Description("Certificate label")
  @DefaultStringValue("Certificate")
  String certificateLabel();

  @Description("Driver label")
  @DefaultStringValue("Driver")
  String driverLabel();

  @Description("Url label")
  @DefaultStringValue("URL")
  String urlLabel();

  @Description("Host label")
  @DefaultStringValue("Host")
  String hostLabel();

  @Description("Format label")
  @DefaultStringValue("Format")
  String formatLabel();

  @Description("Account Login label")
  @DefaultStringValue("Account Login")
  String accountLoginLabel();

  @Description("Provider Login label")
  @DefaultStringValue("Provider's Login")
  String providerLoginLabel();

  @Description("Discovery URI label")
  @DefaultStringValue("Discovery URI")
  String discoveryURILabel();

  @Description("Parameters label")
  @DefaultStringValue("Parameters")
  String parametersLabel();

  @Description("Usage label")
  @DefaultStringValue("Usage")
  String usageLabel();

  @Description("SQL Schema label")
  @DefaultStringValue("SQL Schema")
  String sqlSchemaLabel();

  @Description("Schema label")
  @DefaultStringValue("Schema")
  String schemaLabel();

  @Description("SQL label")
  @DefaultStringValue("SQL")
  String sqlLabel();

  @Description("MongoDB label")
  @DefaultStringValue("MongoDB")
  String mongoDbLabel();

  @Description("Opal Mongo label")
  @DefaultStringValue("Opal Mongo")
  String opalMongoLabel();

  @Description("Default Entity Type label")
  @DefaultStringValue("Default Entity Type")
  String defaultEntityTypeLabel();

  @Description("Key/Value label")
  @DefaultStringValue("key=value")
  String keyValueLabel();

  @Description("Created label")
  @DefaultStringValue("Created")
  String createdLabel();

  @Description("Last Update label")
  @DefaultStringValue("Last updated")
  String lastUpdatedLabel();

  @Description("Last Access label")
  @DefaultStringValue("Last access")
  String lastAccessLabel();

  @Description("Table last update")
  @DefaultStringValue("Table last update")
  String tableLastUpdateLabel();

  @Description("Index last update")
  @DefaultStringValue("Index last update")
  String indexLastUpdateLabel();

  @Description("Schedule")
  @DefaultStringValue("Schedule")
  String scheduleLabel();

  @Description("Manually")
  @DefaultStringValue("Manually")
  String manuallyLabel();

  @Description("Every 5 minutes")
  @DefaultStringValue("Every 5 minutes")
  String minutes5Label();

  @Description("Every 15 minutes")
  @DefaultStringValue("Every 15 minutes")
  String minutes15Label();

  @Description("Every 30 minutes")
  @DefaultStringValue("Every 30 minutes")
  String minutes30Label();

  @Description("Every hour")
  @DefaultStringValue("Every hour")
  String hourlyLabel();

  @Description("Every hour at")
  @DefaultStringValue("Every hour at {0} minutes")
  String hourlyAtLabel();

  @Description("Every day")
  @DefaultStringValue("Every day")
  String dailyLabel();

  @Description("Every day at")
  @DefaultStringValue("Every day at {0}:{1}")
  String dailyAtLabel();

  @Description("Every week")
  @DefaultStringValue("Every week")
  String weeklyLabel();

  @Description("Every week at")
  @DefaultStringValue("Every week on {0} at {1}:{2}")
  String weeklyAtLabel();

  @Description("Edit schedule")
  @DefaultStringValue("Edit schedule")
  String editScheduleLabel();

  @Description("minutes")
  @DefaultStringValue("minutes")
  String minutesLabel();

  @Description("Index outdated")
  @DefaultStringValue("Indexation outdated and scheduled")
  String indexOutdatedScheduled();

  @Description("Index outdated and not scheduled")
  @DefaultStringValue("Indexation outdated and not scheduled")
  String indexOutdatedNotScheduled();

  @Description("Index up-to-date")
  @DefaultStringValue("Indexation up-to-date")
  String indexUpToDate();

  @Description("Indexation not scheduled")
  @DefaultStringValue("Indexation not scheduled")
  String indexNotScheduled();

  @Description("Indexation in progress")
  @DefaultStringValue("Indexation in progress")
  String indexInProgress();

  @Description("Table index is out-of-date")
  @DefaultStringValue("Table index is out-of-date")
  String indexStatusOutOfDate();

  @Description("Indexing of this table is in progress")
  @DefaultStringValue("Indexing of this table is in progress")
  String indexStatusInProgress();

  @Description("Values of this table are not indexed")
  @DefaultStringValue("Values of this table are not indexed")
  String indexStatusNotIndexed();

  @Description("Indices label")
  @DefaultStringValue("Indices")
  String indicesLabel();

  @Description("Table index is up-to-date label")
  @DefaultStringValue("Table index is up-to-date")
  String indexAlertUpToDate();

  @Description("Select some projects label")
  @DefaultStringValue("Select some projects...")
  String selectSomeProjects();

  @Description("Select some Tables label")
  @DefaultStringValue("Select some tables...")
  String selectSomeTables();

  @Description("Select some Terms label")
  @DefaultStringValue("Select some terms...")
  String selectSomeTerms();

  @Description("File based datasources label")
  @DefaultStringValue("Files")
  String fileBasedDatasources();

  @Description("Remote server based datasources label")
  @DefaultStringValue("Servers")
  String remoteServerBasedDatasources();

  @Description("Public datasources label")
  @DefaultStringValue("Public Datasources")
  String publicDatasources();

  @Description("Elasticsearch Configuration")
  @DefaultStringValue("Elasticsearch Configuration")
  String esConfigurationLabel();

  @Description("Specify Generate Identifier")
  @Constants.DefaultStringValue(
      "{0} new identifier will be generated for this mapping. To customize the identifier format modify the settings below.")
  String specifyGenerateIdentifier();

  @Description("Specify Generate Identifiers")
  @Constants.DefaultStringValue(
      "{0} new identifiers will be generated for this mapping. To customize the identifier format modify the settings below.")
  String specifyGenerateIdentifiers();

  @Description("Generate Identifiers button label")
  @DefaultStringValue("Generate")
  String generateIdentifiersButton();

  @Description("Generate Identifiers size help")
  @DefaultStringValue("Identifier size must be {0} to {1} digits long")
  String generateIdentifiersSizeHelp();

  @Description("Title label")
  @DefaultStringValue("Title")
  String titleLabel();

  @Description("Package label")
  @DefaultStringValue("Package")
  String packageLabel();

  @Description("Version label")
  @DefaultStringValue("Version")
  String versionLabel();

  @Description("R Code label")
  @DefaultStringValue("Code")
  String rCodeLabel();

  @Description("R Version label")
  @DefaultStringValue("R Version")
  String rVersionLabel();

  @Description("R Sessions label")
  @DefaultStringValue("R Sessions")
  String rSessionsLabel();

  @Description("System label")
  @DefaultStringValue("System")
  String systemLabel();

  @Description("N item is selected label")
  @DefaultStringValue("{0} is selected.")
  String NItemSelected();

  @Description("N items are selected label")
  @DefaultStringValue("{0} are selected.")
  String NItemsSelected();

  @Description("Select all N items label")
  @DefaultStringValue("Select all {0}")
  String selectAllNItems();

  @Description("Clear selection label")
  @DefaultStringValue("Clear selection")
  String clearSelection();

  @Description("All N items are selected label")
  @DefaultStringValue("All {0} are selected.")
  String allItemsSelected();

  @Description("Add selected variables to view label")
  @DefaultStringValue("Add selected variables to view")
  String addVariablesToViewTitle();

  @Description("Add selected variable to view label")
  @DefaultStringValue("Add selected variable to view")
  String addVariableToViewTitle();

  @Description("Add variable label")
  @DefaultStringValue("Add Variable")
  String addVariable();

  @Description("Add variables label")
  @DefaultStringValue("Add Variables")
  String addVariables();

  @Description("Add Identifiers Mapping label")
  @DefaultStringValue("Add Identifiers Mapping")
  String addIdentifiersMapping();

  @Description("Original variable label")
  @DefaultStringValue("Original variable")
  String originalVariable();

  @Description("Filter Variable label")
  @Constants.DefaultStringValue("Filter variables...")
  String filterVariables();

  @Description("Filter Table label")
  @Constants.DefaultStringValue("Filter tables...")
  String filterTables();

  @Description("Filter VCFs label")
  @Constants.DefaultStringValue("Filter VCFs...")
  String filterVCFs();

  @Description("Filter Project label")
  @Constants.DefaultStringValue("Filter projects...")
  String filterProjects();

  @Description("Filter Vocabulary label")
  @Constants.DefaultStringValue("Filter vocabularies...")
  String filterVocabularies();

  @Description("Filter Term label")
  @Constants.DefaultStringValue("Filter terms...")
  String filterTerms();

  @Description("Filter DataShield Methods label")
  @Constants.DefaultStringValue("Filter methods and packages...")
  String filterDataShieldMethods();

  @Description("Clear filter label")
  @DefaultStringValue("Clear filter")
  String clearFilter();

  @Description("Page Administration title")
  @DefaultStringValue("Administration")
  String pageAdministrationTitle();

  @Description("Page Dashboard title")
  @DefaultStringValue("Dashboard")
  String pageDashboardTitle();

  @Description("Page Databases title")
  @DefaultStringValue("Databases")
  String pageDatabasesTitle();

  @Description("Page DataSHIELD title")
  @DefaultStringValue("DataSHIELD")
  String pageDataShieldTitle();

  @Description("Page Search Index title")
  @DefaultStringValue("Search")
  String pageSearchIndexTitle();

  @Description("Page R Admin title")
  @DefaultStringValue("R")
  String pageRConfigTitle();

  @Description("Page Identifiers Admin title")
  @DefaultStringValue("Identifiers Mappings")
  String pageIdentifiersMappingTitle();

  @Description("Page Files title")
  @DefaultStringValue("Files")
  String pageFileExplorerTitle();

  @Description("Page Report Template title")
  @DefaultStringValue("Reports")
  String pageReportTemplatePage();

  @Description("Page Jobs title")
  @DefaultStringValue("Tasks")
  String pageJobsTitle();

  @Description("Page Users and Groups title")
  @DefaultStringValue("Users and Groups")
  String pageUsersGroupsTitle();

  @Description("Page Profiles title")
  @DefaultStringValue("Profiles")
  String pageProfilesTitle();

  @Description("Page ID Providers title")
  @DefaultStringValue("Identity Providers")
  String pageIDProvidersTitle();

  @Description("Page Projects title")
  @DefaultStringValue("Projects")
  String pageProjectsTitle();

  @Description("Page Search title")
  @DefaultStringValue("Search")
  String pageSearchTitle();

  @Description("Page Cart title")
  @DefaultStringValue("Cart")
  String pageCartTitle();

  @Description("Page Search Variables title")
  @DefaultStringValue("Variables")
  String pageSearchVariablesTitle();

  @Description("Page Search Entities title")
  @DefaultStringValue("Entities")
  String pageSearchEntitiesTitle();

  @Description("Page Search Entity title")
  @DefaultStringValue("Entity")
  String pageSearchEntityTitle();

  @Description("Page General Configuration title")
  @DefaultStringValue("General Configuration")
  String pageGeneralConfigurationTitle();

  @Description("Page Taxonomies title")
  @DefaultStringValue("Taxonomies")
  String pageTaxonomiesTitle();

  @Description("Quick Search Variables title")
  @DefaultStringValue("Search variables...")
  String quickSearchVariablesTitle();
  @Description("Import Github Taxonomy")
  @DefaultStringValue("Import Github Taxonomy")
  String importGitTaxonomy();

  @Description("Import File Taxonomy")
  @DefaultStringValue("Import File Taxonomy")
  String importFileTaxonomy();
  @Description("Import Maelstrom Taxonomy")
  @DefaultStringValue("Import Maelstrom Research Taxonomies")
  String importMaelstromTaxonomies();

  @Description("Add Taxonomy")
  @DefaultStringValue("Add Taxonomy")
  String addTaxonomy();

  @Description("Edit Taxonomy")
  @DefaultStringValue("Edit Taxonomy")
  String editTaxonomy();

  @Description("Add Vocabulary")
  @DefaultStringValue("Add Vocabulary")
  String addVocabulary();

  @Description("Edit Vocabulary")
  @DefaultStringValue("Edit Vocabulary")
  String editVocabulary();

  @Description("Add Term")
  @DefaultStringValue("Add Term")
  String addTerm();

  @Description("Edit Term")
  @DefaultStringValue("Edit Term")
  String editTerm();

  @Description("Page Plugins title")
  @DefaultStringValue("Plugins")
  String pagePluginsTitle();

  @Description("Page Apps title")
  @DefaultStringValue("Apps")
  String pageAppsTitle();

  @Description("Page Java title")
  @DefaultStringValue("Java Virtual Machine")
  String pageJVMTitle();

  @Description("User Name label")
  @DefaultStringValue("Name")
  String userNameLabel();

  @Description("Realm label")
  @DefaultStringValue("Realm")
  String realmLabel();

  @Description("User Groups label")
  @DefaultStringValue("Groups")
  String userGroupsLabel();

  @Description("User Status label")
  @DefaultStringValue("Enabled")
  String userStatusLabel();

  @Description("User Authentication label")
  @DefaultStringValue("Authentication")
  String userAuthenticationLabel();

  @Description("Group Name label")
  @DefaultStringValue("Name")
  String groupNameLabel();

  @Description("Group Users label")
  @DefaultStringValue("Users")
  String groupUsersLabel();

  @Description("Enabled label")
  @DefaultStringValue("Enabled")
  String enabledLabel();

  @Description("Add user with password label")
  @DefaultStringValue("Add user with password")
  String addUserWithPasswordLabel();

  @Description("Add user with certificate label")
  @DefaultStringValue("Add user with certificate")
  String addUserWithCertificateLabel();

  @Description("Add ID Provider label")
  @DefaultStringValue("Add ID Provider")
  String addIDProvider();

  @Description("Add Project label")
  @DefaultStringValue("Add Project")
  String addProject();

  @Description("All Projects label")
  @DefaultStringValue("All")
  String allProjectsLabel();

  @Description("All Tables label")
  @DefaultStringValue("All tables")
  String allTablesLabel();

  @Description("All Resources label")
  @DefaultStringValue("All resources")
  String allResourcesLabel();

  @Description("Download Modal title")
  @DefaultStringValue("Download File")
  String downloadFileModalTitle();

  @Description("Add Token title")
  @DefaultStringValue("Add Token")
  String addTokenModalTitle();

  @Description("Add DataSHIELD Token title")
  @DefaultStringValue("Add DataSHIELD Token")
  String addDataSHIELDTokenModalTitle();

  @Description("Add R Token title")
  @DefaultStringValue("Add R Token")
  String addRTokenModalTitle();

  @Description("Add SQL Token title")
  @DefaultStringValue("Add SQL Token")
  String addSQLTokenModalTitle();

  @Description("Remove Token title")
  @DefaultStringValue("Remove Token")
  String removeTokenModalTitle();

  @Description("Update Registration Token title")
  @DefaultStringValue("Update Registration Token")
  String updateTokenModalTitle();

  @Description("Add Service Discovery title")
  @DefaultStringValue("Add Service Discovery")
  String addServiceDiscoveryTitle();

  @Description("Add Resource title")
  @DefaultStringValue("Add Resource")
  String addResourceModalTitle();

  @Description("Edit Resource title")
  @DefaultStringValue("Edit Resource")
  String editResourceModalTitle();

  @Description("View Resource title")
  @DefaultStringValue("View Resource")
  String viewResourceModalTitle();

  @Description("Export VCF Modal title")
  @DefaultStringValue("Export VCF Files")
  String exportVcfModalTitle();

  @Description("Import VCF Modal title")
  @DefaultStringValue("Import VCF File")
  String importVcfModalTitle();

  @Description("Rename Modal title")
  @DefaultStringValue("Rename File")
  String renameFileModalTitle();

  @Description("Create Folder Modal title")
  @DefaultStringValue("Create Folder")
  String createFolderModalTitle();

  @Description("Unzip Modal title")
  @DefaultStringValue("Extract Archive")
  String unzipModalTitle();

  @Description("Upload File Modal title")
  @DefaultStringValue("Upload File")
  String uploadFileModalTitle();

  @Description("Change Password Modal title")
  @DefaultStringValue("Change Password")
  String changePasswordModalTitle();

  @Description("Entity Details Modal title")
  @DefaultStringValue("Entity Details")
  String entityDetailsModalTitle();

  @Description("Vcs Commit History Modal title")
  @DefaultStringValue("Commit Details")
  String vcsCommitHistoryModalTitle();

  @Description("Update Resource Permissions Modal title")
  @DefaultStringValue("Edit Permission")
  String updateResourcePermissionsModalTile();

  @Description("Remove All Subject Permissions Modal title")
  @DefaultStringValue("Remove Permissions")
  String removeAllSubjectPermissionsModalTile();

  @Description("Remove All Subject Permissions message label")
  @DefaultStringValue("All permissions of {0} '{1}' will be permanently removed, are you sure you want to continue?")
  String removeAllSubjectPermissionsModalMessage();

  @Description("Vcs Commit History Empty")
  @DefaultStringValue("No comment history available")
  String noVcsCommitHistoryAvailable();

  @Description("VCS Commit Info Labels")
  @DefaultStringMapValue({ "id", "ID",
      "Author", "Author",
      "Date", "Date",
      "Comment", "Comment" })
  Map<String, String> commitInfoMap();

  @Description("Script Update Default Prefix Label")
  @DefaultStringValue("Update")
  String scriptUpdateDefaultPrefixLabel();

  @Description("Default label")
  @DefaultStringValue("default")
  String defaultLabel();

  @Description("Preview label")
  @DefaultStringValue("Preview")
  String previewLabel();

  @Description("Edit label")
  @DefaultStringValue("Edit")
  String editLabel();

  @Description("Save label")
  @DefaultStringValue("Save")
  String saveLabel();

  @Description("History label")
  @DefaultStringValue("History")
  String historyLabel();

  @Description("Server running title")
  @DefaultStringValue("Server has been running for {0}.")
  String serverRunningFor();

  @Description("")
  @DefaultStringValue("The project's datasource is loading, Read and Write actions are temporarily unavailable. Try again later.")
  String projectLoadingText();

  @Description("Project Datasource Status Descriptions")
  @DefaultStringMapValue({ "READY", "Ready",
      "BUSY", "Busy: database has write commands that are pending or being processed.",
      "LOADING", "Loading: the database is being loaded, tables operations are not accessible yet.",
      "NONE", "No associated database, data cannot be imported, only views can be defined." })
  Map<String, String> datasourceStatusDescriptionsMap();

  @Description("JVM Labels")
  @DefaultStringMapValue({ "OPAL_VERSION", "Opal Version",
      "JAVA_VERSION", "Java Version",
      "VM_NAME", "VM Name",
      "VM_VENDOR", "VM Vendor",
      "VM_VERSION", "VM Version",
      "MEM_HEAP", "Memory Heap",
      "MEGABYTES", "MegaBytes (Mb)",
      "COMMITTED", "Committed",
      "USED", "Used",
      "MEM_NON_HEAP", "Memory Non-Heap",
      "THREADS", "Threads",
      "COUNT", "Count",
      "PEAK", "Peak",
      "CURRENT", "Current",
      "GC_DELTA", "Garbage Collectors (delta)",
      "DELTA", "Delta",
      "TIME_MS", "Time (ms)",
      "GC_COUNT", "GC Count"
  })
  Map<String, String> jvmMap();

  @Description("Select languages label")
  @DefaultStringValue("Select languages...")
  String selectLanguages();

  @Description("Locale Labels")
  @DefaultStringMapValue({
      "ar", "Arabic",
      "be", "Belarusian",
      "bg", "Bulgarian",
      "ca", "Catalan",
      "cs", "Czech",
      "da", "Danish",
      "de", "German",
      "el", "Greek",
      "en", "English",
      "es", "Spanish",
      "et", "Estonian",
      "fi", "Finnish",
      "fr", "French",
      "ga", "Irish",
      "hi", "Hindi",
      "hr", "Croatian",
      "hu", "Hungarian",
      "in", "Indonesian",
      "is", "Icelandic",
      "it", "Italian",
      "iw", "Hebrew",
      "ja", "Japanese",
      "ko", "Korean",
      "lt", "Lithuanian",
      "lv", "Latvian",
      "mk", "Macedonian",
      "ms", "Malay",
      "mt", "Maltese",
      "nl", "Dutch",
      "no", "Norwegian",
      "pl", "Polish",
      "pt", "Portuguese",
      "ro", "Romanian",
      "ru", "Russian",
      "sk", "Slovak",
      "sl", "Slovenian",
      "sq", "Albanian",
      "sr", "Serbian",
      "sv", "Swedish",
      "th", "Thai",
      "tr", "Turkish",
      "uk", "Ukrainian",
      "vi", "Vietnamese",
      "zh", "Chinese"
  })
  Map<String, String> localeMap();

  @Description("Table References label")
  @DefaultStringValue("Table References")
  String tableReferencesLabel();

  @Description("Resource Reference label")
  @DefaultStringValue("Resource Reference")
  String resourceReferenceLabel();

  @Description("Edit categories label")
  @DefaultStringValue("Edit Categories")
  String editCategories();

  @Description("Edit properties label")
  @DefaultStringValue("Edit Properties")
  String editProperties();

  @Description("Edit entities filter label")
  @DefaultStringValue("Edit Entities Filter")
  String editEntitiesFilter();

  @Description("Edit Identifiers Mapping label")
  @DefaultStringValue("Edit Identifiers Mapping")
  String editIdentifiersMapping();

  @Description("Report templates header")
  @DefaultStringValue("{0} Reports")
  String reportTemplatesHeader();

  @Description("Import label")
  @DefaultStringValue("Import")
  String importLabel();

  @Description("Storage label")
  @DefaultStringValue("Storage")
  String storageLabel();

  @Description("Export label")
  @DefaultStringValue("Export")
  String exportLabel();

  @Description("Opal SQL label")
  @DefaultStringValue("Opal SQL")
  String hibernateDatasourceLabel();

  @Description("Tabular SQL label")
  @DefaultStringValue("Tabular SQL Schema Options")
  String jdbcOptionsLabel();

  @Description("Tabular SQL label")
  @DefaultStringValue("Tabular SQL")
  String jdbcDatasourceLabel();

  @Description("Update entities filter label")
  @DefaultStringValue("Update entities filter")
  String updateWhereComment();

  @Description("Update {0} label")
  @DefaultStringValue("Update {0}")
  String updateComment();

  @Description("Rename {0} to {1} label")
  @DefaultStringValue("Rename {0} to {1}")
  String renameToComment();

  @Description("Update {0} properties label")
  @DefaultStringValue("Update {0} properties")
  String updateVariableProperties();

  @Description("Create variable {0} label")
  @DefaultStringValue("Create variable {0}")
  String createVariable();

  @Description("Moment with ago label")
  @DefaultStringValue("{0} ({1})")
  String momentWithAgo();

  @Description("Required label")
  @DefaultStringValue("Required")
  String required();

  @Description("Authentication Failed message")
  @DefaultStringValue("Authentication failed")
  String authFailed();

  @Description("Password Changed message")
  @DefaultStringValue("Your password has been changed.")
  String passwordChanged();

  @Description("Default storage label")
  @DefaultStringValue("Default storage")
  String defaultStorage();

  @Description("Select database label")
  @DefaultStringValue("Select database...")
  String selectDatabase();

  @Description("None label")
  @DefaultStringValue("None")
  String none();

  @Description("Any label")
  @DefaultStringValue("Any")
  String any();

  @Description("Project Database Name label")
  @DefaultStringValue("Project tables (dictionaries and data) are stored in the database:")
  String projectDatabaseName();

  @Description("Genotype Edit Mapping Modal title")
  @DefaultStringValue("Edit Mapping")
  String projectGenotypeEditMappingeModalTitle();

  @Description("Default Character Set label")
  @DefaultStringValue("Default Character Set")
  String defaultCharsetLabel();

  @Description("Public URL label")
  @DefaultStringValue("Public URL")
  String publicUrl();

  @Description("Terms label")
  @DefaultStringValue("Terms")
  String termsLabel();

  @Description("Description label")
  @DefaultStringValue("Description")
  String descriptionLabel();

  @Description("Name Keywords label")
  @DefaultStringValue("Name (keywords)")
  String nameKeywordsLabel();

  @Description("Details label")
  @DefaultStringValue("Details")
  String details();

  @Description("Collapsible Headings")
  @DefaultStringMapValue({ "ADVANCED_OPTIONS", "Advanced Options",
      "DETAILS", "Details" })
  Map<String, String> collapsibleMap();

  @Description("Display Options label")
  @DefaultStringValue("Display Options")
  String displayOptionsLabel();

  @Description("Add a value mapping label")
  @DefaultStringValue("Add a value mapping")
  String addValueMapping();

  @Description("All tables are selected for being exported label")
  @DefaultStringValue("All tables are selected for being exported.")
  String exportAllTables();

  @Description("All VCF files are selected for being exported label")
  @DefaultStringValue("All VCF files are selected for being exported.")
  String exportAllVCFs();

  @Description("All tables are selected for being copied label")
  @DefaultStringValue("All tables are selected for being copied.")
  String copyAllTables();

  @Description("Opal identifiers label")
  @DefaultStringValue("[None]")
  String opalDefaultIdentifiersLabel();

  @Description("Criterion Filters Labels")
  @DefaultStringMapValue({
      "all", "All",
      "any", "Any",
      "empty", "Empty",
      "not_empty", "Not empty",
      "min", "Min",
      "max", "Max",
      "values", "Values",
      "matches", "Matches",
      "select_operator", "Select an operator...",
      "in", "In",
      "not_in", "Not in",
      "none", "None",
      "select", "Select",
      "range", "Range",
      "values", "Values",
      "custom_match_query", "Custom match query",
      "like", "Like",
      "not_like", "Not like",
      "wildcards_help", "Use the * wildcard character<br/>to replace one or more characters.",
      "from", "From",
      "to", "To",
      "date", "Date",
      "select", "Select...",
      "entity_identifier", "Entity identifier",
      "filter", "Filter..."
  })
  Map<String, String> criterionFiltersMap();

  @Description("Paste certificate label")
  @DefaultStringValue("(Paste certificate)")
  String pasteCertificate();

  @Description("Remove Group label")
  @DefaultStringValue("Remove Group")
  String removeGroup();

  @Description("Remove User label")
  @DefaultStringValue("Remove User")
  String removeUser();

  @Description("Remove User Profile label")
  @DefaultStringValue("Remove User Profile")
  String removeUserProfile();

  @Description("Remove User Profiles label")
  @DefaultStringValue("Remove User Profiles")
  String removeUserProfiles();

  @Description("Remove ID Provider label")
  @DefaultStringValue("Remove ID Provider")
  String removeIDProvider();

  @Description("Remove R Package label")
  @DefaultStringValue("Remove R Package")
  String removeRPackage();

  @Description("Update R Packages label")
  @DefaultStringValue("Update R Packages")
  String updateRPackages();

  @Description("Remove App label")
  @DefaultStringValue("Remove Application")
  String removeApp();

  @Description("Remove Rock Config label")
  @DefaultStringValue("Remove Rock Discovery")
  String removeRockConfig();

  @Description("Create Key Pair label")
  @DefaultStringValue("Create Key Pair")
  String createKeyPairLabel();

  @Description("AuthenticationType Labels")
  @DefaultStringMapValue({ "PASSWORD", "Password", "CERTIFICATE", "Certificate" })
  Map<String, String> authenticationTypeMap();

  @Description("Total label")
  @DefaultStringValue("Total")
  String totalLabel();

  @Description("No results found label")
  @DefaultStringValue("No results found")
  String noResultsFound();

  @Description("Min label")
  @DefaultStringValue("Min")
  String minLabel();

  @Description("Max label")
  @DefaultStringValue("Max")
  String maxLabel();

  @Description("Mean label")
  @DefaultStringValue("Mean")
  String meanLabel();

  @Description("Geometric Mean label")
  @DefaultStringValue("Geometric Mean")
  String geometricMeanLabel();

  @Description("Standard deviation) label")
  @DefaultStringValue("Standard deviation")
  String standardDeviationLabel();

  @Description("N label")
  @DefaultStringValue("N")
  String NLabel();

  @Description("Select categorical variable label")
  @DefaultStringValue("Select categorical variable...")
  String selectCategoricalVariableLabel();

  @Description("Select another variable label")
  @DefaultStringValue("Select another variable...")
  String selectAnotherVariableLabel();

  @Description("Variable Import Failed Error message")
  @DefaultStringValue("Failed to import dictionary. Please make sure the file contains valid data.")
  String variableImportFailed();

  @Description("Full/normal screen tooltip")
  @DefaultStringValue("Full/normal screen")
  String switchScreenDisplay();

  @Description("Descriptive statistics label")
  @DefaultStringValue("Descriptive statistics")
  String descriptiveStatistics();

  @Description("Mode label")
  @DefaultStringValue("Mode")
  String mode();

  @Description("Histogram")
  @DefaultStringValue("Histogram")
  String histogram();

  @Description("Density")
  @DefaultStringValue("Density")
  String density();

  @Description("Normal Probability")
  @DefaultStringValue("Normal Probability")
  String normalProbability();

  @Description("Sample Quantiles")
  @DefaultStringValue("Sample Quantiles")
  String sampleQuantiles();

  @Description("Theroretical Quantiles")
  @DefaultStringValue("Theroretical Quantiles")
  String theoreticalQuantiles();

  @Description("Sum of squares")
  @DefaultStringValue("Sum of squares")
  String sumOfSquares();

  @Description("Sum")
  @DefaultStringValue("Sum")
  String sum();

  @Description("Kurtosis")
  @DefaultStringValue("Kurtosis")
  String kurtosis();

  @Description("Skewness")
  @DefaultStringValue("Skewness")
  String skewness();

  @Description("Variance")
  @DefaultStringValue("Variance")
  String variance();

  @Description("Median")
  @DefaultStringValue("Median")
  String median();

  @Description("Max")
  @DefaultStringValue("Max")
  String max();

  @Description("Min")
  @DefaultStringValue("Min")
  String min();

  @Description("Not null statistics label")
  @DefaultStringValue("Not null")
  String notNullStatistics();

  @Description("Select a derivation method message")
  @DefaultStringValue("Select a derivation method")
  String selectDerivationMethod();

  @Description("Click to remove this item from your bookmarks label")
  @DefaultStringValue("Click to remove this item from your bookmarks")
  String clickToRemoveFromBookmarks();

  @Description("Click to add this item to your bookmarks label")
  @DefaultStringValue("Click to add this item to your bookmarks")
  String clickToAddToBookmarks();

  @Description("Failed to start search service: {0} label")
  @DefaultStringValue("Failed to start search service: {0}")
  String searchSettingsError();

  @Description("Variable not found label")
  @DefaultStringValue("Variable not found: {0}")
  String variableNotFound();

  @Description("Variable not categorical label")
  @DefaultStringValue("Variable is not categorical: {0}")
  String variableNotCategorical();

  @Description("Variable not categorical nor continuous label")
  @DefaultStringValue("Variable is not categorical nor continuous: {0}")
  String variableNotCategoricalNorContinuous();

  @Description("Register label")
  @DefaultStringValue("Register")
  String register();

  @Description("Set Key pair label")
  @DefaultStringValue("Set Key Pair")
  String setKeyPair();

  @Description("Import Identifiers label")
  @DefaultStringValue("Import Identifiers")
  String importIdentifiers();

  @Description("Add User label")
  @DefaultStringValue("Add User")
  String addUser();

  @Description("Data acess label")
  @DefaultStringValue("Data access")
  String dataAccessLabel();

  @Description("Tasks label")
  @DefaultStringValue("Tasks")
  String tasksLabel();

  @Description("Administration label")
  @DefaultStringValue("Administration")
  String administrationLabel();

  @Description("Services label")
  @DefaultStringValue("Services")
  String servicesLabel();

  @Description("Derive label")
  @DefaultStringValue("Derive")
  String derive();

  @Description("Explore Variables label")
  @DefaultStringValue("Explore Variables")
  String exploreVariables();

  @Description("Manage Files label")
  @DefaultStringValue("Manage Files")
  String manageFiles();

  @Description("Run Reports label")
  @DefaultStringValue("Run Reports")
  String runReports();

  @Description("Manage Participant Identifiers label")
  @DefaultStringValue("Manage Participant Identifiers")
  String manageParticipantIdentifiers();

  @Description("My Profile label")
  @DefaultStringValue("My Profile")
  String myProfile();

  @Description("More label")
  @DefaultStringValue("more ...")
  String moreLabel();

  @Description("RSA label")
  @DefaultStringValue("RSA")
  String rsa();

  @Description("Add Key label")
  @DefaultStringValue("Add Key")
  String addKeyLabel();

  @Description("Add Permission label")
  @DefaultStringValue("Add Permission")
  String addPermission();

  @Description("At least one storage database is required for importing data label")
  @DefaultStringValue("At least one storage database is required for importing data.")
  String dataDatabaseRequiredLabel();

  @Description("This database definition is required for importing data label")
  @DefaultStringValue("This database definition is required for importing data.")
  String identifiersDatabaseRequiredLabel();

  @Description("Add Table label")
  @DefaultStringValue("Add Table")
  String addTable();

  @Description("No items label")
  @DefaultStringValue("No items.")
  String noItems();

  @Description("System Error label")
  @DefaultStringValue("System Error")
  String systemErrorLablel();

  @Description("Non-missing label")
  @DefaultStringValue("Non-Missing")
  String nonMissing();

  @Description("Non-missing top N label")
  @DefaultStringValue("Non-Missing (top {0})")
  String nonMissingTopN();

  @Description("Total Non Missings label")
  @DefaultStringValue("Subtotal")
  String subtotal();

  @Description("Not Empty label")
  @DefaultStringValue("Not Empty")
  String notEmpty();

  @Description("N/A label")
  @DefaultStringValue("N/A")
  String naLabel();

  @Description("Approx. Area (Km2) label")
  @DefaultStringValue("Approximate Area (Km2)")
  String approxArea();

  @Description("Variable Template Download Panel label")
  @DefaultStringValue("Use the following Excel template to add new variables or update existing ones:")
  String variablesTemplateDownloadLabel();

  @Description("Variable Template Download Button label")
  @DefaultStringValue("Download Opal Variables Template")
  String variablesTemplateButtonLabel();

  @Description("No Filter comment")
  @DefaultStringValue("no filter")
  String noFilter();

  @Description("No Tables for EntityType label")
  @DefaultStringValue("No tables for this entity type.")
  String noTablesforEntityType();

  @Description("Launch garbage collector title")
  @DefaultStringValue("Launch garbage collector")
  String launchGarbageCollectorTitle();

  @Description("Too many repeated errors label")
  @DefaultStringValue("Too many repeated errors. Stopping further requests")
  String tooManyRepeatedErrorsLabel();

  @Description("Genotypes VCF file label")
  @DefaultStringValue("VCF File")
  String vcfFileLabel();

  @Description("Browse button label")
  @DefaultStringValue("Browse")
  String browseButtonLabel();

  @Description("Genotypes Identified Samples count label")
  @DefaultStringValue("Identified Samples")
  String vcfIdentifiedSamplesCountLabel();

  @Description("Genotypes Participants count label")
  @DefaultStringValue("Participants")
  String vcfParticipantsCountLabel();

  @Description("Genotypes Samples count label")
  @DefaultStringValue("Samples")
  String vcfSamplesCountLabel();

  @Description("Genotypes Genotypes count label")
  @DefaultStringValue("Genotypes")
  String vcfGenotypesCountLabel();

  @Description("Genotypes Variants count label")
  @DefaultStringValue("Variants")
  String vcfVariantsCountLabel();

  @Description("Genotypes Controls count label")
  @DefaultStringValue("Controls")
  String vcfControlsCountLabel();

  @Description("Genotypes selection items default tip message for export or remove")
  @DefaultStringValue("Select VCF files to export or remove.")
  String vcfItemTipsAlertMessageExportOrRemove();

  @Description("Genotypes selection items default tip message for export only")
  @DefaultStringValue("Select VCF files to export.")
  String vcfItemTipsAlertMessageExport();

  @Description("Sort map")
  @DefaultStringMapValue({ "_score:desc", "Relevance",
      "name:asc", "Name (asc)",
      "name:desc", "Name (desc)" })
  Map<String, String> sortOrderMap();

  @Description("Sign in with")
  @DefaultStringValue("Sign in with")
  String signInWith();

  @Description("User account Label")
  @DefaultStringValue("User account")
  String userAccountLabel();

  @Description("Token Label")
  @DefaultStringValue("Personal access token")
  String accessTokenLabel();

  @Description("Credentials Label")
  @DefaultStringValue("User credentials")
  String credentialsLabel();

}
