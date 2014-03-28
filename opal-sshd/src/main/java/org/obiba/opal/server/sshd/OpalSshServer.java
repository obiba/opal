package org.obiba.opal.server.sshd;

import java.io.IOException;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.apache.sshd.SshServer;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.common.Session;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.FileSystemFactory;
import org.apache.sshd.server.FileSystemView;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.session.ServerSession;
import org.apache.sshd.server.sftp.SftpSubsystem;
import org.obiba.opal.core.cfg.OpalConfigurationExtension;
import org.obiba.opal.core.runtime.NoSuchServiceConfigurationException;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.core.runtime.Service;
import org.obiba.opal.core.service.SubjectProfileService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableList;

@Component
public class OpalSshServer implements Service {

  private static final Logger log = LoggerFactory.getLogger(OpalSshServer.class);

  private final SshServer sshd;

  @Autowired
  private OpalRuntime opalRuntime;

  @Autowired
  private SubjectProfileService subjectProfileService;

  private boolean isRunning = false;

  @Autowired
  public OpalSshServer(@Value("${org.obiba.opal.ssh.port}") Integer port) {

    sshd = SshServer.setUpDefaultServer();
    sshd.setPort(port);
    sshd.setKeyPairProvider(
        new PEMGeneratorHostKeyProvider(System.getProperty("OPAL_HOME") + "/conf/sshd.pem", "RSA", 2048));
    sshd.setPasswordAuthenticator(new PasswordAuthenticator() {

      @Override
      public boolean authenticate(String username, String password, ServerSession session) {
        try {
          Subject subject = SecurityUtils.getSubject();
          subject.login(new UsernamePasswordToken(username, password.toCharArray(),
              session.getIoSession().getRemoteAddress().toString()));
          ensureProfile(subject);
          // Sessions don't expire automatically
          SecurityUtils.getSubject().getSession().setTimeout(-1);
        } catch(AuthenticationException ae) {
          return false;
        }
        return SecurityUtils.getSubject().isAuthenticated();
      }

      private void ensureProfile(Subject subject) {
        Object principal = subject.getPrincipal();

        if(!subjectProfileService.supportProfile(principal)) {
          return;
        }
        subjectProfileService.ensureProfile(subject.getPrincipals());
      }
    });
    sshd.setFileSystemFactory(new FileSystemFactory() {

      @Override
      public FileSystemView createFileSystemView(Session session) throws IOException {
        return new OpalFileSystemView(opalRuntime, session.getUsername());
      }
    });
    sshd.setSubsystemFactories(ImmutableList.<NamedFactory<Command>>of(new SftpSubsystem.Factory()));
  }

  @Override
  public boolean isRunning() {
    return isRunning;
  }

  @Override
  public void start() {
    try {
      log.info("Starting Opal SSH Server on port {}", sshd.getPort());
      sshd.start();
      isRunning = true;
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void stop() {
    try {
      isRunning = false;
      sshd.stop(true);
    } catch(InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public String getName() {
    return "ssh";
  }

  @Override
  public OpalConfigurationExtension getConfig() throws NoSuchServiceConfigurationException {
    throw new NoSuchServiceConfigurationException(getName());
  }

}
