JC = javac
JFLAGS = -g -classpath "$(CORENLPW_CLASSPATH)/*:."

.SUFFIXES: .java .class

.java.class:
ifndef CORENLPW_CLASSPATH
	$(error CORENLPW_CLASSPATH is undefined (maybe try `source settings`))
endif
	$(JC) $(JFLAGS) $*.java

CLASSES = CoreNLPWatcher.java

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class
