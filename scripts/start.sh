#!/usr/bin/env bash

realpath () {
(
  TARGET_FILE="$1"
  CHECK_CYGWIN="$2"

  cd "$(dirname "$TARGET_FILE")"
  TARGET_FILE=$(basename "$TARGET_FILE")

  COUNT=0
  while [ -L "$TARGET_FILE" -a $COUNT -lt 100 ]
  do
      TARGET_FILE=$(readlink "$TARGET_FILE")
      cd "$(dirname "$TARGET_FILE")"
      TARGET_FILE=$(basename "$TARGET_FILE")
      COUNT=$(($COUNT + 1))
  done

  if [ "$TARGET_FILE" == "." -o "$TARGET_FILE" == ".." ]; then
    cd "$TARGET_FILE"
    TARGET_FILEPATH=
  else
    TARGET_FILEPATH=/$TARGET_FILE
  fi

  # make sure we grab the actual windows path, instead of cygwin's path.
  if [[ "x$CHECK_CYGWIN" == "x" ]]; then
    echo "$(pwd -P)/$TARGET_FILE"
  else
    echo $(cygwinpath "$(pwd -P)/$TARGET_FILE")
  fi
)
}

declare -r real_script_path="$(realpath "$0")"
declare -r app_home="$(realpath "$(dirname "$real_script_path")")"

#Default arguments
NEW_RELIC=false
PORT=9000

while [[ $# > 1 ]]
do
key="$1"

case $key in
    --cache-password)
        CACHE_PASSWORD="$2"
        shift
        ;;
    --env)
        ENV="$2"
        shift
        ;;
    --port)
        PORT="$2"
        shift
        ;;
    *)

    ;;
esac
shift
done

export CACHE_PASSWORD="${cache_PASSWORD}"
#echo $CACHE_PASSWORD
export ENV="${ENV}"
#echo $ENV

if [[ $ENV = "dev" ]]; then
    $app_home/egc -Dconfig.resource=dev.conf -Dhttp.port=${PORT}
elif [[ $ENV = "qa" ]]; then
    $app_home/egc -Dconfig.resource=qa.conf -Dhttp.port=${PORT}
elif [[ $ENV = "uat" ]]; then
    $app_home/egc -Dconfig.resource=uat.conf -Dhttp.port=${PORT}
elif [[ $ENV = "prod" ]]; then
    $app_home/egc -Dconfig.resource=prod.conf -Dhttp.port=${PORT}
else
    echo "No environment specified"
    exit 1
fi;


