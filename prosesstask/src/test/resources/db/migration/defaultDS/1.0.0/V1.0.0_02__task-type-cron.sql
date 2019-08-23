-- Kopiert fra: https://github.com/navikt/fp-abakus/blob/master/migreringer/src/main/resources/db/migration/defaultDS/1.0/V1.1_09__task-type-cron.sql
ALTER TABLE PROSESS_TASK_TYPE
    ADD COLUMN CRON_EXPRESSION VARCHAR(200) NULL;

COMMENT ON COLUMN PROSESS_TASK_TYPE.CRON_EXPRESSION IS 'Cron-expression for når oppgaven skal kjøres på nytt';

INSERT INTO PROSESS_TASK_FEILHAND (kode, navn, beskrivelse, opprettet_tid, endret_av, endret_tid, input_variabel1, input_variabel2) VALUES ('DEFAULT', 'Eksponentiell back-off med tak', null, current_date, null, null, null, null);
