
start: Test.class Broker.class Computador.class Conector.class Connection.class Mensaje.class Pais.class Utils.class
	rm ./logs/*
	java Test

%.class: %.java
	javac $<

clean:
	rm *.class

# %: %.class
	# @echo $<

