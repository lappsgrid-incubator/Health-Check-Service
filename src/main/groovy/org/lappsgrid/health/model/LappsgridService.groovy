package org.lappsgrid.health.model

import groovy.transform.EqualsAndHashCode
import groovy.util.logging.Slf4j
import org.lappsgrid.client.ServiceClient
import org.lappsgrid.health.util.Status
import org.lappsgrid.metadata.IOSpecification
import org.lappsgrid.metadata.ServiceMetadata
import org.lappsgrid.serialization.Data
import org.lappsgrid.serialization.Serializer

import static org.lappsgrid.discriminator.Discriminators.*


/**
 *
 */
@Slf4j("logger")
//@TupleConstructor
@EqualsAndHashCode(includes = ['url'])
class LappsgridService {
    String id
    String serviceId
    Version version
    String name
    String url
    IOSpecification requires
    IOSpecification produces

    LappsgridService() {

    }

    LappsgridService(String id, String name, String url) {
        this.id = id
        this.name = name
        this.url = url

        String[] parts = id.split('_')
        if (parts.length == 2) {
            serviceId = parts[0]
            version = new Version(parts[1])
        }
        else {
            serviceId = id
            version = new Version('0.0.0')
        }
    }

    void setId(String id) {
        this.id = id
    }

    boolean isConverter() {
        if (requires.format[0] == produces.format[0]) {
            return false
        }
        return requires.annotations.size() == 0 && produces.annotations.size() == 0
    }

    String toString() {
        "$id $url"
    }

    String getServiceId() {
        if (serviceId != null) {
            return serviceId
        }
        parseId()
        return serviceId
    }

    Version getVersion() {
        if (version != null) {
            return version
        }
        parseId()
        return version
    }

    String satisfies(LappsgridService service) {
        if (produces.format.disjoint(service.requires.format)) {
            return "No matching formats"
        }
        if (service.requires.language && service.requires.language.size() > 0) {
            if (service.requires.language.disjoint(produces.language)) {
                return "No matching language"
            }
        }
        List<String> matching = service.requires.annotations.intersect(produces.annotations)
        if (matching.size() != service.requires.annotations.size()) {
            return "Missing required annotations"
        }
        return null
    }

    String update() {
        logger.debug("Getting metadata for {}", url)
        ServiceClient client = new ServiceClient(url, 'tester', 'tester')
        String json = null
        try {
            json = client.getMetadata()
        }
        catch (Exception e) {
            logger.error("Unable to call the client {}", id, e)
            return Status.ERROR
        }
        if (json == null) {
            logger.warn("Client did not return any data: {}", url)
            return Status.NO_DATA
        }

        Data data = Serializer.parse(json)
        if (data.discriminator != Uri.META) {
            logger.warn("Service {} did not return metadata: {}", id, data.discriminator)
            logger.warn(data.payload.toString())
            return Status.NOT_META
        }
        try {
            ServiceMetadata metadata = new ServiceMetadata(data.payload)
            this.requires = metadata.requires
            this.produces = metadata.produces
        }
        catch (Exception e) {
            logger.warn("Unable to parse metadata from service {}", id, e)
            return Status.PARSE
        }
        return Status.OK
    }

    private void parseId() {
        int underscore = id.lastIndexOf('_')
        if (underscore < 0) {
            serviceId = id
            version = new Version('0.0.0')
        }
        else {
            serviceId = id.substring(0, underscore)
            version = new Version(id.substring(underscore+1))
        }
    }
    /*
    boolean active
    String updatedDate
    String serviceId
    String registeredDate
    List<String> supportedLanguages
    String serviceDescription
    String ownerUserId
    String instanceType
    String serviceTypeDomain
    String serviceName
    String endpointUrl
    String serviceType
                "active": true,
            "updatedDate": "2019-07-07T10:31:32+0000",
            "serviceId": "anc:opennlp.cloud.lemmatizer_pipeline_1.0.0",
            "registeredDate": "2019-07-07T10:31:32+0000",
            "supportedLanguages": [

            ],
            "serviceDescription": "Apache OpenNLP SentenceSplitter",
            "ownerUserId": "anc:suderman",
            "instanceType": "EXTERNAL",
            "serviceTypeDomain": "lapps.nlp",
            "serviceName": "Apache OpenNLP Cloud LemmatizerPipeline v1.0.0",
            "endpointUrl": "http://vassar.lappsgrid.org/invoker/anc:opennlp.cloud.lemmatizer_pipeline_1.0.0",
            "serviceType": "SERVICE.TYPE.PROCESSOR"
     */
}
