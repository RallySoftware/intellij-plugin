package com.rallydev.intellij.wsapi

class ConnectionTest {

    RallyClient client

    ConnectionTest() {
        client = RallyClient.instance
    }

    ConnectionTest(URL server, String username, String password, Boolean promptForPassword) {
        client = new RallyClientConfigurable(server, username, password, promptForPassword)
    }

    void doTest() throws Exception {
        ApiResponse response = client.makeRequest(new GetRequest(ApiEndpoint.WORKSPACE)).get()
        if (!response?.results) {
            throw new RuntimeException("Incorrect response from server\n${response ?: 'No response'}")
        }
    }

}
