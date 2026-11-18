ARG TRIVY_VERSION=0.76.2
FROM docker:29-cli AS dockercli
FROM aquasec/trivy:${TRIVY_VERSION} AS trivy

FROM jenkins/jenkins:lts-jdk21
USER root
COPY --from=dockercli /usr/local/bin/docker /usr/local/bin/docker
COPY --from=dockercli /usr/local/libexec/docker/cli-plugins/ /usr/local/libexec/docker/cli-plugins/
COPY --from=trivy /usr/local/bin/trivy /usr/local/bin/trivy
USER jenkins
