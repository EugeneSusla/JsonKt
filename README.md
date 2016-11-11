# Json.kt

An expressive way to create json via Jackson/Gson in Kotlin

```kotlin

json {
  "key" %= "value"
  "key2" %= json {
    "v1" %= 2
    "v2" %= json [ true, false, true ]
    "v3" %= Book(
        author = "Eugene Susla",
        numPages = 121
    )
  }
  "timestamp" %= Date()
}

```

## Features

### Java/Kotlin Objects as Json Objects

```kotlin
class Book(val author: String, val numPages: Int)

json {
	"book" %= Book(
		author = "Eugene Susla",
		numPages = 121
	)
}

```

Yields:

```json
{
	"book": {
		"author": "Eugene Susla",
		"numPages": 121
	}
}
```

### Uses Jackson/Gson data types

#### Using Jackson flavor

```kotlin
import com.fasterxml.jackson.databind.node.*

val o: ObjectNode = json {}
val a: ArrayNode = json []
val i: IntegerNode = json(42)
```

#### Using Gson flavor

```kotlin
import com.google.gson.*

val o: JsonObject = json {}
val a: JsonArray = json []
val i: JsonPrimitive = json(42)
```

### Custom Conversion Rules

```kotlin
json.autoConvert { d: Date -> d.time }

json {
	"timestamp" %= Date()
}

```

### Reflection support

```kotlin
// Explicit
json.fromProperties(bean)
json.fromFields(bean)

// Using defaults
json(bean) // Defaults to json.fromProperties

// or
json {
	"bean" %= bean
}

// Overriding the default
json.autoConvert { b: Any -> json.fromFields(bean) }

json(bean) // Will now use json.fromFields

```

### Code structure similar to json

```kotlin

json {
  "key" %= "value"
  "key2" %= json {
    "v1" %= 2
    "v2" %= json [ true, false, true ]
    "v3" %= Book(
        author = "Eugene Susla",
        numPages = 121
    )
  }
  "timestamp" %= Date()
}

```

Yields:

```json

{
  "key" : "value",
  "key2" : {
    "v1" : 2,
    "v2" : [ true, false, true ],
    "v3" : {
      "author" : "Eugene Susla",
      "numPages" : 121
    },
  },
  "timestamp" : 1477626841724
}

```
