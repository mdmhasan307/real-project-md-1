FROM registry.tacticaledge.us/mls2/java8tomcat:9.0.45_IB

USER root

RUN yum update -y --nogpgcheck --disableplugin=subscription-manager && \
    yum install -y --nogpgcheck --disableplugin=subscription-manager fontconfig && \
    yum install -y --nogpgcheck --disableplugin=subscription-manager dejavu-sans-fonts && \
    yum -y clean all 


## WAIT SUPPORT
ADD https://github.com/ufoscout/docker-compose-wait/releases/download/2.5.0/wait /wait
RUN chmod +x /wait

COPY ./setup.sh /setup.sh
COPY ./start.sh /start.sh
COPY ./sources/opt/ /opt
COPY ./sources/application/ /opt/app/wars
COPY ./sources/mnt/ /mnt

RUN ./setup.sh && /bin/rm ./setup.sh

USER tomcat

ENTRYPOINT ["/start.sh"]
