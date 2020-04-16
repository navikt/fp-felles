# Sensu klient

Modulen integrerer mot Sensu tilgjengelig i NAIS cluster. 
https://doc.nais.io/observability/metrics#push-metrics
Metrikkene som sendes over lagres i influxdb og kan brukes i grafana.

## Oppsett
Applikasjonen som skal bruke klienten må tilgjengeliggjøre to properties
```yaml
SENSU_HOST=sensu.nais
SENSU_PORT=3030
```
hvis verdiene ikke settes i applikasjonenne vil default verdier brukt.

## Viktig 
Det finnes kun enn sensu instans delt mellom prod og dev. 
Derfor er det viktig at data som sendes over representerer også hvor data kommer fra.
Dette gjøres automatisk av SensuEvent ved å legge til cluster, namespace og application taggene inn i requesten.  
