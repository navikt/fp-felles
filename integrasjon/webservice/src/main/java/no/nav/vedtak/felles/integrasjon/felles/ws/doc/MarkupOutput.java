package no.nav.vedtak.felles.integrasjon.felles.ws.doc;

import io.github.swagger2markup.markup.builder.MarkupDocBuilder;

public interface MarkupOutput {

    void apply(int sectionLevel, MarkupDocBuilder doc);
}
