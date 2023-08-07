# Querylight

Querylight is an in memory, kotlin multi platform text indexing library that implements tfidf and a minimal query language. Great for client side search in web apps, android or other Kotlin apps.

This is my attempt at building a small kotlin library for implementing offline search in e.g. a website or inside a
mobile phone app (android, IOS, etc.). This is currently quite early stage and the API can and will change. For documentation, refer to the tests.

# General design

Users of Elasticsearch, will recognize a thing or two. I loosely follow their DSL. At the core is a simple in memory reverse index that implements TF/IDF for ranking. I've implemented a simple Analyzer structure similar to what Elasticsearch has and there's MatchQuery and BoolQuery that are usable for simple queries.

Don't expect amazing performance or search quality. This is not intended to scale massively and only appropriate for small amounts of data. That being said, it does work fine for small data sets in web applications, which is what I use this for.

Key motivation for building this is that I wanted offline search without a server and with some control over things like ranking. Judging from hacker news, there are countless others building their own search libraries. So yes, this is somewhat redundant.

## Kotlin Multiplatform

Since this is a kotlin multiplatform library without dependencies, it should be possible to get this running pretty much anywhere Kotlin runs. I use it mainly in a kotlin-js based web application currently but it should do fine on IOS/Android with compose multiplatform, spring servers, or any other places you use Kotlin.

## Get it 

We publish jars for this library to our own maven repository. Add it like this:

```kotlin
repositories {
    mavenCentral()
    maven("https://maven.tryformation.com/releases")
}
```

and then add the dependency:

```kotlin
implementation("com.github.jillesvangurp:querylight:0.1.5")
```

For older versions of gradle you may have to specify a postfix `-jvm` or `-js`. Supposedly recent versions are smarter about figuring out multiplatform.

Let me know if you have issues accessing the jars.
