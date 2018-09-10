package little.config

import java.time.{ Duration, Period }
import org.scalatest.FlatSpec
import com.typesafe.config.{ Config, ConfigFactory, ConfigMemorySize }

import Implicits._ 

class ConfigSpec extends FlatSpec {
  val config = ConfigFactory.parseString("""
    user { id = 0,  name = root }
    users = [
      { id = 0, name = root }
      { id = 500, name = guest }
    ]
    string = hello
    boolean = true
    int = 123
    long = 123456789
    double = 123.456
    duration = 10s
    period = "7 days"
    size = 10K
  """)

  case class User(id: Int, name: String)

  // Define how to get User from Config
  implicit val getUser: GetValue[User] = (config, path) => {
    val user = config.getConfig(path)
    User(user.getInt("id"), user.getString("name"))
  }

  "Config" should "be read" in {
    val users = Seq(User(0, "root"), User(500, "guest"))
    assert(config.get[User]("user") == User(0, "root"))
    assert(config.get[Seq[User]]("users").sameElements(users))
    assert(config.get[List[User]]("users").sameElements(users))
    assert(config.get[Set[User]]("users").sameElements(users))
    assert(config.get[Array[User]]("users").sameElements(users))
    assert(config.get[Iterator[User]]("users").toSeq.sameElements(users))
    assert(config.get[String]("string") == "hello")
    assert(config.get[Boolean]("boolean") == true)
    assert(config.get[Int]("int") == 123)
    assert(config.get[Long]("long") == 123456789)
    assert(config.get[Double]("double") == 123.456)
    assert(config.get[Duration]("duration") == Duration.ofSeconds(10))
    assert(config.get[Period]("period") == Period.ofDays(7))
    assert(config.get[ConfigMemorySize]("size") == ConfigMemorySize.ofBytes(10240))
  }

  it should "be read with default" in {
    assert(config.getOrElse("user", User(500, "guest")) == User(0, "root"))
    assert(config.getOrElse("users", List[User]()) == List(User(0, "root"), User(500, "guest")))
    assert(config.getOrElse("string", "") == "hello")
    assert(config.getOrElse("boolean", false) == true)
    assert(config.getOrElse("int", 0) == 123)
    assert(config.getOrElse("long", 0L) == 123456789)
    assert(config.getOrElse("double", 0.0) == 123.456)
    assert(config.getOrElse("duration", Duration.ofSeconds(0)) == Duration.ofSeconds(10))
    assert(config.getOrElse("period", Period.ofDays(0)) == Period.ofDays(7))
    assert(config.getOrElse("size", ConfigMemorySize.ofBytes(0)) == ConfigMemorySize.ofBytes(10240))

    assert(config.getOrElse("xuser", User(500, "guest")) == User(500, "guest"))
    assert(config.getOrElse("xusers", List[User]()) == Nil)
    assert(config.getOrElse("xstring", "") == "")
    assert(config.getOrElse("xboolean", false) == false)
    assert(config.getOrElse("xint", 0) == 0)
    assert(config.getOrElse("xlong", 0L) == 0L)
    assert(config.getOrElse("xdouble", 0.0) == 0.0)
    assert(config.getOrElse("xduration", Duration.ofSeconds(0)) == Duration.ofSeconds(0))
    assert(config.getOrElse("xperiod", Period.ofDays(0)) == Period.ofDays(0))
    assert(config.getOrElse("xsize", ConfigMemorySize.ofBytes(0)) == ConfigMemorySize.ofBytes(0))
  }

  it should "be optionally read" in {
    assert(config.getOption[User]("user").contains(User(0, "root")))
    assert(config.getOption[Seq[User]]("users").contains(Seq(User(0, "root"), User(500, "guest"))))
    assert(config.getOption[String]("string").contains("hello"))
    assert(config.getOption[Boolean]("boolean").contains(true))
    assert(config.getOption[Int]("int").contains(123))
    assert(config.getOption[Long]("long").contains(123456789))
    assert(config.getOption[Double]("double").contains(123.456))
    assert(config.getOption[Duration]("duration").contains(Duration.ofSeconds(10)))
    assert(config.getOption[Period]("period").contains(Period.ofDays(7)))
    assert(config.getOption[ConfigMemorySize]("size").contains(ConfigMemorySize.ofBytes(10240)))

    assert(config.getOption[User]("xuser") == None)
    assert(config.getOption[List[User]]("xusers") == None)
    assert(config.getOption[String]("xstring") == None)
    assert(config.getOption[Boolean]("xboolean") == None)
    assert(config.getOption[Int]("xint") == None)
    assert(config.getOption[Long]("xlong") == None)
    assert(config.getOption[Double]("xdouble") == None)
    assert(config.getOption[Duration]("xduration") == None)
    assert(config.getOption[Period]("xperiod") == None)
    assert(config.getOption[ConfigMemorySize]("xsize") == None)
  }

  it should "try to be read" in {
    assert(config.getTry[User]("user").get == User(0, "root"))
    assert(config.getTry[List[User]]("users").get == List(User(0, "root"), User(500, "guest")))
    assert(config.getTry[String]("string").get == "hello")
    assert(config.getTry[Boolean]("boolean").get == true)
    assert(config.getTry[Int]("int").get == 123)
    assert(config.getTry[Long]("long").get == 123456789)
    assert(config.getTry[Double]("double").get == 123.456)
    assert(config.getTry[Duration]("duration").get == Duration.ofSeconds(10))
    assert(config.getTry[Period]("period").get == Period.ofDays(7))
    assert(config.getTry[ConfigMemorySize]("size").get == ConfigMemorySize.ofBytes(10240))

    assert(config.getTry[User]("xuser").isFailure)
    assert(config.getTry[List[User]]("xusers").isFailure)
    assert(config.getTry[String]("xstring").isFailure)
    assert(config.getTry[Boolean]("xboolean").isFailure)
    assert(config.getTry[Int]("xint").isFailure)
    assert(config.getTry[Long]("xlong").isFailure)
    assert(config.getTry[Double]("xdouble").isFailure)
    assert(config.getTry[Duration]("xduration").isFailure)
    assert(config.getTry[Period]("xperiod").isFailure)
    assert(config.getTry[ConfigMemorySize]("xsize").isFailure)
  }
}
