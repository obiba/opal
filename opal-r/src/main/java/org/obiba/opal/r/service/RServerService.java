/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.r.service;

import org.obiba.opal.spi.r.ROperation;

public interface RServerService {

  void start();

  void stop();

  boolean isRunning();

  RServerState getState();

  RServerSession newRServerSession(String user);

  void execute(ROperation rop);
}
