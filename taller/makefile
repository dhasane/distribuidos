
SRCS := $(wildcard *.java)
BINS := $(SRCS:%.java=%)

all: ${BINS}
	@echo ${BINS}

client: TCPCliente.class Connection.class
	java TCPCliente

server: TCPServer.class Connection.class
	java TCPServer

%: %.class
	@echo $<

%.class: %.java
	rm *.class
	javac $<


basico:
	javac *.java

clean:
	rm *.class
