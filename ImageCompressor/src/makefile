PATH:=${JAVA_HOME}/bin:${PATH}
HADOOP_PATH=${hh}
NEW_CLASSPATH=${HADOOP_PATH}/hadoop-core-1.0.4.jar:${HADOOP_PATH}/lib/*:${HADOOP_PATH}/lib/jetty-ext/*:${hh_aux_libs}/ij.jar:${hh_aux_libs}/jna.jar:${hh_libs}/h_ImageJ.jar:/usr/lib/jvm/java-6-openjdk-amd64/lib/tools.jar:${CLASSPATH}

SRC = $(wildcard ./mapreduce/*.java)

all: build

build: ${SRC}
	${JAVA_HOME}/bin/javac -Xlint:deprecation -classpath ${NEW_CLASSPATH} ${SRC}
	${JAVA_HOME}/bin/jar cvf ~/Dropbox/research/ImageCompressor.jar ./mapreduce/*.class
clean:
	rm -f *.class *.jar
