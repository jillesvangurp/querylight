# querylight

This is my attempt at building a small kotlin library for implementing offline search in e.g. a website or inside a
mobile phone app (android, IOS, etc.).

This is currently quite early stage and the API can and will change. For documentation, refer to the tests.

# General design

Users of Elasticsearch, will recognize a thing or two. I loosely follow their DSL. At the core is a simple in memory data structure that implements TF/IDF. I've implemented a simple Analyzer structure similar to what Elasticsearch has and there's MatchQuery and BoolQuery that are getting close to usable.

Don't expect big performance. This is not intended to scale massively and only appropriate for small amounts of data.

Key motivation for building this is that I wanted offline search without a server and with some control over things like ranking. Judging from hacker news, there are countless others building their own search libraries. So yes, this is somewhat redundant.

That being said, it's starting to get useful and I'm looking forward to being able to embed the js version on my 
website at some point. 

## Get it 

We publish jars for this library to our own maven repository. Add it like this

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
