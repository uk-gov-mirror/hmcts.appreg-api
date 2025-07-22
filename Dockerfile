# Build stage: Install bash and dependencies
FROM debian:latest AS build-stage
RUN apt-get update && apt-get install -y bash libtinfo6

# Runtime stage: Use a Debian-based OpenJDK image
FROM openjdk:21-jdk-slim

# Copy bash and necessary libraries from the build stage
COPY --from=build-stage /bin/bash /bin/bash
COPY --from=build-stage /lib/x86_64-linux-gnu/libtinfo.so.6 /lib/x86_64-linux-gnu/libtinfo.so.6

# Copy your application files
COPY lib/applicationinsights.json /opt/app/
COPY build/libs/app-register.jar /opt/app/

# Expose port and set the entrypoint
EXPOSE 4550
ENTRYPOINT ["java", "-Duser.timezone=UTC", "-jar", "/opt/app/app-register.jar"]
