set JAVA_HOME=./jdk1.8.0_91
set CLASSPATH=.;%JAVA_HOME%\lib\dt.jar;%JAVA_HOMe%\lib\tools.jar;
set Path=%JAVA_HOME%\bin;
java -Dloader.path=./lib,./resources -jar game-client-1.0-SNAPSHOT.jar