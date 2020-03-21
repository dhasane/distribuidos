
SRCS := $(wildcard *.java)
BINS := $(SRCS:%.java=%)

all: ${BINS}
	@echo ${BINS}

start: Main.class
	java Main

%: %.class
	@echo $<

%.class: %.java
	rm *.class
	javac $<

basico:
	javac -d *.java

clean:
	rm *.class
