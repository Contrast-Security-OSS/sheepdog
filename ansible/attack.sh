#! /bin/bash

# trap ctrl-c and call ctrl_c()
trap ctrl_c INT

function ctrl_c() {
        echo "** Trapped CTRL-C"
        if [ -n $pid1 ]; then kill $pid1; fi
        if [ -n $pid2 ]; then kill $pid2; fi
        exit 1
}

function webgoat {
java -javaagent:contrast.jar -Dcontrast.dir=working -Dcontrast.override.appname="$app" -Dcontrast.path="/$path" -Dcontrast.server="$server" -Dcontrast.log.daily=true -Dcontrast.level=info -jar webgoat-container-7.0.1-war-exec.jar > /dev/null 2>&1 &
}

function sheepdog {
java -jar sheepdog-1.0-SNAPSHOT.jar -t 2 -s 3600 -d 1500 250 -a 90 > /dev/null 2>&1 &

}

declare -a configs=(
    "WebGoat7|DEV-42|TEST-113|STAGE-1002|PROD-1234"
    "OracleFS|DEV-1382|TEST-2002|PROD-9190"
    "JSPWiki|DEV-992|TEST-113|PROD-99"
    "EnterpriseTPS|DEV-42|STAGE-1382|PROD-1234"
    "Liferay|DEV-1382|STAGE-1002|PROD-9190"
    "Anertix|DEV-992|TEST-2002|PROD-99" )

for config in "${configs[@]}"
do
    IFS='|' read -a item <<<"$config"
    app=${item[0]}

    for ((i = 1; i < ${#item[@]}; i++))
    do
        server=${item[i]}
        path=$(echo "/$app" | tr '[:upper:]' '[:lower:]')
        echo "Starting $app on $server using $path"
        webgoat
        pid1=$!
        sleep 60
        sheepdog
        pid2=$!
        sleep 120
        kill -KILL $pid1
        kill -KILL $pid2
        echo "----------------"
    done

done
