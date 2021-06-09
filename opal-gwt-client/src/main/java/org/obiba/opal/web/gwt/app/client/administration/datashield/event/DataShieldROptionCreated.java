/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.administration.datashield.event;


import com.gwtplatform.dispatch.annotation.GenEvent;
import org.obiba.opal.web.model.client.datashield.DataShieldROptionDto;

@GenEvent
public class DataShieldROptionCreated {

  String profile;

  DataShieldROptionDto optionDto;

}
