

all: compile cleanLogs
	mvn exec:java

compile:
	mvn compile

cleanLogs:
	touch ./logs/hola
	rm ./logs/*


1: compile
	mvn exec:java -Dexec.args="config/c1.json"
2: compile
	mvn exec:java -Dexec.args="config/c2.json"
