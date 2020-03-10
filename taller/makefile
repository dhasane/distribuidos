
SRCS := $(wildcard *.java)
BINS := $(SRCS:%.java=%)

all: ${BINS}
	@echo ${BINS}

%: %.class
	@echo $<

%.class: %.java
	javac $<


basico:
	javac *.java
