export JAVA_HOME="/usr/lib/jvm/jre-1.8.0"

export CATALINA_OPTS="$CATALINA_OPTS -Xms4092m"
export CATALINA_OPTS="$CATALINA_OPTS -Xmx12276m"

export CATALINA_OPTS="$CATALINA_OPTS -Djava.security.manager -Djava.security.policy=/opt/app/stratis/tomcat/current/conf/catalina.policy"

#export CATALINA_OPTS="$CATALINA_OPTS -Djava.security.debug=all"
