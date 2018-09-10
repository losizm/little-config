# little-config

The Scala library that provides extension methods to _com.typesafe.config_.

## Getting Started
To use **little-config**, start by adding it to your project:

* sbt
```scala
libraryDependencies += "com.github.losizm" %% "little-config" % "0.1.0"
```
* Gradle
```groovy
compile group: 'com.github.losizm', name: 'little-config_2.12', version: '0.1.0'
```
* Maven
```xml
<dependency>
  <groupId>com.github.losizm</groupId>
  <artifactId>little-config_2.12</artifactId>
  <version>0.1.0</version>
</dependency>
```

### Using an implementation of com.typesafe.config
**little-config** is compiled with _com.typesafe.config_ 1.3.3, and you must add
the implementation of _com.typesafe.config_ to your project.

So, for example, include the following in your sbt build to add it as a
dependency:

```scala
libraryDependencies += "com.typesafe" % "config" % "1.3.3"
```

## A Taste of little-config
Here's a taste of what **little-config** offers.

### Getting Custom Value from Config

**little-config** is powered by a single trait, `GetValue`. You provide an
implementation of this to get a custom value from `Config`.

```scala
import com.typesafe.config.{ Config, ConfigFactory }
import little.config.GetValue
import little.config.Implicits._ // Unleash the power

case class User(id: Int, name: String)

// Define how to get User from Config
implicit val getUser: GetValue[User] = (config, path) => {
  val user = config.getConfig(path)
  User(user.getInt("id"), user.getString("name"))
}

// Build a Config
val config = ConfigFactory.parseString("""user { id = 0,  name = root }""")

// Get User from Config
val user = config.get[User]("user")
```
A special implementation of `GetValue` is available for converting a
`ConfigList` to a collection of custom values. For example, if you define
`GetValue[User]`, you're automagically provided `GetValue[Seq[User]]`.

```scala
val config = ConfigFactory.parseString("""
  system {
    users = [
      { id = 0, name = root }
      { id = 500, name = guest }
    ]
  }
""")

// Get sequence of users
val users = config.get[Seq[User]]("system.users")

// Or get just about any other type of collection
val listOfUsers = config.get[List[User]]("system.users")
val setOfUsers = config.get[Set[User]]("system.users")
val iterOfUsers = config.get[Iterator[User]]("system.users")
val arrayOfUsers = config.get[Array[User]]("system.users")
```

### Getting Optional Value from Config

If you want to get an optional value, **little-config** has you covered. It
implicitly adds the `getOption[T]` method to `Config`.

```scala
val config = ConfigFactory.parseString("""user { id = 0,  name = root }""")

// Get user or not
val some = config.getOption[User]("user")
val none = config.getOption[User]("guest")
```

And you can use the method with all of the standard types as well.

```scala
import java.time.{ Duration, Period }
import com.typesafe.config.ConfigMemorySize

val config = ConfigFactory.parseString("""
  { id = 500, enabled = false, storage = 2G, timeout = 10s }
""")

// Get optional values
val id = config.getOption[Int]("id")
val name = config.getOption[String]("name")
val enabled = config.getOption[Boolean]("enabled")
val key = config.getOption[Long]("key")
val avgUsage = config.getOption[Double]("avgUsage")
val timeout = config.getOption[Duration]("timeout")
val retention = config.getOption[Period]("retention")
val storage = config.getOption[ConfigMemorySize]("storage")
```

Or, if you rather provide a default value, there's a method for that too.

```scala
val config = ConfigFactory.parseString("""user { id = 0,  name = root }""")

// Get value or supplied default
val user = config.getOrElse("user", User(500, "guest"))
val id = config.getOrElse("id", 500)
val name = config.getOrElse("name", "guest")
val enabled = config.getOrElse("enabled", false)
val key = config.getOrElse("key", 0L)
val avgUsage = config.getOrElse("avgUsage", 0.0)
val timeout = config.getOrElse("timeout", Duration.ofSeconds(10))
val retention = config.getOrElse("retention", Period.ofWeeks(1))
val storage = config.getOrElse("storage", ConfigMemorySize.ofBytes(0))
```

In all fairness, there are other ways to achieve this, such as defining default
values in a reference configuration, or using `Config.withFallback()`. But
providing a default when getting the value can be a nice alternative at times.

### Trying to Get Value from Config

The `getTry[T]` method is added to `Config` to work in much the same way as
getting an optional value. It wraps the value in `Success` if the path is
present and if the value can be converted to the requested type, or otherwise a
`Failure` is returned.

```scala
val config = ConfigFactory.parseString("""user { id = "Oops!",  name = root }""")

// Try to get user
val user = config.getTry[User]("user")
```

And like `getOption[T]`, the method can be used with the standard types as well.

```scala
val config = ConfigFactory.parseString("""
  { id = 500, enabled = false, storage = 2G, timeout = 10s }
""")

// Try to get values
val id = config.getTry[Int]("id")
val name = config.getTry[String]("name")
val enabled = config.getTry[Boolean]("enabled")
val key = config.getTry[Long]("key")
val avgUsage = config.getTry[Double]("avgUsage")
val timeout = config.getTry[Duration]("timeout")
val retention = config.getTry[Period]("retention")
val storage = config.getTry[ConfigMemorySize]("storage")
```

## License
**little-config** is licensed under the Apache License, Version 2. See LICENSE
file for more information.
