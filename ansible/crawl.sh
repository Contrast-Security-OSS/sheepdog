#! /bin/bash

# The port which webgoat will use
wgport=${SHEEPDOG_WG_PORT:-8080}

# Try to verify the webgoat port is open
nc=/usr/bin/nc

if test -x "$nc"; then
    if "$nc" -z localhost $wgport; then
        echo PORT $wgport is in use!
        echo Try setting \$SHEEPDOG_WG_PORT
        exit 1
    fi
else
        echo ''
        echo '###################################'
        echo 'WARNING   WARNING  WARNING  WARNING'
        echo '###################################'
        echo 'Unable to determine if there is a port confict.'
        echo ''
        echo "Webgoat will run on port $wgport"
        echo ''
        echo 'If you see no route coverage after sheepdog runs, you may need to'
        echo 'specify a different port with $SHEEPDOG_WG_PORT.'
        echo ''
fi

# trap ctrl-c and call ctrl_c()
trap ctrl_c INT

function ctrl_c() {
        echo "** Trapped CTRL-C"
        if [ -n $pid1 ]; then kill $pid1; fi
        if [ -n $pid2 ]; then kill $pid2; fi
        exit 1
}

function webgoat {
java -javaagent:contrast.jar -Dcontrast.dir=working -Dcontrast.standalone.appname=$app -Dcontrast.override.appname="$app" -Dcontrast.path="/$path" -Dcontrast.server="$server" -Dcontrast.log.daily=true -jar webgoat-container-7.0.1-war-exec.jar -httpPort=$wgport > /dev/null 2>&1 &
}

function sheepdog {
java -jar sheepdog-1.0-SNAPSHOT.jar -t 3 -s 3600 -d 1500 250 -a 0 -p $wgport > /dev/null 2>&1 &

}

declare -a configs=(
    "WebGoat7|DEV-WG7|TEST-WG7|STAGE-WG7|PROD-WG7"
    "OracleFS|DEV-OFS|TEST-OFS|PROD-OFS"
    "CustomerCare|DEV-CCB|TEST-CCB|PROD-CCB"
    "WebStore|DEV-WS|STAGE-WS|PROD-WS"
    "MedicalRecords|DEV-EMR|STAGE-EMR|PROD-EMR"
    "WebPOS|DEV-WPOS|STAGE-WPOS|PROD-WPOS"
    "TradingFloor|DEV-TF|TEST-TF|PROD-TF" )

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
        echo "Attacking $app on $server using $path"
        sheepdog
        pid2=$!
        sleep 120
        kill -KILL $pid1
        kill -KILL $pid2
        echo "----------------"
    done

done
