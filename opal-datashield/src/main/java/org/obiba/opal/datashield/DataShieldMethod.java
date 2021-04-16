/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.datashield;

import org.obiba.datashield.core.DSMethod;
import org.obiba.datashield.core.DSMethodType;
import org.obiba.opal.spi.r.ROperation;

/**
 * Interface to be implemented by methods that can be executed through datashield
 */
public interface DataShieldMethod extends DSMethod {

  ROperation assign(DSMethodType env);

}
