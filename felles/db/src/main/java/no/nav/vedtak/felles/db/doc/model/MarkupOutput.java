package no.nav.vedtak.felles.db.doc.model;

import io.github.swagger2markup.markup.builder.MarkupDocBuilder;

public interface MarkupOutput {

    void apply(int sectionLevel, MarkupDocBuilder doc);
}
