FROM nexus.gp51.com/azul/zulu-openjdk:21

ARG version
ARG project_name

ADD ./target/${project_name}-${version}.war /usr/app/${project_name}-${version}.war

WORKDIR /usr/app

EXPOSE 8080 8443

ENV VERSION=${version} PROJECT_NAME=${project_name}

CMD java -server -noverify -Xmx2048M -jar /usr/app/${PROJECT_NAME}-${VERSION}.war

