# felles-testutilities

## Hensikten

Samle felles kode som kan brukes i enhetstester, integrasjonstester og lokal kjøring med Jetty.
 
Skal BARE brukes i test.

## CDI lifecycle

`CdiAwareExtension` and `EntityManagerAwareExtension` share a request context
owned by the JUnit extension store. Transaction completion runs before store
cleanup destroys injected dependents and the request context. An already active
request context remains owned by its original caller.

The Weld container is closed with the JUnit root store. `WeldContext.getBean`
still creates synthetic instances, including classes that are not discovered
beans; these instances are destroyed when the container is closed.

## Brukes av

* Andre moduler
* Vedtaksløsningen 
