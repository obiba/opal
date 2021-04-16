/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.spi.analysis.support.generator;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

public final class IdGeneratorFactory {

  public static IdGenerator createDateIdGenerator() {
    return () -> new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()) + "-" + new Random().nextInt();
  }

  public static IdGenerator createUUIDGenerator() {
    return () -> UUID.randomUUID().toString();
  }

}
