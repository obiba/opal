/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.tools.hasher;

import org.apache.shiro.authc.credential.DefaultPasswordService;
import org.apache.shiro.crypto.SecureRandomNumberGenerator;
import org.apache.shiro.crypto.hash.Hash;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.crypto.hash.format.DefaultHashFormatFactory;
import org.apache.shiro.crypto.hash.format.HashFormat;
import org.apache.shiro.crypto.hash.format.HashFormatFactory;
import org.apache.shiro.crypto.hash.format.Shiro1CryptFormat;
import org.apache.shiro.util.ByteSource;

/**
 * Inspired from org.apache.shiro.tools.hasher.Hasher and used by Debian while installing Opal.
 */
@SuppressWarnings("UseOfSystemOutOrSystemErr")
public final class Hasher {

  private static final String DEFAULT_PASSWORD_ALGORITHM_NAME = DefaultPasswordService.DEFAULT_HASH_ALGORITHM;

  private static final int DEFAULT_GENERATED_SALT_SIZE = 128;

  private static final int DEFAULT_PASSWORD_NUM_ITERATIONS = DefaultPasswordService.DEFAULT_HASH_ITERATIONS;

  private static final HashFormatFactory HASH_FORMAT_FACTORY = new DefaultHashFormatFactory();

  private Hasher() {}

  public static void main(String... args) {
    if(args == null || args.length != 1) {
      printUsage();
      return;
    }
    try {
      System.out.println(hash(args[0]));
    } catch(Exception e) {
      printException(e);
      System.exit(-1);
    }
  }

  public static String hash(String value) {
    Hash hash = new SimpleHash(DEFAULT_PASSWORD_ALGORITHM_NAME, value, getSalt(), DEFAULT_PASSWORD_NUM_ITERATIONS);
    HashFormat format = HASH_FORMAT_FACTORY.getInstance(Shiro1CryptFormat.class.getName());
    return format.format(hash);
  }

  private static ByteSource getSalt() {
    int byteSize = DEFAULT_GENERATED_SALT_SIZE / 8; //generatedSaltSize is in *bits* - convert to byte size:
    return new SecureRandomNumberGenerator().nextBytes(byteSize);
  }

  private static void printException(Exception e) {
    System.out.println();
    System.out.println("Error: ");
    e.printStackTrace(System.out);
    System.out.println(e.getMessage());
  }

  private static void printUsage() {
    System.out.println("Usage: java -jar password-hasher-<version>.jar <value>");
    System.out.println("\nPrint a cryptographic hash (aka message digest) of the specified <value>.");
  }

}
