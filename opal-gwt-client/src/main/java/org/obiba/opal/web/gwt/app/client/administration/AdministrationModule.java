/*
 * Copyright (c) 2019 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;
import org.obiba.opal.web.gwt.app.client.administration.configuration.edit.GeneralConfModalPresenter;
import org.obiba.opal.web.gwt.app.client.administration.configuration.edit.GeneralConfModalView;
import org.obiba.opal.web.gwt.app.client.administration.configuration.view.ConfigurationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.configuration.view.ConfigurationView;
import org.obiba.opal.web.gwt.app.client.administration.database.edit.mongo.MongoDatabaseModalPresenter;
import org.obiba.opal.web.gwt.app.client.administration.database.edit.mongo.MongoDatabaseModalView;
import org.obiba.opal.web.gwt.app.client.administration.database.edit.sql.SqlDatabaseModalPresenter;
import org.obiba.opal.web.gwt.app.client.administration.database.edit.sql.SqlDatabaseModalView;
import org.obiba.opal.web.gwt.app.client.administration.database.list.DatabaseAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.database.list.DatabaseAdministrationView;
import org.obiba.opal.web.gwt.app.client.administration.database.list.data.DataDatabasesPresenter;
import org.obiba.opal.web.gwt.app.client.administration.database.list.data.DataDatabasesView;
import org.obiba.opal.web.gwt.app.client.administration.database.list.identifiers.IdentifiersDatabasePresenter;
import org.obiba.opal.web.gwt.app.client.administration.database.list.identifiers.IdentifiersDatabaseView;
import org.obiba.opal.web.gwt.app.client.administration.datashield.presenter.*;
import org.obiba.opal.web.gwt.app.client.administration.datashield.view.*;
import org.obiba.opal.web.gwt.app.client.administration.fs.presenter.FilesAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.fs.view.FilesAdministrationView;
import org.obiba.opal.web.gwt.app.client.administration.identifiers.presenter.*;
import org.obiba.opal.web.gwt.app.client.administration.identifiers.view.*;
import org.obiba.opal.web.gwt.app.client.administration.idproviders.edit.IDProviderPresenter;
import org.obiba.opal.web.gwt.app.client.administration.idproviders.edit.IDProviderView;
import org.obiba.opal.web.gwt.app.client.administration.idproviders.list.IDProvidersAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.idproviders.list.IDProvidersAdministrationView;
import org.obiba.opal.web.gwt.app.client.administration.index.presenter.IndexAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.index.presenter.IndexConfigurationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.index.presenter.IndexPresenter;
import org.obiba.opal.web.gwt.app.client.administration.index.view.IndexAdministrationView;
import org.obiba.opal.web.gwt.app.client.administration.index.view.IndexConfigurationView;
import org.obiba.opal.web.gwt.app.client.administration.index.view.IndexView;
import org.obiba.opal.web.gwt.app.client.administration.jvm.JVMPresenter;
import org.obiba.opal.web.gwt.app.client.administration.jvm.JVMView;
import org.obiba.opal.web.gwt.app.client.administration.plugins.PluginServiceConfigurationModalPresenter;
import org.obiba.opal.web.gwt.app.client.administration.plugins.PluginServiceConfigurationModalView;
import org.obiba.opal.web.gwt.app.client.administration.plugins.PluginsAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.plugins.PluginsAdministrationView;
import org.obiba.opal.web.gwt.app.client.administration.presenter.AdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.r.RAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.r.RAdministrationView;
import org.obiba.opal.web.gwt.app.client.administration.r.RPackageInstallModalPresenter;
import org.obiba.opal.web.gwt.app.client.administration.r.RPackageInstallModalView;
import org.obiba.opal.web.gwt.app.client.administration.r.list.RSessionsPresenter;
import org.obiba.opal.web.gwt.app.client.administration.r.list.RSessionsView;
import org.obiba.opal.web.gwt.app.client.administration.r.list.RWorkspacesPresenter;
import org.obiba.opal.web.gwt.app.client.administration.r.list.RWorkspacesView;
import org.obiba.opal.web.gwt.app.client.administration.taxonomies.TaxonomiesAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.taxonomies.TaxonomiesAdministrationView;
import org.obiba.opal.web.gwt.app.client.administration.taxonomies.edit.TaxonomyEditModalPresenter;
import org.obiba.opal.web.gwt.app.client.administration.taxonomies.edit.TaxonomyEditModalView;
import org.obiba.opal.web.gwt.app.client.administration.taxonomies.git.TaxonomyGitImportModalPresenter;
import org.obiba.opal.web.gwt.app.client.administration.taxonomies.git.TaxonomyGitImportModalView;
import org.obiba.opal.web.gwt.app.client.administration.taxonomies.list.TaxonomiesPresenter;
import org.obiba.opal.web.gwt.app.client.administration.taxonomies.list.TaxonomiesView;
import org.obiba.opal.web.gwt.app.client.administration.taxonomies.term.edit.TermEditModalPresenter;
import org.obiba.opal.web.gwt.app.client.administration.taxonomies.term.edit.TermEditModalView;
import org.obiba.opal.web.gwt.app.client.administration.taxonomies.view.TaxonomyPresenter;
import org.obiba.opal.web.gwt.app.client.administration.taxonomies.view.TaxonomyView;
import org.obiba.opal.web.gwt.app.client.administration.taxonomies.vocabulary.edit.VocabularyEditModalPresenter;
import org.obiba.opal.web.gwt.app.client.administration.taxonomies.vocabulary.edit.VocabularyEditModalView;
import org.obiba.opal.web.gwt.app.client.administration.taxonomies.vocabulary.view.VocabularyPresenter;
import org.obiba.opal.web.gwt.app.client.administration.taxonomies.vocabulary.view.VocabularyView;
import org.obiba.opal.web.gwt.app.client.administration.users.changePassword.ChangePasswordModalPresenter;
import org.obiba.opal.web.gwt.app.client.administration.users.changePassword.ChangePasswordModalView;
import org.obiba.opal.web.gwt.app.client.administration.users.edit.SubjectCredentialsPresenter;
import org.obiba.opal.web.gwt.app.client.administration.users.edit.SubjectCredentialsView;
import org.obiba.opal.web.gwt.app.client.administration.users.list.SubjectCredentialsAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.users.list.SubjectCredentialsAdministrationView;
import org.obiba.opal.web.gwt.app.client.administration.users.profile.AddSubjectTokenModalPresenter;
import org.obiba.opal.web.gwt.app.client.administration.users.profile.AddSubjectTokenModalView;
import org.obiba.opal.web.gwt.app.client.administration.users.profile.SubjectProfilePresenter;
import org.obiba.opal.web.gwt.app.client.administration.users.profile.SubjectProfileView;
import org.obiba.opal.web.gwt.app.client.administration.users.profile.admin.SubjectProfilesAdministrationPresenter;
import org.obiba.opal.web.gwt.app.client.administration.users.profile.admin.SubjectProfilesAdministrationView;
import org.obiba.opal.web.gwt.app.client.administration.view.AdministrationView;

/**
 *
 */
@SuppressWarnings("OverlyCoupledClass")
public class AdministrationModule extends AbstractPresenterModule {

  @Override
  protected void configure() {
    bindPresenter(AdministrationPresenter.class, AdministrationPresenter.Display.class, AdministrationView.class,
        AdministrationPresenter.Proxy.class);
    configureDatabases();
    configureIndexes();
    configureR();
    configureDatashield();
    configureUserGroups();
    configureProfiles();
    configureIDProviders();
    configureSystemConfig();
    configureTaxonomies();
    configureIdentifiers();
  }

  private void configureSystemConfig() {
    bindPresenter(JVMPresenter.class, JVMPresenter.Display.class, JVMView.class, JVMPresenter.Proxy.class);
    bindPresenter(PluginsAdministrationPresenter.class, PluginsAdministrationPresenter.Display.class,
        PluginsAdministrationView.class, PluginsAdministrationPresenter.Proxy.class);
    bindPresenterWidget(PluginServiceConfigurationModalPresenter.class,
        PluginServiceConfigurationModalPresenter.Display.class, PluginServiceConfigurationModalView.class);
    bindPresenter(FilesAdministrationPresenter.class, FilesAdministrationPresenter.Display.class,
        FilesAdministrationView.class, FilesAdministrationPresenter.Proxy.class);
    bindPresenter(ConfigurationPresenter.class, ConfigurationPresenter.Display.class, ConfigurationView.class,
        ConfigurationPresenter.Proxy.class);
    bindPresenterWidget(GeneralConfModalPresenter.class, GeneralConfModalPresenter.Display.class,
        GeneralConfModalView.class);
  }

  private void configureTaxonomies() {
    bindPresenter(TaxonomiesAdministrationPresenter.class, TaxonomiesAdministrationPresenter.Display.class,
        TaxonomiesAdministrationView.class, TaxonomiesAdministrationPresenter.Proxy.class);
    bindPresenterWidget(TaxonomiesPresenter.class, TaxonomiesPresenter.Display.class, TaxonomiesView.class);
    bindPresenterWidget(TaxonomyPresenter.class, TaxonomyPresenter.Display.class, TaxonomyView.class);
    bindPresenterWidget(TaxonomyEditModalPresenter.class, TaxonomyEditModalPresenter.Display.class,
        TaxonomyEditModalView.class);
    bindPresenterWidget(TaxonomyGitImportModalPresenter.class, TaxonomyGitImportModalPresenter.Display.class,
        TaxonomyGitImportModalView.class);
    bindPresenterWidget(VocabularyPresenter.class, VocabularyPresenter.Display.class, VocabularyView.class);
    bindPresenterWidget(VocabularyEditModalPresenter.class, VocabularyEditModalPresenter.Display.class,
        VocabularyEditModalView.class);
    bindPresenterWidget(TermEditModalPresenter.class, TermEditModalPresenter.Display.class, TermEditModalView.class);
  }

  private void configureUserGroups() {
    bindPresenter(SubjectCredentialsAdministrationPresenter.class,
        SubjectCredentialsAdministrationPresenter.Display.class, SubjectCredentialsAdministrationView.class,
        SubjectCredentialsAdministrationPresenter.Proxy.class);
    bindPresenterWidget(SubjectCredentialsPresenter.class, SubjectCredentialsPresenter.Display.class,
        SubjectCredentialsView.class);
  }

  private void configureProfiles() {
    bindPresenter(SubjectProfilesAdministrationPresenter.class, SubjectProfilesAdministrationPresenter.Display.class,
        SubjectProfilesAdministrationView.class, SubjectProfilesAdministrationPresenter.Proxy.class);
    bindPresenter(SubjectProfilePresenter.class, SubjectProfilePresenter.Display.class, SubjectProfileView.class,
        SubjectProfilePresenter.Proxy.class);
    bindPresenterWidget(ChangePasswordModalPresenter.class, ChangePasswordModalPresenter.Display.class,
        ChangePasswordModalView.class);
    bindPresenterWidget(AddSubjectTokenModalPresenter.class, AddSubjectTokenModalPresenter.Display.class,
        AddSubjectTokenModalView.class);
  }

  private void configureIDProviders() {
    bindPresenter(IDProvidersAdministrationPresenter.class, IDProvidersAdministrationPresenter.Display.class,
        IDProvidersAdministrationView.class, IDProvidersAdministrationPresenter.Proxy.class);
    bindPresenterWidget(IDProviderPresenter.class, IDProviderPresenter.Display.class,
        IDProviderView.class);
  }

  private void configureR() {
    bindPresenter(RAdministrationPresenter.class, RAdministrationPresenter.Display.class, RAdministrationView.class,
        RAdministrationPresenter.Proxy.class);
    bindPresenterWidget(RSessionsPresenter.class, RSessionsPresenter.Display.class, RSessionsView.class);
    bindPresenterWidget(RWorkspacesPresenter.class, RWorkspacesPresenter.Display.class, RWorkspacesView.class);
    bindPresenterWidget(RPackageInstallModalPresenter.class, RPackageInstallModalPresenter.Display.class,
        RPackageInstallModalView.class);
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
    bindPresenterWidget(DataShieldROptionsPresenter.class, DataShieldROptionsPresenter.Display.class,
        DataShieldROptionsView.class);
    bindPresenterWidget(DataShieldROptionModalPresenter.class, DataShieldROptionModalPresenter.Display.class,
        DataShieldROptionModalView.class);
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
    bindPresenter(IdentifiersAdministrationPresenter.class, IdentifiersAdministrationPresenter.Display.class,
        IdentifiersAdministrationView.class, IdentifiersAdministrationPresenter.Proxy.class);
    bindPresenterWidget(IdentifiersTablePresenter.class, IdentifiersTablePresenter.Display.class,
        IdentifiersTableView.class);
    bindPresenterWidget(IdentifiersTableModalPresenter.class, IdentifiersTableModalPresenter.Display.class,
        IdentifiersTableModalView.class);
    bindPresenterWidget(ImportSystemIdentifiersModalPresenter.class,
        ImportSystemIdentifiersModalPresenter.Display.class, ImportSystemIdentifiersModalView.class);
    bindPresenterWidget(CopySystemIdentifiersModalPresenter.class, CopySystemIdentifiersModalPresenter.Display.class,
        CopySystemIdentifiersModalView.class);
    bindPresenterWidget(ImportIdentifiersMappingModalPresenter.class,
        ImportIdentifiersMappingModalPresenter.Display.class, ImportIdentifiersMappingModalView.class);
    bindPresenterWidget(IdentifiersMappingModalPresenter.class, IdentifiersMappingModalPresenter.Display.class,
        IdentifiersMappingModalView.class);
    bindPresenterWidget(GenerateIdentifiersModalPresenter.class, GenerateIdentifiersModalPresenter.Display.class,
        GenerateIdentifiersModalView.class);
  }
}
