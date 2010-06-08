package org.obiba.opal.server.sshd;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.sshd.SshServer;
import org.apache.sshd.common.Factory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.Environment;
import org.apache.sshd.server.ExitCallback;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.session.ServerSession;
import org.obiba.opal.core.runtime.Service;
import org.obiba.opal.shell.CommandRegistry;
import org.obiba.opal.shell.OpalShell;
import org.obiba.opal.shell.OpalShellExitCallback;
import org.obiba.opal.shell.OpalShellFactory;
import org.obiba.opal.shell.OpalShellHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OpalSshServer implements Service {

  private static final Logger log = LoggerFactory.getLogger(OpalSshServer.class);

  private final SshServer sshd;

  private final CommandRegistry commandRegistry;

  private final OpalShellHolder opalShellHolder;

  private final OpalShellFactory shellFactory;

  private boolean isRunning = false;

  @Autowired
  public OpalSshServer(@Qualifier("ssh") CommandRegistry commandRegistry, OpalShellFactory shellFactory, OpalShellHolder opalShellHolder, @Value("${org.obiba.opal.ssh.port}") Integer port) {
    this.commandRegistry = commandRegistry;
    this.shellFactory = shellFactory;
    this.opalShellHolder = opalShellHolder;

    sshd = SshServer.setUpDefaultServer();
    sshd.setPort(port);
    sshd.setKeyPairProvider(new PEMGeneratorHostKeyProvider(System.getProperty("OPAL_HOME") + "/conf/sshd.pem", "RSA", 2048));
    sshd.setShellFactory(new Factory<Command>() {

      public Command create() {
        return new OpalShellCommand();
      }

    });
    sshd.setPasswordAuthenticator(new PasswordAuthenticator() {

      public boolean authenticate(String username, String password, ServerSession session) {
        try {
          SecurityUtils.getSubject().login(new UsernamePasswordToken(username, password.toCharArray(), session.getIoSession().getRemoteAddress().toString()));
          // Sessions don't expire automatically
          SecurityUtils.getSubject().getSession().setTimeout(-1);
        } catch(AuthenticationException ae) {

        }
        return SecurityUtils.getSubject().isAuthenticated();
      }
    });
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

  private class OpalShellCommand implements Command {

    private Thread thread;

    private ExitCallback exitCallback;

    private InputStream in;

    private OutputStream out;

    private OutputStream err;

    @SuppressWarnings("deprecation")
    public void destroy() {
      thread.stop();
    }

    public void setErrorStream(OutputStream err) {
      this.err = err;
    }

    public void setExitCallback(ExitCallback callback) {
      exitCallback = callback;
    }

    public void setInputStream(InputStream in) {
      this.in = in;
    }

    public void setOutputStream(OutputStream out) {
      this.out = out;
    }

    public void start(Environment env) throws IOException {
      OpalShell shell = shellFactory.newShell(commandRegistry, in, out, err);
      shell.addExitCallback(new OpalShellExitCallback() {

        public void onExit() {
          SecurityUtils.getSubject().logout();
          exitCallback.onExit(0);
        }
      });
      opalShellHolder.bind(shell);
      thread = new Thread(shell);
      thread.setDaemon(true);
      thread.start();
    }
  }

}
