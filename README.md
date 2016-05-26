# WS Experiment
Experiment with web services

This experiment presents a simple way to develop a web service in Java EE 7 and two different approaches for creating clients.

The project uses Maven and makes use of GlassFish Embedded Plugin for running the examples making it simple to run with no extra installations.

## Server
```
mvn clean integration-test -Prun-server
```

## Web client
```
mvn clean integration-test -Prun-web-client
```

## Desktop client
```
mvn clean integration-test -Prun-desktop-client
```
