package no.nav.vedtak.felles.jpa;

import javax.enterprise.util.AnnotationLiteral;

/**
 * For programmatisk oppslag av beans vha. CDI
 */
public class VLPersistenceUnitLiteral extends AnnotationLiteral<VLPersistenceUnit> implements VLPersistenceUnit {

    static final String DEFAULT_KODE = "pu-default";

    final String kode;

    public VLPersistenceUnitLiteral() {
        this(DEFAULT_KODE);
    }

    public VLPersistenceUnitLiteral(String kode) {
        this.kode = kode;
    }

    @Override
    public String value() {
        return kode;
    }

}