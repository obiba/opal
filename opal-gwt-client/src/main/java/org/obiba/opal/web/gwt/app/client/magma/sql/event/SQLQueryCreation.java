/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.magma.sql.event;

import com.gwtplatform.dispatch.annotation.GenEvent;

/**
 * Request for creating a new SQL query.
 */
@GenEvent
public class SQLQueryCreation {

  String project;

  String query;

}
