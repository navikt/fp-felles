package no.nav.vedtak.felles.testutilities.cdi;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.enterprise.inject.Instance;
import javax.enterprise.util.TypeLiteral;

public class UnitTestLookupInstanceImpl<T> implements Instance<T> {

    T verdi;

    public UnitTestLookupInstanceImpl(T verdi) {
        this.verdi = verdi;
    }

    @Override
    public T get() {
        return verdi;
    }

    // Metodene her kan implementeres etter behov

    @Override
    public Instance<T> select(Annotation... annotations) {
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <U extends T> Instance<U> select(Class<U> aClass, Annotation... annotations) {
        return (Instance<U>) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <U extends T> Instance<U> select(TypeLiteral<U> typeLiteral, Annotation... annotations) {
        return (Instance<U>) this;
    }

    @Override
    public boolean isUnsatisfied() {
        return false;
    }

    @Override
    public boolean isAmbiguous() {
        return false;
    }

    @Override
    public void destroy(T t) {
    }

    @Override
    public Iterator<T> iterator() {
        return List.of(get()).iterator();
    }
}
