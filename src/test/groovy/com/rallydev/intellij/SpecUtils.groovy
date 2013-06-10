package com.rallydev.intellij

import com.rallydev.intellij.wsapi.ApiResponse

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

}
