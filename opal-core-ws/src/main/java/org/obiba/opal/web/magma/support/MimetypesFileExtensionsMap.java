/*******************************************************************************
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *  
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *  
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.magma.support;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Iterator;

import javax.activation.MimetypesFileTypeMap;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

/**
 * This class allows reverse mapping from a mime-type to a collection of file extensions. the first file extension is
 * considered to be the preferred one.
 * 
 * <p>
 * <b>MIME types file search order:</b>
 * </p>
 * 
 * <ol>
 * <li>The file or resources named META-INF/mime.types.</li>
 * <li>The file or resource named META-INF/mimetypes.default (usually found only in the activation.jar file).</li>
 * </ol>
 * 
 * @see MimetypesFileTypeMap
 */
public class MimetypesFileExtensionsMap {

  public static final String DEFAULT_FILE_EXTENSION = "bin";

  private static MimetypesFileExtensionsMap singleton;

  private Multimap<String, String> mimetypes;

  public static MimetypesFileExtensionsMap get() {
    if(singleton == null) {
      singleton = new MimetypesFileExtensionsMap();
    }
    return singleton;
  }

  public String getPreferedFileExtension(String mimetype) {
    if(mimetype == null) return DEFAULT_FILE_EXTENSION;

    Collection<String> exts = mimetypes.get(mimetype);
    if(exts.size() == 0) return DEFAULT_FILE_EXTENSION;
    else
      return exts.iterator().next();
  }

  public boolean containsMimetype(String mimetype) {
    return mimetypes.containsKey(mimetype);
  }

  public boolean containsFileExtension(String fileExtension) {
    return mimetypes.containsValue(fileExtension);
  }

  public Collection<String> getFileExtensions(String mimetype) {
    return mimetypes.get(mimetype);
  }

  private MimetypesFileExtensionsMap() {
    mimetypes = LinkedHashMultimap.create();
    initFromStream(ClassLoader.getSystemResourceAsStream("META-INF/mime.types"));
    initFromStream(ClassLoader.getSystemResourceAsStream("META-INF/mimetypes.default"));
  }

  private void initFromStream(InputStream in) {
    if(in == null) return;

    BufferedReader reader = new BufferedReader(new InputStreamReader(in));

    String line = null;
    try {
      while((line = reader.readLine()) != null) {
        if(line.startsWith("#") == false) {
          final String[] entries = line.split("\\s+");
          if(entries != null && entries.length > 1) {
            mimetypes.putAll(entries[0], new Iterable<String>() {

              @Override
              public Iterator<String> iterator() {

                return new Iterator<String>() {
                  private int pos = 1;

                  @Override
                  public boolean hasNext() {
                    return pos < entries.length;
                  }

                  @Override
                  public String next() {
                    return entries[pos++].trim();
                  }

                  @Override
                  public void remove() {
                  }

                };
              }
            });
          }
        }
      }
    } catch(IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

}
