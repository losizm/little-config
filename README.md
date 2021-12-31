# little-config

[![Maven Central](https://img.shields.io/maven-central/v/com.github.losizm/little-config_3.svg?label=Maven%20Central)](https://search.maven.org/search?q=g:%22com.github.losizm%22%20AND%20a:%22little-config_3%22)

The Scala library that provides extension methods for [Typesafe Config](https://github.com/lightbend/config).

## Getting Started
To get started, add **little-config** to your project:

```scala
libraryDependencies += "com.github.losizm" %% "little-config" % "3.0.0"
```
_**NOTE:** Starting with version 1.0, **little-config** is written for Scala 3
exclusively. See previous releases for compatibility with Scala 2.12 and Scala
2.13._

### Using Implementation of Typesafe Config
**little-config** has a runtime dependency to _Typesafe Config 1.4.x_, and
you must add an implementation to your project.

For example, you could add the following to your build:

```scala
libraryDependencies += "com.typesafe" % "config" % "1.4.1"
```

## A Taste of little-config
Here's a taste of what **little-config** offers.

### Getting Custom Value from Config

**little-config** is powered by a single trait: `ConfigDelegate`. You provide an
implementation of this to get a custom value from `Config`.

```scala
import com.typesafe.config.{ Config, ConfigFactory }
import little.config.{ *, given }

case class User(id: Int, name: String)

// Define how to get User from Config
given ConfigDelegate[User] with
  def get(config: Config, path: String): User =
    val user = config.getConfig(path)
    User(user.getInt("id"), user.getString("name"))

val config = ConfigFactory.parseString("""user { id = 0, name = root }""")

// Get User from Config
val user = config.get[User]("user")
```
A special implementation of `ConfigDelegate` is available for converting a
`ConfigList` to a collection of custom values. For example, if you define
`ConfigDelegate[User]`, you automatically get `ConfigDelegate[Seq[User]]`.

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
val listOfUsers  = config.get[List[User]]("system.users")
val setOfUsers   = config.get[Set[User]]("system.users")
val iterOfUsers  = config.get[Iterator[User]]("system.users")
val arrayOfUsers = config.get[Array[User]]("system.users")
```

### Getting Optional Value from Config

If you want to get an optional value, **little-config** adds the `getOption[T]`
method to `Config`.

```scala
val config = ConfigFactory.parseString("""user { id = 0,  name = root }""")

// Get user or not
val some = config.getOption[User]("user")
val none = config.getOption[User]("guest")
```

And, you can use the method with all of the standard types as well.

```scala
import java.time.{ Duration, Period }
import com.typesafe.config.ConfigMemorySize

val config = ConfigFactory.parseString("""
  { id = 500, enabled = false, storage = 2G, timeout = 10s }
""")

// Get optional values
val id        = config.getOption[Int]("id")
val name      = config.getOption[String]("name")
val enabled   = config.getOption[Boolean]("enabled")
val key       = config.getOption[Long]("key")
val avgUsage  = config.getOption[Double]("avgUsage")
val timeout   = config.getOption[Duration]("timeout")
val retention = config.getOption[Period]("retention")
val storage   = config.getOption[ConfigMemorySize]("storage")
```

Or, if you rather provide a default value, there's a method for that too.

```scala
val config = ConfigFactory.parseString("""user { id = 0,  name = root }""")

// Get value or supplied default
val user      = config.getOrElse("user", User(500, "guest"))
val id        = config.getOrElse("id", 500)
val name      = config.getOrElse("name", "guest")
val enabled   = config.getOrElse("enabled", false)
val key       = config.getOrElse("key", 0L)
val avgUsage  = config.getOrElse("avgUsage", 0.0)
val timeout   = config.getOrElse("timeout", Duration.ofSeconds(10))
val retention = config.getOrElse("retention", Period.ofWeeks(1))
val storage   = config.getOrElse("storage", ConfigMemorySize.ofBytes(0))
```

### Trying to Get Value from Config

The `getTry[T]` method is added to `Config` to work in much the same way as
getting an optional value. It wraps the value in `Success` if the path is
present and the value can be converted to the requested type; otherwise, a
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
val id        = config.getTry[Int]("id")
val name      = config.getTry[String]("name")
val enabled   = config.getTry[Boolean]("enabled")
val key       = config.getTry[Long]("key")
val avgUsage  = config.getTry[Double]("avgUsage")
val timeout   = config.getTry[Duration]("timeout")
val retention = config.getTry[Period]("retention")
val storage   = config.getTry[ConfigMemorySize]("storage")
```

### Getting Java Enum Value from Config

To finish off, **little-config** provides an implementation of `ConfigDelegate`
for getting Java enums. This gives you the power of the previous features
discussed, such as getting a list of enums, getting an optional enum, getting an
enum with a default value, and trying to get an enum.

```scala
import java.time.Month, Month.*

val config = ConfigFactory.parseString("""
  first = JANUARY
  summer = [JUNE, JULY, AUGUST]
""")

val first    = config.get[Month]("first")
val summer   = config.get[List[Month]]("summer")
val last     = config.getOption[Month]("last")
val fall     = config.getOrElse("fall", Seq(SEPTEMBER, OCTOBER, NOVEMBER))
val vacation = config.getTry[Month]("vacation") // :(
```

## API Documentation

See [scaladoc](https://losizm.github.io/little-config/latest/api/little/config.html)
for additional details.

## License
**little-config** is licensed under the Apache License, Version 2. See [LICENSE](LICENSE)
for more information.
