
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
	javac $<


basico:
	javac *.java
