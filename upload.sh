#!/bin/bash
VERSION_POSTFIX="$1"

function deployArtifact() {
  POM_PATH="$1"
  VERSION="$2"
  POM_FOLDER="$(echo ${POM_PATH} | sed "s/flattened//")"
  if [[ -d "${POM_FOLDER}/target/" && $(ls "${POM_FOLDER}/target/." | grep "\-${VERSION}.jar" | wc -l | xargs) -gt 0 ]]; then
    toBeReplaced=".jar"
    sourcesPostfix="-sources.jar"
    ls "${POM_FOLDER}/target/." | grep "\-${VERSION}.jar" | xargs -L1 -I{} mvn deploy:deploy-file -q -s ./.m2/settings.xml -DrepositoryId=github -Dfile="${POM_FOLDER}/target/"{} -DsourceFile="${POM_FOLDER}/target/${{}/$toBeReplaced/$sourcesPostfix}" -DpomFile="${POM_PATH}" -Durl=https://maven.pkg.github.com/navikt/fp-felles
  else
    mvn deploy:deploy-file -q -s ./.m2/settings.xml -DrepositoryId=github -Dfile="${POM_PATH}" -DpomFile="${POM_PATH}" -Durl=https://maven.pkg.github.com/navikt/fp-felles
  fi

}

export -f deployArtifact
export VERSION_POSTFIX
find . -name ".flattened" -exec bash -c 'deployArtifact "$0" "$1"' {} "${VERSION_POSTFIX}" \;
