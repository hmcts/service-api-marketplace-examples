 # renovate: datasource=github-releases depName=microsoft/ApplicationInsights-Java
ARG APP_INSIGHTS_AGENT_VERSION=3.7.9
FROM hmctspublic.azurecr.io/base/java:21-distroless

COPY lib/applicationinsights.json /opt/app/
COPY build/libs/apim-marketplace-examples.jar /opt/app/

EXPOSE 8081
CMD [ "apim-marketplace-examples.jar" ]
