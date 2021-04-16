/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.core.service;

public class NoSuchVCFSamplesMappingException extends RuntimeException {

  private static final long serialVersionUID = -6357540199499515674L;

  private final String name;

  public NoSuchVCFSamplesMappingException(String vcfSamplesMappingName) {
    super("No VCF Samples mapping exists with the specified name '" + vcfSamplesMappingName + "'");
    name = vcfSamplesMappingName;
  }

  public String getName() {
    return name;
  }
}
