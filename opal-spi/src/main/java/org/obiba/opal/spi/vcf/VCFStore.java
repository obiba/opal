/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.spi.vcf;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.NoSuchElementException;

/**
 * A vcf store gives access to the VCF files collection and associated summary and operations.
 */
public interface VCFStore {

  /**
   * The store has a unique name.
   *
   * @return
   */
  String getName();

  /**
   * Get the names of the VCF that are stored.
   *
   * @return
   */
  Collection<String> getVCFNames();

  /**
   * Check if a VCF with the given name is stored.
   *
   * @param name
   * @return
   */
  boolean hasVCF(String name);

  /**
   * Get some metrics about a VCF.
   *
   * @param name
   * @return
   * @throws NoSuchElementException
   */
  VCFSummary getVCFSummary(String name) throws NoSuchElementException;

  /**
   * Store a VCF with an associated name. The underlying VCF file is expected to be compressed.
   *
   * @param name
   * @param vcf
   */
  void writeVCF(String name, InputStream vcf);

  /**
   * Delete the VCF associated to the name.
   *
   * @param name
   */
  void deleteVCF(String name);

  /**
   * Read the VCF stored with the given name.
   *
   * @param name
   * @return
   */
  OutputStream readVCF(String name) throws NoSuchElementException;

  /**
   * Some metrics about the VCF.
   */
  interface VCFSummary {

    /**
     * Get the associated VCF name.
     *
     * @return
     */
    String getName();

    /**
     * The sample IDs.
     *
     * @return
     */
    Collection<String> sampleIds();

    /**
     * Number of variants.
     *
     * @return
     */
    int variantsCount();

    /**
     * Number of genotypes.
     *
     * @return
     */
    int genotypesCount();

    /**
     * Compressed file equivalent approximate size.
     *
     * @return
     */
    long size();

  }

}
