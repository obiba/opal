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
  @DefaultStringMapValue( { "NOT_STARTED", "Not Started", "IN_PROGRESS", "In Progress", "SUCCEEDED", "Succeeded", "FAILED", "Failed", "CANCEL_PENDING", "Cancel Pending", "CANCELED", "Cancelled" })
  Map<String, String> statusMap();

  @Description("Actions label")
  @DefaultStringValue("Actions")
  String actionsLabel();

  @Description("Action map")
  @DefaultStringMapValue( { "Log", "Log", "Cancel", "Cancel", "Delete", "Delete" })
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

  @Description("Unit label")
  @DefaultStringValue("Unit")
  String unitLabel();

  @Description("User message map")
  @DefaultStringMapValue( { "jobCancelled", "Job cancelled.", "jobDeleted", "Job deleted.", "completedJobsDeleted", "All completed jobs deleted.", "SetCommandStatus_NotFound", "Job could not be cancelled (not found).", "SetCommandStatus_BadRequest_IllegalStatus", "Job status cannot be set to the specified value.", "SetCommandStatus_BadRequest_NotCancellable", "Job has completed and has already been cancelled.", "DeleteCommand_NotFound", "Job could not be deleted (not found).", "DeleteCommand_BadRequest_NotDeletable", "Job is currently running and therefore cannot be deleted at this time." })
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
  @DefaultStringValue("Dataset")
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
  @DefaultStringMapValue( { "clearJobsList", "Clear Jobs List", "cancelJob", "Cancel Job", "replaceExistingFile", "Replace File" })
  Map<String, String> confirmationTitleMap();

  @Description("Confirmation message map")
  @DefaultStringMapValue( { "confirmClearJobsList", "All the completed jobs (succeeded, failed or cancelled) will be removed from the jobs list. Currently running jobs will be unaffected.<br /><br />Please confirm that you want to clear the jobs list.", "confirmCancelJob", "The job will be cancelled. Changes will be rolled back as much as possible: although cancelled, a job might be partially completed.<br /><br />Please confirm that you want cancel this job.", "confirmReplaceExistingFile", "The file that you are uploading already exist in the file system.<br /><br />Please confirm that you want to replace the existing file." })
  Map<String, String> confirmationMessageMap();
}
