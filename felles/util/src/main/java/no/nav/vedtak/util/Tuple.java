package no.nav.vedtak.util;

@Deprecated(since = "3.1.x", forRemoval = true)
/* Bruk Pair fra org.javatuples:javatuples isteden. */
public class Tuple<X, Y> {
    private final X element1;
    private final Y element2;

    public static <X, Y> Tuple<X, Y> of(X element1, Y element2) {
        return new Tuple<>(element1, element2);
    }

    public Tuple(X element1, Y element2) {
        java.util.Objects.requireNonNull(element1);
        java.util.Objects.requireNonNull(element2);

        this.element1 = element1;
        this.element2 = element2;
    }

    public X getElement1() {
        return element1;
    }

    public Y getElement2() {
        return element2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Tuple))
            return false;

        Tuple<?, ?> tuple = (Tuple<?, ?>) o;

        return element1.equals(tuple.element1) && element2.equals(tuple.element2);
    }

    @Override
    public int hashCode() {
        int result = element1.hashCode();
        result = 31 * result + element2.hashCode();
        return result;
    }
}
