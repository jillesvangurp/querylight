# Ktjsearch

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

This is a kotlin multiplatform distribution with packages for `-jvm` and `-js` (currently). Currently, 
multiplatform does not work with `jitpack.io` which I use on other projects. If you are interested,
there's an [open bug for this](https://github.com/jitpack/jitpack.io/issues/3853).

So, as a workaround, I currently distribute jars via my website. To add the repository, add something 
like this to your `build.gradle.kts` file:

```kotlin
repositories {
    mavenCentral()
    maven { url = uri("https://www.jillesvangurp.com/maven") }
}
```

and then add the dependency:

```kotlin
implementation("com.github.jillesvangurp:ktjsearch:0.0.1")
```

For older versions of gradle you may have to specify a postfix `-jvm` or `-js`. Supposedly recent versions are smarter about figuring out multiplatform.

Let me know if you have issues accessing the jars.
