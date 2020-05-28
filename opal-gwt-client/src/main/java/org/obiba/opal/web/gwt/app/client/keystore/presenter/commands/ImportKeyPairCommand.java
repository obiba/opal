/*
 * Copyright (c) 2020 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.keystore.presenter.commands;

import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.opal.KeyForm;
import org.obiba.opal.web.model.client.opal.KeyType;

import javax.annotation.Nullable;

import static com.google.gwt.http.client.Response.SC_BAD_REQUEST;
import static com.google.gwt.http.client.Response.SC_CREATED;
import static com.google.gwt.http.client.Response.SC_INTERNAL_SERVER_ERROR;
import static com.google.gwt.http.client.Response.SC_OK;

public class ImportKeyPairCommand extends AbstractKeystoreCommand {

  private String publicKey;

  private String privateKey;

  private KeyType keyType;

  @Override
  public void execute(@Nullable ResponseCodeCallback success, @Nullable ResponseCodeCallback failure) {
    KeyForm keyForm = KeyForm.create();
    keyForm.setAlias(alias);
    keyForm.setKeyType(keyType);

    keyForm.setPrivateImport(privateKey);
    keyForm.setPublicImport(publicKey);

    if(update) {
      ResourceRequestBuilderFactory.newBuilder() //
          .forResource(url) //
          .withResourceBody(KeyForm.stringify(keyForm)) //
          .withCallback(SC_OK, success).withCallback(failure, SC_BAD_REQUEST, SC_INTERNAL_SERVER_ERROR).put().send();

    } else {
      ResourceRequestBuilderFactory.newBuilder() //
          .forResource(url) //
          .withResourceBody(KeyForm.stringify(keyForm)) //
          .withCallback(SC_CREATED, success).withCallback(failure, SC_BAD_REQUEST, SC_INTERNAL_SERVER_ERROR).post()
          .send();

    }
  }

  public static class Builder extends AbstractKeystoreCommand.Builder<Builder, ImportKeyPairCommand> {

    private Builder() {
      command = new ImportKeyPairCommand();
    }

    public static Builder newBuilder() {
      return new Builder();
    }

    public Builder setPublicKey(String value) {
      command.publicKey = value;
      return this;
    }

    public Builder setKeyType(KeyType value) {
      command.keyType = value;
      return this;
    }

    public Builder setPrivateKey(String value) {
      command.privateKey = value;
      return this;
    }

  }

}
