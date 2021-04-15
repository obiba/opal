/*
 * Copyright (c) 2021 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.keystore.commands;

import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.opal.KeyForm;
import org.obiba.opal.web.model.client.opal.KeyType;

import javax.annotation.Nullable;

import static com.google.gwt.http.client.Response.SC_BAD_REQUEST;
import static com.google.gwt.http.client.Response.SC_INTERNAL_SERVER_ERROR;
import static com.google.gwt.http.client.Response.SC_OK;
import static com.google.gwt.http.client.Response.SC_CREATED;

public class CreateKeyPairCommand extends AbstractKeystoreCommand {
  private String algorithm;
  private String size;
  private String firstLastName;
  private String organization;
  private String organizationalUnit;
  private String locality;
  private String state;
  private String country;

  public CreateKeyPairCommand() {}

  @Override
  public void execute(@Nullable ResponseCodeCallback success, @Nullable ResponseCodeCallback failure) {
    KeyForm keyForm = KeyForm.create();
    keyForm.setAlias(alias);
    keyForm.setKeyType(KeyType.KEY_PAIR);

    keyForm.setPrivateForm(getPrivateKeyForm(algorithm, size));
    keyForm
        .setPublicForm(getPublicKeyForm(firstLastName, organization, organizationalUnit, locality, state, country));

    if (update) {
      ResourceRequestBuilderFactory.newBuilder() //
          .forResource(url) //
          .withResourceBody(KeyForm.stringify(keyForm)) //
          .withCallback(SC_OK, success)
          .withCallback(failure, SC_BAD_REQUEST, SC_BAD_REQUEST, SC_INTERNAL_SERVER_ERROR)
          .put().send();

    } else {
      ResourceRequestBuilderFactory.newBuilder() //
          .forResource(url) //
          .withResourceBody(KeyForm.stringify(keyForm)) //
          .withCallback(SC_CREATED, success)
          .withCallback(failure, SC_BAD_REQUEST, SC_BAD_REQUEST, SC_INTERNAL_SERVER_ERROR)
          .post().send();
    }

  }

  public static class Builder extends AbstractKeystoreCommand.Builder<Builder, CreateKeyPairCommand> {

    private Builder() {
      command = new CreateKeyPairCommand();
    }

    public static Builder newBuilder() {
      return new Builder();
    }

    public Builder setAlgorithm(String value) {
      command.algorithm = value;
      return this;
    }

    public Builder setSize(String value) {
      command.size = value;
      return this;
    }

    public Builder setFirstLastName(String value) {
      command.firstLastName = value;
      return this;
    }

    public Builder setOrganizationalUnit(String value) {
      command.organizationalUnit = value;
      return this;
    }

    public Builder setOrganization(String value) {
      command.organization = value;
      return this;
    }

    public Builder setLocality(String value) {
      command.locality = value;
      return this;
    }

    public Builder setState(String value) {
      command.state = value;
      return this;
    }

    public Builder setCountry(String value) {
      command.country = value;
      return this;
    }
  }
}
