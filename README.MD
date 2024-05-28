# Logback  configuration production ready

This document maintains documentation for this repository in both english & french translations.
Documentation pour le 
## English version
This repository aims to exhibit a logback configuration  tailored for production.
It shows many Logback  related tricks:
* limit message size
* limit stack trace depth
* limit file size with rolling appenders (if file storage used)
* reduce the number of occurences for the same message
* activate automatic  file reload 
* and so on

The main goal is to provide a configuration file to  be used as a template by projects.
Some unit tests show how to unit tests such features (most of them).
Configuration used in tests may be the one picked from test/resources or one done programmatically.


## Version française
Ce projet cherche à fournir un modèle de fichier logback.xml destiné à la production en évitant les pièges classiques de la gestion des logs:
# fichiers trop gros et remplissant  le disque
# traces trop lourdes et trop verbeuses
# exceptions remontées trop longues
# trop nombreuses répétitions du message (inutiles donc)
# rechargement automatique de  la configuration 

La configuration  logback utilisée dans les tests peut-être celle venant du dossier test/resources (logback-test.xml) ou élaborée
par code  dans le test unitaire.