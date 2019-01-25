FROM openjdk:jre-alpine

ENV H2_DOWNLOAD_URL http://www.h2database.com/h2-2018-03-18.zip
ENV DATA_DIR /opt/h2/data

RUN apk add --no-cache curl

RUN mkdir -p ${DATA_DIR} \
    && curl ${H2_DOWNLOAD_URL} -o h2.zip \
    && unzip h2.zip -d /opt/ \
    && rm h2.zip \
    && ls -al /opt \
    && ls -al /opt/h2 \
    && ls -al /opt/h2/bin

COPY h2.server.properties /root/.h2.server.properties

EXPOSE 9092
EXPOSE 9999

CMD sh /opt/h2/bin/h2.sh -tcp -tcpAllowOthers -tcpPort 9092 -web -webAllowOthers -webPort 9999 -baseDir ${DATA_DIR}
