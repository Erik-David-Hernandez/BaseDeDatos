MVN ?= mvn
MVN_ARGS ?=
POM := Biblioteca/biblioteca/pom.xml
TYPE ?= app-image

.DEFAULT_GOAL := help
.PHONY: help build run package test clean setup

help:
	@printf '%s\n' \
	  'make build                 Build the Java JAR' \
	  'make run                   Run the JavaFX application' \
	  'make package               Build a native application image' \
	  'make package TYPE=deb      Build an installer (deb/rpm/exe/dmg)' \
	  'make test                  Run Maven tests' \
	  'make clean                 Remove build output' \
	  'make setup                 Prepare PostgreSQL for the application' \
	  'Override MVN or MVN_ARGS to customize Maven invocation.'

build:
	$(MVN) $(MVN_ARGS) -f "$(POM)" clean package

run:
	$(MVN) $(MVN_ARGS) -f "$(POM)" javafx:run

package:
	$(MVN) $(MVN_ARGS) -f "$(POM)" -Pnative clean verify -Djpackage.type="$(TYPE)"

test:
	$(MVN) $(MVN_ARGS) -f "$(POM)" test

clean:
	$(MVN) $(MVN_ARGS) -f "$(POM)" clean

setup:
	bash database/setup-postgresql.sh --app
