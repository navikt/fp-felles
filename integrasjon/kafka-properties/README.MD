# Kafka - Aiven - oppsett
Denne modulen inneholder støttemetoder for oppsett av Kafka Producers og Consumer + String-standard for topics som bruker JSON.

Producer:
* Enten opprett en KafkaSender for hver topic og bruk send med key + message
* Eller opprett global KafkaSender med topic = null og bruk send med key + message + topic

Consumers
* Håndterere (ofte Transactional) implementerer KafkaMessageHandler / KafkaStringMessageHandler
* Applikasjonene definerer en Controllable som lager en KafkaConsumerManager av handlere og starter/stopper den

Enkelte applikasjoner konsumerer AVRO-topics - de må selv legge til avhengighet til confluent.io og sette opp Schema Registry og deserialisering av Avro
