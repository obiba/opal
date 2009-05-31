/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.datasource.onyx;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.XStream;

/**
 * A Map-like utility class used for holding the values of parameters used during encryption. This class offers XML
 * marshalling/unmarshalling to simplify its transport.
 * <p>
 * Example XML transport file:
 * 
 * <pre>
 * &lt;parameters&gt;
 *  &lt;parameter name=&quot;publicKey&quot;&gt;
 *  &lt;value class=&quot;byte-array&quot;&gt;MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAOR9KIp9t1yn3xduuk4tYK2oeCtNt6G2bmI43tLZmsdb
 *  TxrgNLvl+ozhP9GF7xQZG5RP9Y2uupIDy/WeFwS2rlkCAwEAAQ==&lt;/value&gt;
 *  &lt;/parameter&gt;
 *  &lt;parameter name=&quot;publicKeyFormat&quot;&gt;
 *  &lt;value class=&quot;string&quot;&gt;X.509&lt;/value&gt;
 *  &lt;/parameter&gt;
 *  &lt;parameter name=&quot;key&quot;&gt;
 *  &lt;value class=&quot;byte-array&quot;&gt;M0j5qW6FaWgylytzhP7J3KTsrS+AY38abkO7U7Iplk4p0mwNQjelSOw+Y7CivxruFBQ50Q1Oktl4
 *  6kWX3lyu2w==&lt;/value&gt;
 *  &lt;/parameter&gt;
 *  &lt;parameter name=&quot;transformation&quot;&gt;
 *  &lt;value class=&quot;string&quot;&gt;AES/CFB/NoPadding&lt;/value&gt;
 *  &lt;/parameter&gt;
 *  &lt;parameter name=&quot;iv&quot;&gt;
 *  &lt;value class=&quot;byte-array&quot;&gt;4WGRhaaZ1MRXRQ1glbiuzw==&lt;/value&gt;
 *  &lt;/parameter&gt;
 *  &lt;parameter name=&quot;algorithmParameters&quot;&gt;
 *  &lt;value class=&quot;byte-array&quot;&gt;BBDhYZGFppnUxFdFDWCVuK7P&lt;/value&gt;
 *  &lt;/parameter&gt;
 *  &lt;/parameters&gt;
 * 
 * </pre>
 */
public class EncryptionData {

  public static class Entry {

    private String name;

    private Object value;

    public Entry(String name, Object value) {
      this.name = name;
      this.value = value;
    }
  }

  private List<Entry> entries = new ArrayList<Entry>();

  public EncryptionData() {

  }

  @SuppressWarnings("unchecked")
  public <T> T getEntry(String name) {
    for(Entry entry : entries) {
      if(entry.name.equals(name)) {
        return (T) entry.value;
      }
    }
    return null;
  }

  public static EncryptionData fromXml(byte[] xml) {
    try {
      return (EncryptionData) buildXStream().fromXML(new String(xml, "ISO-8859-1"));
    } catch(UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  public static EncryptionData fromXml(InputStream xml) {
    return (EncryptionData) buildXStream().fromXML(xml);
  }

  public byte[] toXml() {
    try {
      return buildXStream().toXML(this).getBytes("ISO-8859-1");
    } catch(UnsupportedEncodingException e) {
      throw new RuntimeException(e);
    }
  }

  public void addEntry(String name, String value) {
    entries.add(new Entry(name, value));
  }

  public void addEntry(String name, byte[] value) {
    entries.add(new Entry(name, value));
  }

  private static XStream buildXStream() {
    XStream xstream = new XStream();
    xstream.alias("parameters", EncryptionData.class);
    xstream.alias("parameter", Entry.class);

    xstream.addImplicitCollection(EncryptionData.class, "entries");
    xstream.useAttributeFor(Entry.class, "name");
    return xstream;
  }
}
