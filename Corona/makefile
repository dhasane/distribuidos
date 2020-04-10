

all: compile cleanLogs


compile:
	mvn compile

cleanLogs:
	touch ./logs/hola
	rm ./logs/*


1: compile
	mvn exec:java -Dexec.args="config/general.json pc1"
2: compile
	mvn exec:java -Dexec.args="config/general.json pc2"
3: compile
	mvn exec:java -Dexec.args="config/general.json pc3"
