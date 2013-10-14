package com.rallydev.intellij

class SpecUtils {

    static String minimalResponseJson = '''
{
    "QueryResult": {
        "_rallyAPIMajor": "1",
        "_rallyAPIMinor": "39",
        "Errors": [],
        "Warnings": [],
        "TotalResultCount": 0,
        "StartIndex": 1,
        "PageSize": 20,
        "Results":[
            { }
        ]
    }
}
'''

    static String getTypedMinimalResponseJson(String type) {
        return """
{
    "QueryResult": {
        "_rallyAPIMajor": "1",
        "_rallyAPIMinor": "39",
        "Errors": [],
        "Warnings": [],
        "TotalResultCount": 0,
        "StartIndex": 1,
        "PageSize": 20,
        "Results":[
            { _type: $type }
        ]
    }
}
"""
    }

}
