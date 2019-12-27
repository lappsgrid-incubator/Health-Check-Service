FROM openjdk:8
COPY health-check.jar /root
RUN mkdir /var/log/health
CMD ["java", "-jar", "/root/health-check.jar"]
