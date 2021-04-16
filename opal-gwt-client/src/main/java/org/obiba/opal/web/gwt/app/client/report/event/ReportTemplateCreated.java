/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.report.event;

import org.obiba.opal.web.model.client.opal.ReportTemplateDto;

import com.gwtplatform.dispatch.annotation.GenEvent;

@GenEvent
public class ReportTemplateCreated {

  ReportTemplateDto reportTemplate;

}
