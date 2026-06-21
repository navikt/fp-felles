package no.nav.vedtak.server.jetty;

import org.eclipse.jetty.util.component.LifeCycle;

public class DataSourceShutdownListener implements LifeCycle.Listener {

        private final Runnable action;

        public DataSourceShutdownListener(Runnable action) {
            this.action = action;
        }

        @Override
        public void lifeCycleStopped(LifeCycle event) {
            if (action != null) {
                action.run();
            }
        }
    }
