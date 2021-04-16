/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.spi.vcf;

import java.io.File;
import java.io.IOException;
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
   * The sample IDs of all the VCFs.
   *
   * @return
   */
  Collection<String> getSampleIds();

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
   * Store a VCF with an associated file name: the file name suffix specifies if it is a compressed VCF file.
   * Subsequent requests will refer to the base name of the provided file name. Example: written file "file_example.vcf.gz" will
   * be referred as "file_example".
   *
   * @param name
   * @param vcf
   */
  void writeVCF(String name, InputStream vcf) throws IOException;

  /**
   * Delete the VCF associated to the name.
   *
   * @param name
   */
  void deleteVCF(String name);

  /**
   * Read the VCF stored with the given name. The stream is a compressed VCF file.
   *
   * @param name
   * @param out The stream to write to.
   * @return
   */
  void readVCF(String name, OutputStream out) throws NoSuchElementException, IOException;

  /**
   * Read the VCF stored with the given name. The stream is a compressed VCF file.
   *
   * @param name
   * @param format
   * @param out The stream to write to.
   * @return
   */
  void readVCF(String name, Format format, OutputStream out) throws NoSuchElementException, IOException;

  /**
   * Read the VCF stored with the given name, with a subset applied to the provided samples. The returned stream is a compressed VCF file.
   *
   * @param name
   * @param out The stream to write to.
   * @param samples The sample IDs.
   * @return
   */
  void readVCF(String name, OutputStream out, Collection<String> samples) throws NoSuchElementException, IOException;


  /**
   * Read the VCF stored with the given name, with a subset applied to the provided samples. The returned stream is a compressed VCF file.
   *
   * @param name
   * @param format
   * @param out The stream to write to.
   * @param samples The sample IDs.
   * @return
   */
  void readVCF(String name, Format format, OutputStream out, Collection<String> samples) throws NoSuchElementException, IOException;


  /**
   * Read the VCF statistics stored with the given name. The stream is a plain text file.
   *
   * @param name
   * @param out The stream to write to.
   * @return
   */
  void readVCFStatistics(String name, OutputStream out) throws NoSuchElementException, IOException;

  /**
   * VCF file format flavor.
   */
  enum Format {
    VCF, BCF
  }

  /**
   * Some metrics about a VCF file.
   */
  interface VCFSummary {

    /**
     * Get the associated VCF name.
     *
     * @return
     */
    String getName();

    /**
     * Get the VCF format flavour: VCF or BCF
     * @return
     */
    Format getFormat();

    /**
     * The sample IDs.
     *
     * @return
     */
    Collection<String> getSampleIds();

    /**
     * Number of variants.
     *
     * @return
     */
    int getVariantsCount();

    /**
     * Number of genotypes.
     *
     * @return
     */
    int getGenotypesCount();

    /**
     * Compressed file equivalent approximate size.
     *
     * @return
     */
    long size();

  }

}
