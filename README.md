# Crossref Event Data Hypothes.is Agent

Monitors the Hypothes.is annotation service for annotations to Works that have DOIs. Also looks for people who mention Works that have DOIs in the annotation.

## Tests

### Unit tests

 - `time docker-compose -f docker-compose-unit-tests.yml run -w /usr/src/app test lein test :unit`

## Demo

    time docker-compose -f docker-compose-unit-tests.yml run -w /usr/src/app test lein repl

## Config

Uses Event Data global configuration namespace.

 - `HYPOTHESIS_JWT`
 - `GLOBAL_ARTIFACT_URL_BASE`, e.g. https://artifact.eventdata.crossref.org
 - `GLOBAL_KAFKA_BOOTSTRAP_SERVERS`
 - `GLOBAL_STATUS_TOPIC`

