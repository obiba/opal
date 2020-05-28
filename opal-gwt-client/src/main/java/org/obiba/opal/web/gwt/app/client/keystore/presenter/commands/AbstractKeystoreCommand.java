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

import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.model.client.opal.PrivateKeyForm;
import org.obiba.opal.web.model.client.opal.PublicKeyForm;

import javax.annotation.Nullable;

public abstract class AbstractKeystoreCommand implements KeystoreCommand {

  protected String url;
  protected String alias;
  protected boolean update = false;

  public abstract void execute(@Nullable ResponseCodeCallback success, @Nullable ResponseCodeCallback failure);


  protected PrivateKeyForm getPrivateKeyForm(String algorithm, String size) {
    PrivateKeyForm privateForm = PrivateKeyForm.create();
    privateForm.setAlgo(algorithm);
    privateForm.setSize(Integer.parseInt(size));
    return privateForm;
  }

  protected PublicKeyForm getPublicKeyForm(String firstLastName, String organization, String organizationalUnit,
      String locality, String state, String country) {
    PublicKeyForm publicForm = PublicKeyForm.create();
    publicForm.setName(firstLastName);
    publicForm.setOrganization(organization);
    publicForm.setOrganizationalUnit(organizationalUnit);
    publicForm.setLocality(locality);
    publicForm.setState(state);
    publicForm.setCountry(country);
    return publicForm;
  }

  protected static class Builder<T extends Builder, C extends AbstractKeystoreCommand> {
    protected C command;

    public T setUrl(String value) {
      command.url = value;
      return (T)this;
    }

    public T setAlias(String value) {
      command.alias = value;
      return (T)this;
    }

    public T setUpdate(boolean value) {
      command.update = value;
      return (T)this;
    }


    public C build() {
      return command;
    }
  }

}
