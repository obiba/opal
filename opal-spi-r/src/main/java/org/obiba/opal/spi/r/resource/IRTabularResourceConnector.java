/*
 * Copyright (c) 2022 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.spi.r.resource;

import org.obiba.opal.spi.r.RServerResult;
import org.obiba.opal.spi.resource.TabularResourceConnector;

public interface IRTabularResourceConnector extends TabularResourceConnector {

  RServerResult execute(String script);

}
