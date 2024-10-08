/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.core.service.security;

import java.io.InputStream;
import java.io.OutputStream;

public interface CryptoService {

  String generateSecretKey();

  String encrypt(String plain);

  String decrypt(String encrypted);

  InputStream newCipherInputStream(InputStream in);

  OutputStream newCipherOutputStream(OutputStream out);

}
