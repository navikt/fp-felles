package no.nav.vedtak.openapi;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.swagger.v3.core.jackson.TypeNameResolver;

/**
 * Special TypeNameResolver that always resolves to fully qualified type names, but strips given prefix from resolved name.
 * <br>
 * This lets us strip a common package name from resolved names to cut down on the length when the name is still unique
 * among all classes that is resolved through this instance.
 */
public class PrefixStrippingFQNTypeNameResolver extends TypeNameResolver {
    private final Map<String, Class<?>> previouslyResolvedTo = new HashMap<>();
    private final List<String> stripPrefixes;

    /**
     * Resolver will attempt to remove given stripPrefixes in the order given. If a given prefix is a match, it will not
     * try removing later prefixes. So if one has multiple prefixes starting with the same string, the most specific
     * should be first in the list.
     */
    public PrefixStrippingFQNTypeNameResolver(final List<String> stripPrefixes) {
        super();
        this.setUseFqn(true);
        this.stripPrefixes = Objects.requireNonNull(stripPrefixes, "stripPrefixes must be specified");
    }

    public PrefixStrippingFQNTypeNameResolver(final String ...stripPrefixes) {
        this(List.of(stripPrefixes));
    }

    private boolean hasPreviouslyResolvedToOtherClass(final String name, final Class<?> cls) {
        final var prevCls = this.previouslyResolvedTo.get(name);
        if (cls != null && prevCls != null) {
            return !prevCls.equals(cls);
        }
        return false;
    }

    private boolean hasNotPreviouslyResolvedToOtherClass(final String name, final Class<?> cls) {
        return !this.hasPreviouslyResolvedToOtherClass(name, cls);
    }

    private String strippedName(final String fqn, final Class<?> cls) {
        for(final var stripPrefix : this.stripPrefixes) {
            if (fqn.startsWith(stripPrefix)) {
                final var stripped = fqn.substring(stripPrefix.length());
                if (this.hasNotPreviouslyResolvedToOtherClass(stripped, cls)) {
                    return stripped;
                }
            }
        }
        return fqn; // No match
    }

    @Override
    protected String getNameOfClass(Class<?> cls) {
        final String name = this.strippedName(super.getNameOfClass(cls), cls);
        if (this.hasPreviouslyResolvedToOtherClass(name, cls)) { // Should not really ever happen
            final Class<?> otherClass = this.previouslyResolvedTo.get(name);
            if (otherClass != null) {
                throw new IllegalArgumentException("Type name \"" + name + "\", (for class"+ cls.getName() + ") has previously resolved to another class (" + otherClass.getName() + ")");
            } else {
                throw new IllegalArgumentException("Type name \"" + name + "\", (for class"+ cls.getName() + ") has previously resolved to another class");
            }
        } else {
            this.previouslyResolvedTo.put(name, cls);
            return name;
        }
    }
}
