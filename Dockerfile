#
# Oracle Java 8 Dockerfile
#
# https://github.com/dockerfile/java
# https://github.com/dockerfile/java/tree/master/oracle-java8
#

# Pull base image.
FROM ubuntu:16.04

ARG species=all

RUN apt-get -y update && apt-get install -y software-properties-common python-software-properties

# Install Java.
RUN \
  echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | debconf-set-selections && \
  add-apt-repository -y ppa:webupd8team/java && \
  apt-get update && \
  apt-get install -y oracle-java8-installer && \
  rm -rf /var/lib/apt/lists/* && \
  rm -rf /var/cache/oracle-jdk8-installer

# Define commonly used JAVA_HOME variable
ENV JAVA_HOME /usr/lib/jvm/java-8-oracle

# Define working directory.
WORKDIR /data
ADD owlsim-services/target/owlsim-services-3.0-SNAPSHOT.jar /data/
#ADD configuration-samples/configuration-all.yaml /data/configuration.yaml
ADD scripts/golr-exporter.py /data/golr-exporter.py
ADD scripts/configuration-generator.py /data/configuration-generator.py

RUN apt-get -y update && apt-get install python-pip -y
RUN pip install pyyaml

RUN if [ $species = "all" ];  \
  then \
    cd /data && python golr-exporter.py; \
  else \
    cd /data && python golr-exporter.py -t $species; \
  fi
RUN cd /data && python configuration-generator.py

CMD java -Xmx10g -jar /data/owlsim-services-3.0-SNAPSHOT.jar server /data/configuration.yaml

EXPOSE 8080
