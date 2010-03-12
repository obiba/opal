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
import org.obiba.opal.shell.CommandRegistry;
import org.obiba.opal.shell.OpalShell;
import org.obiba.opal.shell.OpalShellExitCallback;
import org.obiba.opal.shell.OpalShellFactory;
import org.obiba.opal.shell.OpalShellHolder;

public class OpalSshServer {

  private final SshServer sshd;

  private final CommandRegistry commandRegistry;

  private final OpalShellHolder opalShellHolder;

  private final OpalShellFactory shellFactory;

  public OpalSshServer(CommandRegistry commandRegistry, OpalShellFactory shellFactory, OpalShellHolder opalShellHolder, Integer port) {
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
        } catch(AuthenticationException ae) {

        }
        return SecurityUtils.getSubject().isAuthenticated();
      }
    });
  }

  public void start() {
    try {
      sshd.start();
    } catch(IOException e) {
      throw new RuntimeException(e);
    }
  }

  public void stop() {
    try {
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
