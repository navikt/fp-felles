package no.nav.vedtak.felles.testutilities.db;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import no.nav.vedtak.felles.jpa.TransactionHandler;
import no.nav.vedtak.felles.jpa.TransactionHandler.Work;
import no.nav.vedtak.felles.testutilities.cdi.WeldContext;
import no.nav.vedtak.felles.testutilities.sikkerhet.DummySubjectHandler;
import no.nav.vedtak.felles.testutilities.sikkerhet.SubjectHandlerUtils;

/**
 * JUnit Rule som initialiserer Repository (med PersistenceUnit og
 * EntityManager) for bruk i tester som aksessererer datalageret.
 * <p>
 * Bruk {@link EntityManagerAwareExtension} med JUnit 5.
 * 
 */
@Deprecated(forRemoval = true, since = "2.1.x")
public abstract class RepositoryRule extends PersistenceUnitInitializer implements MethodRule {

    static {
        // trenger en subject handler i alle tester som endrer i database pga at det
        // brukes i BaseEntitet ved enhver endring
        SubjectHandlerUtils.useSubjectHandler(DummySubjectHandler.class);
    }

    private boolean transaksjonell = true;

    public RepositoryRule() {
        super();
    }

    public RepositoryRule(boolean transaksjonell) {
        super();
        this.transaksjonell = transaksjonell;
    }

    private Statement adaptStatementWithTx(Statement statement, FrameworkMethod method) {
        if (!transaksjonell) {
            return statement;
        } else {
            return new Statement() {

                @Override
                public void evaluate() throws Throwable {
                    EntityTransaction transaction = startTransaction();

                    try {
                        statement.evaluate();
                    } catch (Exception e) {
                        throw e; // NOSONAR (må tillate dette pga ExceptionRule i test)
                    } finally {
                        Commit commitAnnotation = method.getAnnotation(Commit.class);
                        if (commitAnnotation != null) {
                            transaction.commit();
                        } else {
                            transaction.rollback();
                        }

                        getEntityManager().clear();
                    }
                }

                private EntityTransaction startTransaction() {
                    EntityTransaction transaction = getEntityManager().getTransaction();

                    transaction.begin();
                    return transaction;
                }
            };
        }
    }

    @Override
    public Statement apply(Statement statement, FrameworkMethod method, Object target) {

        return new Statement() {

            @Override
            public void evaluate() throws Throwable {
                WeldContext.getInstance().doWithScope(() -> {
                    Statement stmt = adaptStatementWithTx(statement, method);
                    try {
                        stmt.evaluate();
                        return null;
                    } catch (RuntimeException | Error e) {
                        throw e;
                    } catch (Throwable e) {
                        throw new IllegalStateException(e);
                    }
                });
            }

        };

    }

    public <R> R doInTransaction(EntityManager entityManager, Work<R> func) throws Exception {
        if (transaksjonell) {
            throw new IllegalStateException(getClass().getSimpleName() + " er konfigurert som transaksjonell, kan ikke starte ny");
        }
        return new TransactionHandler<R>() {

            @Override
            protected R doWork(EntityManager entityManager) throws Exception {
                return func.doWork(entityManager);
            }
        }.apply(entityManager);
    }

    /**
     * Kjør i transaksjon (når denne reglen var satt opp som ikke-transaksjonell.).
     * Starter ny transaksjon.
     */
    public <R> R doInTransaction(Work<R> func) throws Exception {
        return doInTransaction(getEntityManager(), func);
    }

    @Override
    public EntityManager getEntityManager() {
        return WeldContext.getInstance().doWithScope(super::getEntityManager);
    }

    public Repository getRepository() {
        return new Repository(getEntityManager());
    }

}
