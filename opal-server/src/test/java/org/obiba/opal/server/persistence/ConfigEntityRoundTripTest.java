/*
 * Copyright (c) 2026 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.server.persistence;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.Test;
import org.obiba.datashield.core.DSMethodType;
import org.obiba.datashield.core.impl.DefaultDSMethod;
import org.obiba.opal.core.domain.*;
import org.obiba.opal.core.domain.database.Database;
import org.obiba.opal.core.domain.database.MongoDbSettings;
import org.obiba.opal.core.domain.database.SqlSettings;
import org.obiba.opal.core.domain.kubernetes.Container;
import org.obiba.opal.core.domain.kubernetes.PodSpec;
import org.obiba.opal.core.domain.kubernetes.Toleration;
import org.obiba.opal.core.domain.security.*;
import org.obiba.opal.core.runtime.App;
import org.obiba.opal.datashield.cfg.DataShieldProfile;
import org.obiba.opal.r.service.RSessionActivity;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Writes every configuration entity and reads it back from the database.
 * <p>
 * This is where a mis-mapped field shows up. Schema validation only proves the columns exist; it says nothing about
 * whether a set, a map or a nested object survives the trip, and those are stored as JSON through an
 * {@code AttributeConverter}. A converter wired to the wrong field, or a generic type erased to the wrong shape,
 * passes validation and silently loses data - so each test here populates the converted fields and asserts them back.
 */
public class ConfigEntityRoundTripTest extends AbstractConfigPersistenceTest {

  @Test
  public void test_project_keeps_its_tags_and_identifiers_mappings() {
    Project project = Project.Builder.create()
        .name("cohort")
        .title("Cohort")
        .description("A description long enough to be worth a clob.")
        .idMapping(ProjectIdentifiersMapping.newBuilder()
            .entityType("Participant").name("mapping").mapping("id").build())
        .build();
    project.addTag("epidemiology");
    project.addTag("longitudinal");

    Project reloaded = roundTrip(project);

    assertThat(reloaded.getName()).isEqualTo("cohort");
    assertThat(reloaded.getTags()).containsOnly("epidemiology", "longitudinal");
    assertThat(reloaded.getIdentifiersMappings()).hasSize(1);
    ProjectIdentifiersMapping mapping = reloaded.getIdentifiersMappings().iterator().next();
    assertThat(mapping.getEntityType()).isEqualTo("Participant");
    assertThat(mapping.getName()).isEqualTo("mapping");
    assertThat(mapping.getMapping()).isEqualTo("id");
  }

  @Test
  public void test_database_keeps_its_usage_and_settings() {
    SqlSettings sqlSettings = new SqlSettings();
    sqlSettings.setDriverClass("org.h2.Driver");
    sqlSettings.setUrl("jdbc:h2:file:store");
    sqlSettings.setUsername("opal");
    sqlSettings.setPassword("secret");
    sqlSettings.setSqlSchema(SqlSettings.SqlSchema.JDBC);

    MongoDbSettings mongoDbSettings = new MongoDbSettings();
    mongoDbSettings.setUrl("mongodb://localhost:27017");
    mongoDbSettings.setUsername("mongo");

    Database database = new Database();
    database.setName("store");
    database.setUsage(Database.Usage.STORAGE);
    database.setDefaultStorage(true);
    database.setSqlSettings(sqlSettings);
    database.setMongoDbSettings(mongoDbSettings);

    Database reloaded = roundTrip(database);

    assertThat(reloaded.getUsage()).isEqualTo(Database.Usage.STORAGE);
    assertThat(reloaded.isDefaultStorage()).isTrue();
    assertThat(reloaded.getSqlSettings().getUrl()).isEqualTo("jdbc:h2:file:store");
    assertThat(reloaded.getSqlSettings().getUsername()).isEqualTo("opal");
    assertThat(reloaded.getSqlSettings().getSqlSchema()).isEqualTo(SqlSettings.SqlSchema.JDBC);
    assertThat(reloaded.getMongoDbSettings().getUrl()).isEqualTo("mongodb://localhost:27017");
  }

  @Test
  public void test_subject_acl_keeps_its_subject_type() {
    SubjectAcl acl = new SubjectAcl("opal", "/project/cohort", SubjectAcl.SubjectType.USER.subjectFor("alice"),
        "PROJECT_ALL");

    SubjectAcl reloaded = roundTrip(acl);

    assertThat(reloaded.getDomain()).isEqualTo("opal");
    assertThat(reloaded.getNode()).isEqualTo("/project/cohort");
    assertThat(reloaded.getPrincipal()).isEqualTo("alice");
    assertThat(reloaded.getType()).isEqualTo(SubjectAcl.SubjectType.USER);
    assertThat(reloaded.getPermission()).isEqualTo("PROJECT_ALL");
  }

  @Test
  public void test_subject_credentials_keeps_its_groups_and_leaves_the_certificate_out() {
    SubjectCredentials credentials = new SubjectCredentials("alice");
    credentials.setAuthenticationType(SubjectCredentials.AuthenticationType.PASSWORD);
    credentials.setPassword("hashed");
    credentials.setEnabled(true);
    credentials.setGroups(Sets.newHashSet("analysts", "readers"));
    // Held only long enough to reach the keystore, and never stored with the credentials.
    credentials.setCertificate(new byte[]{1, 2, 3});

    SubjectCredentials reloaded = roundTrip(credentials);

    assertThat(reloaded.getAuthenticationType()).isEqualTo(SubjectCredentials.AuthenticationType.PASSWORD);
    assertThat(reloaded.isEnabled()).isTrue();
    assertThat(reloaded.getGroups()).containsOnly("analysts", "readers");
    assertThat(reloaded.getCertificate()).isNull();
  }

  @Test
  public void test_subject_profile_keeps_its_bookmarks_and_provider_claims() {
    SubjectProfile profile = new SubjectProfile("alice", "opal-user-realm");
    profile.setGroups(Sets.newHashSet("analysts"));
    profile.setBookmarks(Sets.newHashSet(new Bookmark("/project/cohort")));
    profile.setSecret("otp-secret");
    Map<String, Object> userInfo = ImmutableMap.of("email", "alice@example.org", "email_verified", true);
    profile.setUserInfo(userInfo);

    SubjectProfile reloaded = roundTrip(profile);

    assertThat(reloaded.getGroups()).containsOnly("analysts");
    assertThat(reloaded.getBookmarks()).hasSize(1);
    assertThat(reloaded.getBookmarks().iterator().next().getResource()).isEqualTo("/project/cohort");
    assertThat(reloaded.getSecret()).isEqualTo("otp-secret");
    assertThat(reloaded.getUserInfo().get("email")).isEqualTo("alice@example.org");
    assertThat(reloaded.getUserInfo().get("email_verified")).isEqualTo(Boolean.TRUE);
  }

  @Test
  public void test_subject_token_keeps_its_scopes_and_flags() {
    SubjectToken token = new SubjectToken();
    token.setToken("abcdef");
    token.setPrincipal("alice");
    token.setName("scripting");
    token.setProjects(Sets.newHashSet("cohort", "study"));
    token.setCommands(Sets.newHashSet("import", "export"));
    token.setUseR(true);
    token.setSystemAdmin(false);

    SubjectToken reloaded = roundTrip(token);

    assertThat(reloaded.getProjects()).containsOnly("cohort", "study");
    assertThat(reloaded.getCommands()).containsOnly("import", "export");
    assertThat(reloaded.isUseR()).isTrue();
    assertThat(reloaded.isSystemAdmin()).isFalse();
  }

  @Test
  public void test_group_keeps_its_members() {
    Group group = new Group("analysts");
    group.setSubjectCredentials(Sets.newHashSet("alice", "bob"));

    Group reloaded = roundTrip(group);

    assertThat(reloaded.getName()).isEqualTo("analysts");
    assertThat(reloaded.getSubjectCredentials()).containsOnly("alice", "bob");
  }

  @Test
  public void test_resource_reference_keeps_its_models_and_own_timestamps() {
    ResourceReference reference = new ResourceReference();
    reference.setProject("cohort");
    reference.setName("files");
    reference.setProvider("opal-resource-plugin");
    reference.setFactory("file");
    reference.setParametersModel("{\"path\":\"/data\"}");
    reference.setEncryptedCredentialsModel("encrypted");

    ResourceReference reloaded = roundTrip(reference);

    assertThat(reloaded.getProject()).isEqualTo("cohort");
    assertThat(reloaded.getParametersModel()).isEqualTo("{\"path\":\"/data\"}");
    assertThat(reloaded.getEncryptedCredentialsModel()).isEqualTo("encrypted");
    assertThat(reloaded.getCreated()).isNotNull();
  }

  @Test
  public void test_vcf_samples_mapping_round_trips() {
    VCFSamplesMapping mapping = new VCFSamplesMapping("cohort");
    mapping.setTableReference("cohort.samples");
    mapping.setParticipantIdVariable("participant_id");
    mapping.setSampleRoleVariable("role");

    VCFSamplesMapping reloaded = roundTrip(mapping);

    assertThat(reloaded.getProjectName()).isEqualTo("cohort");
    assertThat(reloaded.getTableReference()).isEqualTo("cohort.samples");
    assertThat(reloaded.getParticipantIdVariable()).isEqualTo("participant_id");
    assertThat(reloaded.getSampleRoleVariable()).isEqualTo("role");
  }

  @Test
  public void test_opal_analysis_keeps_its_variables() {
    OpalAnalysis analysis = OpalAnalysis.Builder.create(null)
        .name("validation")
        .datasource("cohort")
        .table("samples")
        .pluginName("opal-analysis-validate")
        .templateName("default")
        .variables(Lists.newArrayList("age", "sex"))
        .build();

    OpalAnalysis reloaded = roundTrip(analysis);

    assertThat(reloaded.getName()).isEqualTo("validation");
    assertThat(reloaded.getTable()).isEqualTo("samples");
    assertThat(reloaded.getVariables()).containsExactly("age", "sex");
  }

  @Test
  public void test_opal_analysis_result_round_trips() {
    // The result carries no setters: it is built from the analysis that produced it, so a default instance is all
    // this can assert - that the natural string key it assigns itself survives the trip.
    OpalAnalysisResult<?> result = new OpalAnalysisResult<>();

    OpalAnalysisResult<?> reloaded = roundTrip(result);

    assertThat(reloaded).isNotNull();
    assertThat(reloaded.getId()).isEqualTo(result.getId());
  }

  @Test
  public void test_general_config_keeps_its_locales() {
    OpalGeneralConfig config = new OpalGeneralConfig();
    config.setName("Opal");
    config.setLocales(Lists.newArrayList(Locale.ENGLISH, Locale.FRENCH));
    config.setPublicUrl("https://opal.example.org");
    config.setEnforced2FA(true);

    OpalGeneralConfig reloaded = roundTrip(config);

    assertThat(reloaded.getLocales()).containsExactly(Locale.ENGLISH, Locale.FRENCH);
    assertThat(reloaded.getPublicUrl()).isEqualTo("https://opal.example.org");
    assertThat(reloaded.isEnforced2FA()).isTrue();
  }

  @Test
  public void test_apps_config_keeps_its_rock_configurations() {
    AppsConfig config = new AppsConfig();
    config.setToken("registration-token");
    config.setRockAppConfigs(Lists.newArrayList(new RockAppConfig("https://rock.example.org")));

    AppsConfig reloaded = roundTrip(config);

    assertThat(reloaded.getToken()).isEqualTo("registration-token");
    assertThat(reloaded.getRockAppConfigs()).hasSize(1);
    assertThat(reloaded.getRockAppConfigs().get(0).getHost()).isEqualTo("https://rock.example.org");
  }

  @Test
  public void test_app_keeps_its_tags() {
    App app = new App("rock-1");
    app.setName("rock");
    app.setType("rock");
    app.setCluster("default");
    app.setServer("https://rock.example.org");
    app.setTags(Lists.newArrayList("r", "datashield"));

    App reloaded = roundTrip(app);

    assertThat(reloaded.getName()).isEqualTo("rock");
    assertThat(reloaded.getTags()).containsExactly("r", "datashield");
  }

  @Test
  public void test_pod_spec_keeps_its_container_labels_and_tolerations() {
    PodSpec spec = new PodSpec("rock")
        .setType("rock")
        .setNamespace("opal")
        .setLabels(ImmutableMap.of("app", "rock"))
        .setNodeSelector(ImmutableMap.of("disk", "ssd"))
        .setContainer(new Container().setName("rock").setImage("obiba/rock:latest").setPort(8085))
        .setTolerations(Lists.newArrayList(
            new Toleration().setKey("dedicated").setOperator(Toleration.TolerationOperator.Equal).setValue("opal")))
        .setEnabled(true);

    PodSpec reloaded = roundTrip(spec);

    assertThat(reloaded.getLabels()).isEqualTo(ImmutableMap.of("app", "rock"));
    assertThat(reloaded.getNodeSelector()).isEqualTo(ImmutableMap.of("disk", "ssd"));
    assertThat(reloaded.getContainer().getImage()).isEqualTo("obiba/rock:latest");
    assertThat(reloaded.getContainer().getPort()).isEqualTo(8085);
    assertThat(reloaded.getTolerations()).hasSize(1);
    Toleration toleration = reloaded.getTolerations().get(0);
    assertThat(toleration.getKey()).isEqualTo("dedicated");
    assertThat(toleration.getOperator()).isEqualTo(Toleration.TolerationOperator.Equal);
    assertThat(toleration.getValue()).isEqualTo("opal");
    assertThat(reloaded.isEnabled()).isTrue();
  }

  @Test
  public void test_keystore_state_keeps_its_bytes() {
    byte[] bytes = new byte[1024];
    for(int i = 0; i < bytes.length; i++) bytes[i] = (byte) (i % 256);

    KeyStoreState state = new KeyStoreState("cohort");
    state.setKeyStore(bytes);

    KeyStoreState reloaded = roundTrip(state);

    assertThat(reloaded.getName()).isEqualTo("cohort");
    assertThat(reloaded.getKeyStore()).isEqualTo(bytes);
  }

  @Test
  public void test_datashield_profile_keeps_its_methods_and_options() {
    DataShieldProfile profile = new DataShieldProfile("default");
    profile.setEnabled(true);
    profile.setRestrictedAccess(true);
    profile.getEnvironment(DSMethodType.AGGREGATE)
        .addOrUpdate(new DefaultDSMethod("meanDS", "dsBase::meanDS"));
    profile.addOrUpdateOption("datashield.privacyLevel", "5");

    DataShieldProfile reloaded = roundTrip(profile);

    assertThat(reloaded.getName()).isEqualTo("default");
    assertThat(reloaded.isEnabled()).isTrue();
    assertThat(reloaded.isRestrictedAccess()).isTrue();
    List<?> methods = reloaded.getEnvironment(DSMethodType.AGGREGATE).getMethods();
    assertThat(methods).hasSize(1);
    assertThat(reloaded.getEnvironment(DSMethodType.AGGREGATE).getMethod("meanDS")).isNotNull();
    assertThat(reloaded.getOption("datashield.privacyLevel").getValue()).isEqualTo("5");
  }

  @Test
  public void test_r_session_activity_keeps_the_fields_it_inherits() {
    RSessionActivity activity = new RSessionActivity();
    activity.setId("session-1");
    activity.setUser("alice");
    activity.setContext("R");
    activity.setProfile("default");
    activity.setExecutionTimeMillis(1234L);
    activity.setUpdated(new Date());

    RSessionActivity reloaded = roundTrip(activity);

    assertThat(reloaded.getId()).isEqualTo("session-1");
    assertThat(reloaded.getUser()).isEqualTo("alice");
    assertThat(reloaded.getContext()).isEqualTo("R");
    assertThat(reloaded.getProfile()).isEqualTo("default");
    assertThat(reloaded.getExecutionTimeMillis()).isEqualTo(1234L);
    assertThat(reloaded.getCreated()).isNotNull();
  }

  @Test
  public void test_timestamps_are_stored() {
    Set<String> names = Sets.newHashSet("a");
    Group group = new Group("timestamped");
    group.setSubjectCredentials(names);
    Date before = new Date(System.currentTimeMillis() - 1000);

    Group reloaded = roundTrip(group);

    assertThat(reloaded.getCreated()).isNotNull();
    assertThat(reloaded.getCreated().after(before)).isTrue();
  }
}
