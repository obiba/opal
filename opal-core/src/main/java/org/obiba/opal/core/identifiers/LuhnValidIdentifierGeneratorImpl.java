package org.obiba.opal.core.identifiers;

import org.obiba.opal.core.tools.LuhnValidator;

public final class LuhnValidIdentifierGeneratorImpl implements IdentifierGenerator {

  private int keySize;

  private String prefix;

  public LuhnValidIdentifierGeneratorImpl() {
    keySize = 10;
    prefix = "";
  }

  public void setKeySize(int keySize) {
    this.keySize = keySize;
  }

  public void setPrefix(String prefix) {
    this.prefix = prefix != null ? prefix.trim() : "";
  }

  @Override
  public String generateIdentifier() {
    if(keySize < 1) {
      throw new IllegalStateException("keySize must be at least 1: " + keySize);
    }

    return prefix + LuhnValidator.generate(keySize);
  }
}
