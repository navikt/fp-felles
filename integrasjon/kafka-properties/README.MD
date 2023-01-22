# Kafka - Aiven - oppsett
Denne modulen inneholder en støttemetode for oppsett av Kafka Producers og Streams + standard String-serdes for topics som bruker JSON.

Enkelte applikasjoner konsumerer AVRO-topics - de må selv legge til avhengighet til confluent.io og sette opp Schema Registry og deserialisering av Avro
