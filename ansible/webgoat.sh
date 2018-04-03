java -javaagent:contrast.jar \
-Dcontrast.dir=working \
-Dcontrast.server=demo \
-Dcontrast.server.activity.period=5000 \
-Dcontrast.log.daily=true \
-Dcontrast.level=info \
-jar webgoat-container-7.0.1-war-exec.jar
