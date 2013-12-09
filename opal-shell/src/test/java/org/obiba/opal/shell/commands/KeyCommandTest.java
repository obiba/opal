/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.shell.commands;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.easymock.EasyMock;
import org.junit.Test;
import org.obiba.core.util.FileUtil;
import org.obiba.opal.core.domain.Project;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.core.security.OpalKeyStore;
import org.obiba.opal.core.service.ProjectService;
import org.obiba.opal.core.service.security.ProjectsKeyStoreService;
import org.obiba.opal.fs.OpalFileSystem;
import org.obiba.opal.shell.OpalShell;
import org.obiba.opal.shell.commands.options.KeyCommandOptions;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.replay;

/**
 * Unit tests for {@link KeyCommand}.
 */
public class KeyCommandTest {

  private static final String[] CERTIFICATE_INFO = { "Bob Friendly", "Department of Bob", "Bob Inc.", "Montreal",
      "Quebec", "CA" };

  @Test
  public void testCreateActionCreatesOrUpdatesKey() {
    KeyCommandOptions options = createMockOptionsForCreateAction("my-unit", "my-alias");

    OpalRuntime mockRuntime = createMockRuntime();

    OpalShell mockShell = createMockShellForCreateAction("my-alias");
    Project project = new Project("my-unit");
    ProjectService projectService = createMock(ProjectService.class);
    expect(projectService.getProject("my-unit")).andReturn(project).atLeastOnce();

    ProjectsKeyStoreService projectsKeyStoreService = createMock(ProjectsKeyStoreService.class);

    expect(projectsKeyStoreService.aliasExists(project, "my-alias")).andReturn(false).atLeastOnce();
    projectsKeyStoreService.createOrUpdateKey(project, "my-alias", "RSA", 2048, getCertificateInfoAsString());
    expectLastCall().atLeastOnce();

    replay(options, mockRuntime, mockShell, projectsKeyStoreService, projectService);

    KeyCommand keyCommand = createKeyCommand(mockRuntime, projectsKeyStoreService, projectService);
    keyCommand.setOptions(options);
    keyCommand.setShell(mockShell);
    keyCommand.setProjectsKeyStoreService(projectsKeyStoreService);
    keyCommand.execute();
  }

  @Test
  public void testDeleteActionDeletesKey() {
    KeyCommandOptions mockOptions = createMockOptionsForDeleteAction("my-unit", "my-alias");

    OpalRuntime mockRuntime = createMockRuntime();

    Project project = new Project("my-unit");
    ProjectService projectService = createMock(ProjectService.class);
    expect(projectService.getProject("my-unit")).andReturn(project).atLeastOnce();

    OpalShell mockShell = createMockShellForDeleteAction("my-unit", "my-alias");

    ProjectsKeyStoreService projectsKeyStoreService = createMock(ProjectsKeyStoreService.class);
    expect(projectsKeyStoreService.aliasExists(project, "my-alias")).andReturn(true).atLeastOnce();
    projectsKeyStoreService.deleteKeyStore(project, "my-alias");
    expectLastCall().atLeastOnce();

    replay(mockOptions, mockRuntime, mockShell, projectsKeyStoreService, projectService);

    KeyCommand keyCommand = createKeyCommand(mockRuntime, projectsKeyStoreService, projectService);
    keyCommand.setOptions(mockOptions);
    keyCommand.setShell(mockShell);
    keyCommand.setProjectsKeyStoreService(projectsKeyStoreService);
    keyCommand.execute();
  }

  @Test
  @SuppressWarnings({ "OverlyLongMethod", "PMD.NcssMethodCount" })
  public void testImportActionImportsKey() throws FileSystemException {
    KeyCommandOptions mockOptions = createMockOptionsForImportAction("my-unit", "my-alias", "private.pem",
        "certificate.pem");

    FileObject privateFile = createMockFile("private.pem", true, true);
    FileObject certificateFile = createMockFile("certificate.pem", true, true);

    FileObject mockFileSystemRoot = createMockFileSystemRoot();
    expect(mockFileSystemRoot.resolveFile("private.pem")).andReturn(privateFile).atLeastOnce();
    expect(mockFileSystemRoot.resolveFile("certificate.pem")).andReturn(certificateFile).atLeastOnce();

    OpalFileSystem mockFileSystem = createMockFileSystem(mockFileSystemRoot);

    OpalRuntime mockRuntime = createMockRuntime();
    expect(mockRuntime.getFileSystem()).andReturn(mockFileSystem).atLeastOnce();

    Project project = new Project("my-unit");
    ProjectService projectService = createMock(ProjectService.class);
    expect(projectService.getProject("my-unit")).andReturn(project).atLeastOnce();

    OpalShell mockShell = createMockShellForImportAction("my-alias");

    ProjectsKeyStoreService projectsKeyStoreService = createMock(ProjectsKeyStoreService.class);
    expect(projectsKeyStoreService.aliasExists(project, "my-alias")).andReturn(false).atLeastOnce();
    projectsKeyStoreService.importKey(project, "my-alias", privateFile, certificateFile);
    expectLastCall().atLeastOnce();

    replay(mockOptions, mockFileSystemRoot, mockFileSystem, mockRuntime, mockShell, projectsKeyStoreService,
        projectService);

    KeyCommand keyCommand = createKeyCommand(mockRuntime, projectsKeyStoreService, projectService);
    keyCommand.setOptions(mockOptions);
    keyCommand.setShell(mockShell);
    keyCommand.setProjectsKeyStoreService(projectsKeyStoreService);
    keyCommand.execute();
  }

  @Test
  @SuppressWarnings({ "OverlyLongMethod", "PMD.NcssMethodCount" })
  public void testExportActionExportsKey() throws IOException, GeneralSecurityException {
    KeyCommandOptions mockOptions = createMockOptionsForExportAction("my-unit", "my-alias", "certificate.pem");

    FileContent mockFileContent = createMock(FileContent.class);
    expect(mockFileContent.getOutputStream()).andReturn(new ByteArrayOutputStream()).atLeastOnce();

    FileObject certificateFile = createMockFile("certificate.pem", true, false);
    expect(certificateFile.getContent()).andReturn(mockFileContent).atLeastOnce();

    FileObject mockFileSystemRoot = createMockFileSystemRoot();
    expect(mockFileSystemRoot.resolveFile("certificate.pem")).andReturn(certificateFile).atLeastOnce();

    OpalFileSystem mockFileSystem = createMockFileSystem(mockFileSystemRoot);

    OpalShell mockShell = createMockShellForExportAction("certificate.pem");
    mockShell.printf((String) EasyMock.anyObject(), (String) EasyMock.anyObject());
    EasyMock.expectLastCall().anyTimes();

    Project project = new Project("my-unit");
    ProjectService projectService = createMock(ProjectService.class);
    expect(projectService.getProject("my-unit")).andReturn(project).atLeastOnce();

    ProjectsKeyStoreService projectsKeyStoreService = createMock(ProjectsKeyStoreService.class);
    expect(projectsKeyStoreService.getKeyStore(project)).andReturn(new OpalKeyStore("my-unit", getKeyStore()))
        .atLeastOnce();

    OpalRuntime mockRuntime = createMockRuntime();
    expect(mockRuntime.getFileSystem()).andReturn(mockFileSystem).atLeastOnce();

    replay(mockOptions, mockFileSystemRoot, mockFileSystem, certificateFile, mockFileContent, mockRuntime, mockShell,
        projectsKeyStoreService, projectService);

    KeyCommand keyCommand = createKeyCommand(mockRuntime, projectsKeyStoreService, projectService);
    keyCommand.setOptions(mockOptions);
    keyCommand.setShell(mockShell);
    keyCommand.setProjectsKeyStoreService(projectsKeyStoreService);
    keyCommand.execute();
  }

  private KeyCommand createKeyCommand(OpalRuntime mockRuntime, ProjectsKeyStoreService projectsKeyStoreService,
      ProjectService projectService) {
    KeyCommand keyCommand = new KeyCommand();
    keyCommand.setOpalRuntime(mockRuntime);
    keyCommand.setProjectsKeyStoreService(projectsKeyStoreService);
    keyCommand.setProjectService(projectService);
    return keyCommand;
  }

  private KeyCommandOptions createMockOptionsForCreateAction(String unitName, String alias) {
    KeyCommandOptions mockOptions = createMock(KeyCommandOptions.class);
    expect(mockOptions.isUnit()).andReturn(true).atLeastOnce();
    expect(mockOptions.getUnit()).andReturn(unitName).atLeastOnce();
    expect(mockOptions.isAlias()).andReturn(true).atLeastOnce();
    expect(mockOptions.getAlias()).andReturn(alias).atLeastOnce();
    expect(mockOptions.getAction()).andReturn("create").atLeastOnce();
    expect(mockOptions.isAlgorithm()).andReturn(true).atLeastOnce();
    expect(mockOptions.getAlgorithm()).andReturn("RSA").atLeastOnce();
    expect(mockOptions.isSize()).andReturn(true).atLeastOnce();
    expect(mockOptions.getSize()).andReturn(2048).atLeastOnce();
    return mockOptions;
  }

  private OpalRuntime createMockRuntime() {
    return createMock(OpalRuntime.class);
  }

  private OpalShell createMockShellForCreateAction(String alias) {
    OpalShell mockShell = createMock(OpalShell.class);
    mockShell.printf("%s:\n", "Certificate creation");
    mockShell.printf(" %s\n", "What is your first and last name?");
    expect(mockShell.prompt("  [Unknown]:  ")).andReturn(CERTIFICATE_INFO[0]);
    mockShell.printf(" %s\n", "What is the name of your organizational unit?");
    expect(mockShell.prompt("  [Unknown]:  ")).andReturn(CERTIFICATE_INFO[1]);
    mockShell.printf(" %s\n", "What is the name of your organization?");
    expect(mockShell.prompt("  [Unknown]:  ")).andReturn(CERTIFICATE_INFO[2]);
    mockShell.printf(" %s\n", "What is the name of your City or Locality?");
    expect(mockShell.prompt("  [Unknown]:  ")).andReturn(CERTIFICATE_INFO[3]);
    mockShell.printf(" %s\n", "What is the name of your State or Province?");
    expect(mockShell.prompt("  [Unknown]:  ")).andReturn(CERTIFICATE_INFO[4]);
    mockShell.printf(" %s\n", "What is the two-letter country code for this unit?");
    expect(mockShell.prompt("  [Unknown]:  ")).andReturn(CERTIFICATE_INFO[5]);
    mockShell
        .printf(" %s\n", "Is CN=Bob Friendly, OU=Department of Bob, O=Bob Inc., L=Montreal, ST=Quebec, C=CA correct?");
    expect(mockShell.prompt("  [no]:  ")).andReturn("yes");
    mockShell.printf("Key generated with alias '%s'.\n", alias);

    return mockShell;
  }

  private String getCertificateInfoAsString() {
    return "CN=" + CERTIFICATE_INFO[0] + ", OU=" + CERTIFICATE_INFO[1] + ", O=" + CERTIFICATE_INFO[2] + ", L=" +
        CERTIFICATE_INFO[3] + ", ST=" + CERTIFICATE_INFO[4] + ", C=" + CERTIFICATE_INFO[5];
  }

  private KeyCommandOptions createMockOptionsForDeleteAction(String unitName, String alias) {
    KeyCommandOptions mockOptions = createMock(KeyCommandOptions.class);
    expect(mockOptions.isUnit()).andReturn(true).atLeastOnce();
    expect(mockOptions.getUnit()).andReturn(unitName).atLeastOnce();
    expect(mockOptions.getAlias()).andReturn(alias).atLeastOnce();
    expect(mockOptions.isAlias()).andReturn(true).atLeastOnce();
    expect(mockOptions.getAction()).andReturn("delete").atLeastOnce();

    return mockOptions;
  }

  private OpalShell createMockShellForDeleteAction(String unitName, String alias) {
    OpalShell mockShell = createMock(OpalShell.class);
    mockShell.printf("Deleted key with alias '%s' from keystore '%s'.\n", alias, unitName);

    return mockShell;
  }

  private KeyCommandOptions createMockOptionsForImportAction(String unitName, String alias, String privateFile,
      String certificateFile) {
    KeyCommandOptions mockOptions = createMock(KeyCommandOptions.class);
    expect(mockOptions.isUnit()).andReturn(true).atLeastOnce();
    expect(mockOptions.getUnit()).andReturn(unitName).atLeastOnce();
    expect(mockOptions.getAlias()).andReturn(alias).atLeastOnce();
    expect(mockOptions.isAlias()).andReturn(true).atLeastOnce();
    expect(mockOptions.getAction()).andReturn("import").atLeastOnce();
    expect(mockOptions.isPrivate()).andReturn(true).atLeastOnce();
    expect(mockOptions.getPrivate()).andReturn(privateFile).atLeastOnce();
    expect(mockOptions.isCertificate()).andReturn(true).atLeastOnce();
    expect(mockOptions.getCertificate()).andReturn(certificateFile).atLeastOnce();

    return mockOptions;
  }

  private OpalShell createMockShellForImportAction(String alias) {
    OpalShell mockShell = createMock(OpalShell.class);
    mockShell.printf("Key imported with alias '%s'.\n", alias);

    return mockShell;
  }

  private KeyCommandOptions createMockOptionsForExportAction(String unitName, String alias, String certificateFile) {
    KeyCommandOptions mockOptions = createMock(KeyCommandOptions.class);
    expect(mockOptions.isUnit()).andReturn(true).atLeastOnce();
    expect(mockOptions.getUnit()).andReturn(unitName).atLeastOnce();
    expect(mockOptions.getAlias()).andReturn(alias).atLeastOnce();
    expect(mockOptions.isAlias()).andReturn(true).atLeastOnce();
    expect(mockOptions.getAction()).andReturn("export").atLeastOnce();
    expect(mockOptions.isCertificate()).andReturn(true).atLeastOnce();
    expect(mockOptions.isPrivate()).andReturn(false).atLeastOnce();
    expect(mockOptions.getCertificate()).andReturn(certificateFile).atLeastOnce();

    return mockOptions;
  }

  private OpalShell createMockShellForExportAction(String certificateFile) {
    OpalShell mockShell = createMock(OpalShell.class);
    mockShell.printf("Certificate written to the file [%s]\n", certificateFile);

    return mockShell;
  }

  private OpalFileSystem createMockFileSystem(FileObject mockFileSystemRoot) {
    OpalFileSystem mockFileSystem = createMock(OpalFileSystem.class);
    expect(mockFileSystem.getRoot()).andReturn(mockFileSystemRoot).atLeastOnce();

    return mockFileSystem;
  }

  private FileObject createMockFileSystemRoot() {
    return createMock(FileObject.class);
  }

  private FileObject createMockFile(String baseName, boolean exists, boolean withReplay) throws FileSystemException {
    FileObject mockFile = createMock(FileObject.class);
    expect(mockFile.exists()).andReturn(exists).anyTimes();
    expect(mockFile.getName()).andReturn(createMockFileName(baseName)).anyTimes();

    if(withReplay) {
      replay(mockFile);
    }

    return mockFile;
  }

  private FileName createMockFileName(String baseName) {
    FileName mockFileName = createMock(FileName.class);
    expect(mockFileName.getBaseName()).andReturn(baseName).atLeastOnce();

    replay(mockFileName);

    return mockFileName;
  }

  private KeyStore getKeyStore() throws IOException, GeneralSecurityException {
    KeyStore keyStore = KeyStore.getInstance("JKS");

    FileInputStream fis = null;
    try {
      fis = new FileInputStream(FileUtil.getFileFromResource("KeyCommandTest/opal.jks"));
      keyStore.load(fis, "password".toCharArray());
    } finally {
      if(fis != null) {
        fis.close();
      }
    }

    return keyStore;
  }
}
