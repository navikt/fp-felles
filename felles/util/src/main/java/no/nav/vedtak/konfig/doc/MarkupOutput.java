package no.nav.vedtak.konfig.doc;

import io.github.swagger2markup.markup.builder.MarkupDocBuilder;

public interface MarkupOutput {

    void apply(int sectionLevel, MarkupDocBuilder doc);
}
