package no.nav.vedtak.felles.prosesstask.impl;

import java.util.function.Consumer;

import javax.persistence.PersistenceException;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.log.mdc.MDCOperations;
import no.nav.vedtak.util.FPDateUtil;

class TaskManagerRunnableTask implements Runnable {
    private final String taskName;
    private final RunTaskInfo taskInfo;
    private final String callId;
    private Consumer<IdentRunnable> fatalErrorSubmitFunc;

    TaskManagerRunnableTask(String taskName, RunTaskInfo taskInfo, String callId, Consumer<IdentRunnable> fatalErrorSubmitFunc) {
        this.taskName = taskName;
        this.taskInfo = taskInfo;
        this.callId = callId;
        this.fatalErrorSubmitFunc = fatalErrorSubmitFunc;
    }

    @Override
    public void run() {
        
        RunTask runSingleTask = newRunTaskInstance();
        IdentRunnable errorCallback = null;
        try {
            initLogContext(callId, taskName);

            runSingleTask.doRun(taskInfo);

        } catch (PersistenceException fatal) {
            // transaksjonen er rullet tilbake, markert for rollback eller inaktiv nå. Submitter derfor en oppdatering som en separat oppgave (gjennom
            // en callback).
            errorCallback = lagErrorCallback(taskInfo, callId, fatal);
        } catch (Exception e) {
            errorCallback = lagErrorCallback(taskInfo, callId, e);
        } catch (Throwable t) { // NOSONAR
            errorCallback = lagErrorCallback(taskInfo, callId, t);
        } finally {
            clearLogContext();
            // dispose CDI etter bruk
            TaskManagerGenerateRunnableTasks.CURRENT.destroy(runSingleTask);
            
            // kjør etter at runTask er destroyed og logcontext renset
            handleErrorCallback(errorCallback);
        }
    }
    
    IdentRunnable lagErrorCallback(final RunTaskInfo taskInfo, final String callId, final Throwable fatal) {
        Runnable errorCallback;
        errorCallback = () -> {
            final FatalErrorTask errorTask = TaskManagerGenerateRunnableTasks.CURRENT.select(FatalErrorTask.class).get();
            try {
                initLogContext(callId, taskInfo.getTaskType());
                errorTask.doRun(taskInfo, fatal);
            } catch (Throwable t) {  // NOSONAR
                // logg at vi ikke klarte å registrer feilen i db
                Feil feil = TaskManagerFeil.FACTORY.kunneIkkeLoggeUventetFeil(taskInfo.getId(), taskInfo.getTaskType(), t);
                feil.log(TaskManagerGenerateRunnableTasks.log);
            } finally {
                clearLogContext();
                TaskManagerGenerateRunnableTasks.CURRENT.destroy(errorTask);
            }
        };

        // logg at vi kommer til å skrive dette i ny transaksjon pga fatal feil.
        TaskManagerFeil.FACTORY.kritiskFeilKunneIkkeProsessereTaskPgaFatalFeil(taskInfo.getId(), taskInfo.getTaskType(), fatal)
            .log(TaskManagerGenerateRunnableTasks.log);

        return new IdentRunnableTask(taskInfo.getId(), errorCallback, FPDateUtil.nå());
    }

    void clearLogContext() {
        TaskManagerGenerateRunnableTasks.LOG_CONTEXT.clear();
        MDCOperations.removeCallId();
    }

    void initLogContext(final String callId, String taskName) {
        if (callId != null) {
            MDCOperations.putCallId(callId);
        } else {
            MDCOperations.putCallId();
        }
        TaskManagerGenerateRunnableTasks.LOG_CONTEXT.add("task", taskName); //$NON-NLS-1$
    }
    
    void handleErrorCallback(IdentRunnable errorCallback) {
        if (errorCallback != null) {
            // NB - kjøres i annen transaksjon enn opprinnelig
            fatalErrorSubmitFunc.accept(errorCallback);
        }
    }

    RunTask newRunTaskInstance() {
        return TaskManagerGenerateRunnableTasks.CURRENT.select(RunTask.class).get();
    }

}