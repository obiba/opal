/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
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

  private ValueRenderingHelper() {
  }

  public static String getSizeWithUnit(double size) {
    if(size < KB) {
      return (long) size + " B";
    }
    if(size < MB) {
      double sizeInKB = size / KB;
      long iPart = (long) sizeInKB;
      long fPart = Math.round((sizeInKB - iPart) * 10);
      return iPart + "." + fPart + " KB";
    }
    if(size < GB) {
      double sizeInMB = size / MB;
      long iPart = (long) sizeInMB;
      long fPart = Math.round((sizeInMB - iPart) * 10);
      return iPart + "." + fPart + " MB";
    }
    double sizeInGB = size / GB;
    long iPart = (long) sizeInGB;
    long fPart = Math.round((sizeInGB - iPart) * 10);
    return iPart + "." + fPart + " GB";
  }

}
