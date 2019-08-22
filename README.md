# Felles kode / bibliotek for foredrepenger området



## Postgres
I test og utvikling bruker vi postgres. For å enkelt sette opp dette slik at det fungerer med unit-tests:
```
docker rm -f fp-postgres
docker run --rm --name fp-postgres -e POSTGRES_USER=fp_unit -e POSTGRES_PASSWORD=fp_unit -e POSTGRES_DB=fp_unit  -d -p 5432:5432 postgres:alpine
```
