package org.obiba.opal.web.gwt.rest.client;

import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import org.obiba.opal.web.model.client.opal.GeneralConf;

/**
 * Singleton that keeps track of Opal REST calls and session timeout.
 * It uses a timer with a delay equal to the session timeout.
 * Every time a REST call is made, the timer is reset.
 * When timer reaches 0, browser location is set to login page.
 */
public class UserSessionTracker {

    private static UserSessionTracker INSTANCE = new UserSessionTracker();

    private Timer timeoutTimer = null;
    private Long clientSessionTimeout;

    public static final UserSessionTracker getInstance() {
        return INSTANCE;
    }

    private UserSessionTracker() {

    }

    public void configure() {
        if (timeoutTimer != null) {
            resetTimer(true);
        } else {
            requestClientSessionTimeout();
        }
    }

    private void resetTimer(boolean restart) {
        if (timeoutTimer != null) {
            timeoutTimer.cancel();
            timeoutTimer = null;
        }

        if (clientSessionTimeout != null && restart) {
            timeoutTimer = new Timer() {
                @Override
                public void run() {
                    timedOut();
                }
            };
            timeoutTimer.schedule(clientSessionTimeout.intValue());
        }
    }

    private void timedOut() {
        Window.Location.assign("/");
    }

    public void sessionTouched() {
        resetTimer(true);
    }

    private void handleGeneralConf(GeneralConf conf) {
        if (conf.hasClientSessionTimeout()) {
            clientSessionTimeout = (long)conf.getClientSessionTimeout();
        } else {
            clientSessionTimeout = null;
        }
    }

    private void requestClientSessionTimeout() {
        ResourceRequestBuilderFactory.<GeneralConf>newBuilder()
                .forResource(UriBuilders.SYSTEM_CONF_GENERAL.create().build())
                .get()
                .withCallback(new ResourceCallback<GeneralConf>() {
                    @Override
                    public void onResource(Response response, GeneralConf resource) {
                        handleGeneralConf(resource);
                    }
                })
                .send();
    }

}
