package no.nav.vedtak.felles.testutilities.cdi;

import java.lang.annotation.Annotation;
import java.util.Iterator;

import javax.enterprise.inject.Instance;
import javax.enterprise.util.TypeLiteral;

public class UnitTestInstanceImpl<T> implements Instance<T> {

    T verdi;

    public UnitTestInstanceImpl(T verdi) {
        this.verdi = verdi;
    }

    @Override
    public T get() {
        return verdi;
    }

    //Metodene her kan implementeres etter behov

    @Override
    public Instance<T> select(Annotation... annotations) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <U extends T> Instance<U> select(Class<U> aClass, Annotation... annotations) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <U extends T> Instance<U> select(TypeLiteral<U> typeLiteral, Annotation... annotations) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isUnsatisfied() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isAmbiguous() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void destroy(T t) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<T> iterator() {
        throw new UnsupportedOperationException();
    }

}
