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

/**
 * Programmatically available localised text strings. This interface will be bound to localised properties files found
 * in the {@code com.google.gwt.i18n.client} package.
 */
public interface Translations extends Constants {

  String errorDialogTitle();

  String warningDialogTitle();

  String infoDialogTitle();

  String nameLabel();

  String valueTypeLabel();

  String labelLabel();

  String idLabel();

  String typeLabel();

  String userLabel();

  String startLabel();

  String endLabel();

  String statusLabel();

  Map<String, String> statusMap();

  String actionsLabel();

  String sizeLabel();

  String lastModifiedLabel();

  String dateLabel();

  String messageLabel();

  String jobLabel();

  String jobsLabel();

  Map<String, String> actionMap();

  String fileSystemLabel();

  String entityTypeLabel();

  String variablesLabel();

  String unitLabel();

  Map<String, String> userMessageMap();

  String fileMustBeSelected();

  String yesLabel();

  String noLabel();

  String missingLabel();

  String categoriesLabel();

  String attributesLabel();

  String languageLabel();

  String valueLabel();

  String codeLabel();
}
