ARG APP_INSIGHTS_AGENT_VERSION=3.2.10
FROM hmctspublic.azurecr.io/base/java:17-distroless

COPY build/libs/em-ped-api.jar /opt/app/

EXPOSE 8080
CMD [ "em-ped-api.jar" ]
