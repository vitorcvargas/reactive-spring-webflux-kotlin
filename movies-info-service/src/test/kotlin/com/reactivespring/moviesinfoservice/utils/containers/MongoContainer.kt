package com.reactivespring.moviesinfoservice.utils.containers

import org.testcontainers.containers.MongoDBContainer
import org.testcontainers.utility.DockerImageName

class MongoContainer {
    companion object {

        private var container = MongoDBContainer(DockerImageName.parse("mongo").withTag("latest")).withReuse(true)

        fun getInstance(): MongoDBContainer {
            return container
        }
    }
}