package org.obiba.opal.search.service.support;

import org.apache.lucene.search.ScoreDoc;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class ScoreDocSerializer {
  public static String serialize(ScoreDoc scoreDoc) {
    String doc = scoreDoc.doc + ":" + scoreDoc.score + ":" + scoreDoc.shardIndex;
    return Base64.getEncoder().encodeToString(doc.getBytes(StandardCharsets.UTF_8));
  }

  public static ScoreDoc deserialize(String serialized) {
    byte[] decoded = Base64.getDecoder().decode(serialized);
    String[] parts = new String(decoded, StandardCharsets.UTF_8).split(":");
    return new ScoreDoc(Integer.parseInt(parts[0]), Float.parseFloat(parts[1]), Integer.parseInt(parts[2]));
  }
}
