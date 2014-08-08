package org.obiba.opal.core.support;

import java.util.Date;

/**
 * Created by carlos on 8/8/14.
 */
public class TestMessageListener implements MessageListener {

    //private List<String> collected = new ArrayList<>();

    @Override
    public void debug(String msgFormat, Object... args) {
        add("DEBUG", msgFormat, args);
    }

    @Override
    public void info(String msgFormat, Object... args) {
        add("INFO", msgFormat, args);
    }

    @Override
    public void warn(String msgFormat, Object... args) {
        add("WARN", msgFormat, args);
    }

    @Override
    public void error(String msgFormat, Object... args) {
        add("ERROR", msgFormat, args);
    }

    private void add(String type, String msgFormat, Object... args) {
        String msg = msgFormat.format(msgFormat, args);
        //System.out.println(msg);
        String fullMsg = String.format("%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS.%1$tL %2$s %3$s", new Date(), type, msg);
        System.out.println(fullMsg);
        //collected.add(fullMsg);
    }

}
