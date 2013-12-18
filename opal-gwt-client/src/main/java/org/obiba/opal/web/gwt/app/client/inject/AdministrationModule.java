/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.inject;

import org.obiba.opal.web.gwt.app.client.administration.configuration.presenter.ConfigurationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.configuration.presenter.GeneralConfModalPresenter;
import org.obiba.opal.web.gwt.app.client.administration.configuration.view.ConfigurationView;
import org.obiba.opal.web.gwt.app.client.administration.configuration.view.GeneralConfModalView;
import org.obiba.opal.web.gwt.app.client.administration.database.presenter.DataDatabasesPresenter;
import org.obiba.opal.web.gwt.app.client.administration.database.presenter.DatabaseAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.database.presenter.IdentifiersDatabasePresenter;
import org.obiba.opal.web.gwt.app.client.administration.database.presenter.MongoDatabaseModalPresenter;
import org.obiba.opal.web.gwt.app.client.administration.database.presenter.SqlDatabaseModalPresenter;
import org.obiba.opal.web.gwt.app.client.administration.database.view.DataDatabasesView;
import org.obiba.opal.web.gwt.app.client.administration.database.view.DatabaseAdministrationView;
import org.obiba.opal.web.gwt.app.client.administration.database.view.IdentifiersDatabaseView;
import org.obiba.opal.web.gwt.app.client.administration.database.view.MongoDatabaseModalView;
import org.obiba.opal.web.gwt.app.client.administration.database.view.SqlDatabaseModalView;
import org.obiba.opal.web.gwt.app.client.administration.datashield.presenter.DataShieldAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.datashield.presenter.DataShieldConfigPresenter;
import org.obiba.opal.web.gwt.app.client.administration.datashield.presenter.DataShieldMethodPresenter;
import org.obiba.opal.web.gwt.app.client.administration.datashield.presenter.DataShieldPackageAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.datashield.presenter.DataShieldPackageCreatePresenter;
import org.obiba.opal.web.gwt.app.client.administration.datashield.presenter.DataShieldPackagePresenter;
import org.obiba.opal.web.gwt.app.client.administration.datashield.view.DataShieldAdministrationView;
import org.obiba.opal.web.gwt.app.client.administration.datashield.view.DataShieldConfigView;
import org.obiba.opal.web.gwt.app.client.administration.datashield.view.DataShieldMethodView;
import org.obiba.opal.web.gwt.app.client.administration.datashield.view.DataShieldPackageAdministrationView;
import org.obiba.opal.web.gwt.app.client.administration.datashield.view.DataShieldPackageCreateView;
import org.obiba.opal.web.gwt.app.client.administration.datashield.view.DataShieldPackageView;
import org.obiba.opal.web.gwt.app.client.administration.fs.presenter.FilesAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.fs.view.FilesAdministrationView;
import org.obiba.opal.web.gwt.app.client.administration.identifiers.presenter.GenerateIdentifiersModalPresenter;
import org.obiba.opal.web.gwt.app.client.administration.identifiers.presenter.IdentifiersAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.identifiers.presenter.IdentifiersMappingModalPresenter;
import org.obiba.opal.web.gwt.app.client.administration.identifiers.presenter.IdentifiersTableModalPresenter;
import org.obiba.opal.web.gwt.app.client.administration.identifiers.presenter.IdentifiersTablePresenter;
import org.obiba.opal.web.gwt.app.client.administration.identifiers.presenter.ImportIdentifiersMappingModalPresenter;
import org.obiba.opal.web.gwt.app.client.administration.identifiers.presenter.ImportSystemIdentifiersModalPresenter;
import org.obiba.opal.web.gwt.app.client.administration.identifiers.view.GenerateIdentifiersModalView;
import org.obiba.opal.web.gwt.app.client.administration.identifiers.view.IdentifiersAdministrationView;
import org.obiba.opal.web.gwt.app.client.administration.identifiers.view.IdentifiersMappingModalView;
import org.obiba.opal.web.gwt.app.client.administration.identifiers.view.IdentifiersTableModalView;
import org.obiba.opal.web.gwt.app.client.administration.identifiers.view.IdentifiersTableView;
import org.obiba.opal.web.gwt.app.client.administration.identifiers.view.ImportIdentifiersMappingModalView;
import org.obiba.opal.web.gwt.app.client.administration.identifiers.view.ImportSystemIdentifiersModalView;
import org.obiba.opal.web.gwt.app.client.administration.index.presenter.IndexAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.index.presenter.IndexConfigurationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.index.presenter.IndexPresenter;
import org.obiba.opal.web.gwt.app.client.administration.index.view.IndexAdministrationView;
import org.obiba.opal.web.gwt.app.client.administration.index.view.IndexConfigurationView;
import org.obiba.opal.web.gwt.app.client.administration.index.view.IndexView;
import org.obiba.opal.web.gwt.app.client.administration.jvm.presenter.JVMPresenter;
import org.obiba.opal.web.gwt.app.client.administration.jvm.view.JVMView;
import org.obiba.opal.web.gwt.app.client.administration.presenter.AdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.r.presenter.RAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.r.view.RAdministrationView;
import org.obiba.opal.web.gwt.app.client.administration.subjectCredentials.presenter.SubjectCredentialsAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.subjectCredentials.presenter.SubjectCredentialsPresenter;
import org.obiba.opal.web.gwt.app.client.administration.subjectCredentials.view.SubjectCredentialsAdministrationView;
import org.obiba.opal.web.gwt.app.client.administration.subjectCredentials.view.SubjectCredentialsView;
import org.obiba.opal.web.gwt.app.client.administration.taxonomies.presenter.TaxonomiesPresenter;
import org.obiba.opal.web.gwt.app.client.administration.taxonomies.presenter.TaxonomyEditModalPresenter;
import org.obiba.opal.web.gwt.app.client.administration.taxonomies.presenter.VocabularyEditPresenter;
import org.obiba.opal.web.gwt.app.client.administration.taxonomies.presenter.VocabularyPresenter;
import org.obiba.opal.web.gwt.app.client.administration.taxonomies.view.TaxonomiesView;
import org.obiba.opal.web.gwt.app.client.administration.taxonomies.view.TaxonomyEditModalView;
import org.obiba.opal.web.gwt.app.client.administration.taxonomies.view.VocabularyEditView;
import org.obiba.opal.web.gwt.app.client.administration.taxonomies.view.VocabularyView;
import org.obiba.opal.web.gwt.app.client.administration.view.AdministrationView;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 *
 */
@SuppressWarnings("OverlyCoupledClass")
public class AdministrationModule extends AbstractPresenterModule {

  @Override
  protected void configure() {
    bindPresenter(AdministrationPresenter.class, AdministrationPresenter.Display.class, AdministrationView.class,
        AdministrationPresenter.Proxy.class);
    bindPresenter(RAdministrationPresenter.class, RAdministrationPresenter.Display.class, RAdministrationView.class,
        RAdministrationPresenter.Proxy.class);
    configureDatabases();
    configureIndexes();
    configureDatashield();
    configureUserGroups();
    configureSystemConfig();
    configureTaxonomies();
    configureIdentifiers();
  }

  private void configureSystemConfig() {
    bindPresenter(JVMPresenter.class, JVMPresenter.Display.class, JVMView.class, JVMPresenter.Proxy.class);

    bindPresenter(FilesAdministrationPresenter.class, FilesAdministrationPresenter.Display.class,
        FilesAdministrationView.class, FilesAdministrationPresenter.Proxy.class);
    bindPresenter(ConfigurationPresenter.class, ConfigurationPresenter.Display.class, ConfigurationView.class,
        ConfigurationPresenter.Proxy.class);
    bindPresenterWidget(GeneralConfModalPresenter.class, GeneralConfModalPresenter.Display.class,
        GeneralConfModalView.class);
  }

  private void configureTaxonomies() {
    bindPresenter(TaxonomiesPresenter.class, TaxonomiesPresenter.Display.class, TaxonomiesView.class,
        TaxonomiesPresenter.Proxy.class);
    bindPresenterWidget(TaxonomyEditModalPresenter.class, TaxonomyEditModalPresenter.Display.class,
        TaxonomyEditModalView.class);
    bindPresenter(VocabularyPresenter.class, VocabularyPresenter.Display.class, VocabularyView.class,
        VocabularyPresenter.Proxy.class);
    bindPresenter(VocabularyEditPresenter.class, VocabularyEditPresenter.Display.class, VocabularyEditView.class,
        VocabularyEditPresenter.Proxy.class);
  }

  private void configureUserGroups() {
    bindPresenter(SubjectCredentialsAdministrationPresenter.class,
        SubjectCredentialsAdministrationPresenter.Display.class, SubjectCredentialsAdministrationView.class,
        SubjectCredentialsAdministrationPresenter.Proxy.class);
    bindPresenterWidget(SubjectCredentialsPresenter.class, SubjectCredentialsPresenter.Display.class,
        SubjectCredentialsView.class);
  }

  private void configureDatashield() {
    bindPresenter(DataShieldConfigPresenter.class, DataShieldConfigPresenter.Display.class, DataShieldConfigView.class,
        DataShieldConfigPresenter.Proxy.class);
    bindPresenterWidget(DataShieldPackageAdministrationPresenter.class,
        DataShieldPackageAdministrationPresenter.Display.class, DataShieldPackageAdministrationView.class);
    bindPresenterWidget(DataShieldAdministrationPresenter.class, DataShieldAdministrationPresenter.Display.class,
        DataShieldAdministrationView.class);
    bindPresenterWidget(DataShieldPackageCreatePresenter.class, DataShieldPackageCreatePresenter.Display.class,
        DataShieldPackageCreateView.class);
    bindPresenterWidget(DataShieldPackagePresenter.class, DataShieldPackagePresenter.Display.class,
        DataShieldPackageView.class);
    bindPresenterWidget(DataShieldMethodPresenter.class, DataShieldMethodPresenter.Display.class,
        DataShieldMethodView.class);
  }

  private void configureIndexes() {
    bindPresenter(IndexAdministrationPresenter.class, IndexAdministrationPresenter.Display.class,
        IndexAdministrationView.class, IndexAdministrationPresenter.Proxy.class);
    bindPresenterWidget(IndexPresenter.class, IndexPresenter.Display.class, IndexView.class);
    bindPresenterWidget(IndexConfigurationPresenter.class, IndexConfigurationPresenter.Display.class,
        IndexConfigurationView.class);
  }

  private void configureDatabases() {
    bindPresenter(DatabaseAdministrationPresenter.class, DatabaseAdministrationPresenter.Display.class,
        DatabaseAdministrationView.class, DatabaseAdministrationPresenter.Proxy.class);
    bindPresenterWidget(IdentifiersDatabasePresenter.class, IdentifiersDatabasePresenter.Display.class,
        IdentifiersDatabaseView.class);
    bindPresenterWidget(DataDatabasesPresenter.class, DataDatabasesPresenter.Display.class, DataDatabasesView.class);
    bindPresenterWidget(SqlDatabaseModalPresenter.class, SqlDatabaseModalPresenter.Display.class,
        SqlDatabaseModalView.class);
    bindPresenterWidget(MongoDatabaseModalPresenter.class, MongoDatabaseModalPresenter.Display.class,
        MongoDatabaseModalView.class);
  }
  private void configureIdentifiers() {
    bindPresenter(IdentifiersAdministrationPresenter.class, IdentifiersAdministrationPresenter.Display.class, IdentifiersAdministrationView.class,
        IdentifiersAdministrationPresenter.Proxy.class);
    bindPresenterWidget(IdentifiersTablePresenter.class, IdentifiersTablePresenter.Display.class,
        IdentifiersTableView.class);
    bindPresenterWidget(IdentifiersTableModalPresenter.class, IdentifiersTableModalPresenter.Display.class,
        IdentifiersTableModalView.class);
    bindPresenterWidget(ImportSystemIdentifiersModalPresenter.class, ImportSystemIdentifiersModalPresenter.Display.class,
        ImportSystemIdentifiersModalView.class);
    bindPresenterWidget(ImportIdentifiersMappingModalPresenter.class, ImportIdentifiersMappingModalPresenter.Display.class,
        ImportIdentifiersMappingModalView.class);
    bindPresenterWidget(IdentifiersMappingModalPresenter.class, IdentifiersMappingModalPresenter.Display.class,
        IdentifiersMappingModalView.class);
    bindPresenterWidget(GenerateIdentifiersModalPresenter.class, GenerateIdentifiersModalPresenter.Display.class,
        GenerateIdentifiersModalView.class);
  }
}
