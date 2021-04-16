/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.support;


public class ValueRenderingHelper {

  private static final long KB = 1024l;

  private static final long MB = KB * KB;

  private static final long GB = MB * KB;

  private static final long K = 1000l;

  private static final long M = K * K;

  private static final long G = M * K;

  private ValueRenderingHelper() {
  }

  public static String getSizeInBytes(double size) {
    return getSizeWithUnit(size, KB, MB, GB, "B");
  }

  public static String getSize(double size) {
    return getSizeWithUnit(size, K, M, G, "");
  }

  private static String getSizeWithUnit(double size, long k, long m, long g, String unit) {
    if(size < k) {
      return (long) size + " " + unit;
    }
    if(size < m) {
      double sizeInK = size / k;
      long iPart = (long) sizeInK;
      long fPart = Math.round((sizeInK - iPart) * 10);
      return iPart + "." + fPart + " K" + unit;
    }
    if(size < g) {
      double sizeInM = size / m;
      long iPart = (long) sizeInM;
      long fPart = Math.round((sizeInM - iPart) * 10);
      return iPart + "." + fPart + " M" + unit;
    }
    double sizeInG = size / g;
    long iPart = (long) sizeInG;
    long fPart = Math.round((sizeInG - iPart) * 10);
    return iPart + "." + fPart + " G" + unit;
  }

}
