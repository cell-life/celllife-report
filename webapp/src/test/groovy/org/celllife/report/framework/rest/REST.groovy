package org.celllife.report.framework.rest

import groovyx.net.http.*

/**
 * User: Kevin W. Sewell
 * Date: 2013-04-04
 * Time: 14h39
 */
class REST {

    static String contextPath = "/report"

    static String baseUrl = "http://localhost:9000"

    static RESTClient client = new RESTClient(baseUrl)

    static {
        client.auth.basic("internal", "password")
    }

    static get(Map<String, ?> args) {
        def response = client.get(args)
        return response.data
    }

    static post(Map<String, ?> args) {
        def response = client.post(args)
        return response.data
    }

    static delete(Map<String, ?> args) {
        def response = client.delete(args)
        return response.data
    }
}
