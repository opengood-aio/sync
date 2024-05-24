FROM chubb-docker-hub.nexus.chubbdigital.com/cloud-engg/chubb-standard-images/java/17:1.19
LABEL org=prs
LABEL classification=@deploymentKubernetesNamespace@
LABEL appName=@applicationName@
USER root
RUN /usr/bin/microdnf -y update
RUN /usr/bin/microdnf clean all
COPY target/@applicationName@-*.jar app.jar
EXPOSE 8080
USER 185
ENTRYPOINT ["java","-jar","app.jar","$JAVA_TOOL_OPTIONS"]
