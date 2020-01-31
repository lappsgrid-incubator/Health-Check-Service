ROOT=$(shell pwd)
JAR=health-check.jar
REPO=docker.lappsgrid.org
GROUP=lappsgrid
DOCKER=src/main/docker
NAME=health
IMAGE=$(GROUP)/$(NAME)
TARGET=target/$(JAR)
TAG=$(REPO)/$(IMAGE)
VERSION=$(shell cat VERSION)

jar:
	mvn package

clean:
	mvn clean
	if [ -e $(DOCKER)/$(JAR) ] ; then rm $(DOCKER)/$(JAR) ; fi
 
run:
	java -jar $(TARGET)

docker:
	if [ ! -e $(DOCKER)/$(JAR) ] ; then cp $(TARGET) $(DOCKER) ; fi
	if [ $(TARGET) -nt $(DOCKER)/$(JAR) ] ; then cp $(TARGET) $(DOCKER) ; fi
	cd $(DOCKER) && docker build -t $(IMAGE) .

start:
	docker run -d -p 8080:8080 --name $(NAME) -v /private/etc/lapps:/etc/lapps $(IMAGE)

stop:
	docker rm -f $(NAME)

push:
	docker tag $(IMAGE) $(TAG)
	docker push $(TAG)

release:
	docker tag $(IMAGE) $(TAG):$(VERSION)
	docker push $(TAG):$(VERSION)
	
update:
	curl -i http://129.114.17.83:9000/api/webhooks/6df201d2-808c-427a-a855-df1b2b6edd56
	