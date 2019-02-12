# Aktør klient

Modulen inneholder klientbibliotek mot aktør-tjenesten. Aktør-tjenesten tilbyr tjenesten for å mappe mellom AktørId og fødselsnummer, og mellom fødselsnummer og AktørId.

## Hensikten

* Finn fødselsnumer for aktørId.
* Finn aktørId for fødselsnummer.

## Brukes av

Vedtaksløsningen bruker internt AktørId, og denne modulen brukes mange steder hvor der er behov for mapping mellom AktørId og fødselsnummer.

## Integrasjoner

* tjeneste_v3:virksomhet:Aktoer_v2 (Aktørtjenesten)