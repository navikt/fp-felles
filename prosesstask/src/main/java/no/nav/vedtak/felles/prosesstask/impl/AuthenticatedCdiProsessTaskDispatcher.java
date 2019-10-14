package no.nav.vedtak.felles.prosesstask.impl;

import javax.enterprise.context.ApplicationScoped;

import no.nav.vedtak.felles.prosesstask.api.ProsessTaskData;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskHandler;
import no.nav.vedtak.felles.prosesstask.api.ProsessTaskInfo;
import no.nav.vedtak.felles.prosesstask.rest.dto.ProsessTaskSporingsloggId;
import no.nav.vedtak.log.sporingslogg.Sporingsdata;
import no.nav.vedtak.log.sporingslogg.SporingsloggHelper;
import no.nav.vedtak.sikkerhet.loginmodule.ContainerLogin;
import no.nav.vedtak.util.MdcExtendedLogContext;

/**
 * Implementerer dispatch vha. CDI scoped beans.
 */
@ApplicationScoped
public class AuthenticatedCdiProsessTaskDispatcher extends BasicCdiProsessTaskDispatcher {
    private static final MdcExtendedLogContext LOG_CONTEXT = MdcExtendedLogContext.getContext("prosess"); //$NON-NLS-1$

    public AuthenticatedCdiProsessTaskDispatcher() {
    }

    @Override
    public void dispatch(ProsessTaskData task) {
        try (ProsessTaskHandlerRef prosessTaskHandler = findHandler(task)) {
            if (task.getFagsakId() != null) {
                LOG_CONTEXT.add("fagsak", task.getFagsakId()); // NOSONAR //$NON-NLS-1$
            }
            if (task.getBehandlingId() != null) {
                LOG_CONTEXT.add("behandling", task.getBehandlingId()); // NOSONAR //$NON-NLS-1$
            }

            prosessTaskHandler.doTask(task);
            sporingslogg(task);

            // renser ikke LOG_CONTEXT her. tar alt i RunTask slik at vi kan logge exceptions også
        }

    }

    static void sporingslogg(ProsessTaskData data) {
        String action = data.getTaskType();
        Sporingsdata sporingsdata = Sporingsdata.opprett(action);

        String aktørId = data.getAktørId();
        if (aktørId != null) {
            sporingsdata.leggTilId(ProsessTaskSporingsloggId.AKTOR_ID, aktørId);
        }
        Long fagsakId = data.getFagsakId();
        if (fagsakId != null) {
            sporingsdata.leggTilId(ProsessTaskSporingsloggId.FAGSAK_ID, fagsakId);
        }
        Long behandlingId = data.getBehandlingId();
        if (behandlingId != null) {
            sporingsdata.leggTilId(ProsessTaskSporingsloggId.BEHANDLING_ID, behandlingId);
        }

        SporingsloggHelper.logSporingForTask(AuthenticatedCdiProsessTaskDispatcher.class, sporingsdata, data.getTaskType());
    }

    @SuppressWarnings("resource")
    @Override
    public ProsessTaskHandlerRef findHandler(ProsessTaskInfo task) {
        ProsessTaskHandlerRef prosessTaskHandler = super.findHandler(task);
        return new AuthenticatedProsessTaskHandlerRef(prosessTaskHandler.getBean());
    }

    private static class AuthenticatedProsessTaskHandlerRef extends ProsessTaskHandlerRef {

        private ContainerLogin containerLogin;
        private boolean successFullLogin = false;

        AuthenticatedProsessTaskHandlerRef(ProsessTaskHandler bean) {
            super(bean);
            containerLogin = new ContainerLogin();
        }

        @Override
        public void close() {
            super.close();
            if (containerLogin != null && successFullLogin) {
                containerLogin.logout();
                successFullLogin = false;
            }
        }

        @Override
        public void doTask(ProsessTaskData prosessTaskData) {
            containerLogin.login();
            successFullLogin = true;
            super.doTask(prosessTaskData);
        }

    }

}
