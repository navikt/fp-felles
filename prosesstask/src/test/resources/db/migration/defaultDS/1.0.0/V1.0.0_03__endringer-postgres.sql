-- Endringer etter at vi kopierte dette fra FP-ABAKUS
-- https://github.com/navikt/fp-felles/commit/aa13aa084e50fbfcdc99910bef0db66cf89203b5#diff-d86f672fadaf64d7ab6f428177a0fee7
alter table prosess_task
    add SISTE_KJOERING_PLUKK_TS TIMESTAMP(6);
