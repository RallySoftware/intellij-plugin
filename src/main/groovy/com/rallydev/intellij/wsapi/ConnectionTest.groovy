package com.rallydev.intellij.wsapi

class ConnectionTest {

    void doTest() throws Exception {
        ApiResponse response = RallyClient.getInstance()
                .makeRequest(new GetRequest(ApiEndpoint.WORKSPACE))
        if (!response?.results) {
            throw new RuntimeException("Incorrect response from server\n${response ?: 'No response'}")
        }
    }

}
