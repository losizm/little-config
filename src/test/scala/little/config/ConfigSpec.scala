/*
 * Copyright 2018 Carlos Conyers
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package little.config

import java.time.{ Duration, Period, Month }
import org.scalatest.FlatSpec
import com.typesafe.config.{ Config, ConfigFactory, ConfigMemorySize }

import Implicits._ 
import Month._

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
    month = MARCH
    months = [JANUARY, JULY, DECEMBER]
    size = 10K
  """)

  case class User(id: Int, name: String)

  // Define how to get User from Config
  implicit val getUser: GetValue[User] = (config, path) => {
    val user = config.getConfig(path)
    User(user.getInt("id"), user.getString("name"))
  }

  val users = Seq(User(0, "root"), User(500, "guest"))
  val months = Seq(JANUARY, JULY, DECEMBER)

  "Config" should "be read" in {
    assert(config.get[User]("user") == User(0, "root"))
    assert(config.get[Month]("month") == MARCH)
    assert(config.get[String]("string") == "hello")
    assert(config.get[Boolean]("boolean") == true)
    assert(config.get[Int]("int") == 123)
    assert(config.get[Long]("long") == 123456789)
    assert(config.get[Double]("double") == 123.456)
    assert(config.get[Duration]("duration") == Duration.ofSeconds(10))
    assert(config.get[Period]("period") == Period.ofDays(7))
    assert(config.get[ConfigMemorySize]("size") == ConfigMemorySize.ofBytes(10240))

    assert(config.get[Seq[User]]("users").sameElements(users))
    assert(config.get[List[User]]("users").sameElements(users))
    assert(config.get[Set[User]]("users").sameElements(users))
    assert(config.get[Array[User]]("users").sameElements(users))
    assert(config.get[Iterator[User]]("users").toSeq.sameElements(users))

    assert(config.get[Seq[Month]]("months").sameElements(months))
    assert(config.get[List[Month]]("months").sameElements(months))
    assert(config.get[Set[Month]]("months").sameElements(months))
    assert(config.get[Array[Month]]("months").sameElements(months))
    assert(config.get[Iterator[Month]]("months").toSeq.sameElements(months))
  }

  it should "be read with default" in {
    assert(config.getOrElse("user", User(500, "guest")) == User(0, "root"))
    assert(config.getOrElse("month", OCTOBER) == MARCH)
    assert(config.getOrElse("string", "") == "hello")
    assert(config.getOrElse("boolean", false) == true)
    assert(config.getOrElse("int", 0) == 123)
    assert(config.getOrElse("long", 0L) == 123456789)
    assert(config.getOrElse("double", 0.0) == 123.456)
    assert(config.getOrElse("duration", Duration.ofSeconds(0)) == Duration.ofSeconds(10))
    assert(config.getOrElse("period", Period.ofDays(0)) == Period.ofDays(7))
    assert(config.getOrElse("size", ConfigMemorySize.ofBytes(0)) == ConfigMemorySize.ofBytes(10240))
    assert(config.getOrElse("users", List[User]()).sameElements(users))
    assert(config.getOrElse("months", List[Month]()).sameElements(months))

    assert(config.getOrElse("xuser", User(500, "guest")) == User(500, "guest"))
    assert(config.getOrElse("xmonth", OCTOBER) == OCTOBER)
    assert(config.getOrElse("xstring", "") == "")
    assert(config.getOrElse("xboolean", false) == false)
    assert(config.getOrElse("xint", 0) == 0)
    assert(config.getOrElse("xlong", 0L) == 0L)
    assert(config.getOrElse("xdouble", 0.0) == 0.0)
    assert(config.getOrElse("xduration", Duration.ofSeconds(0)) == Duration.ofSeconds(0))
    assert(config.getOrElse("xperiod", Period.ofDays(0)) == Period.ofDays(0))
    assert(config.getOrElse("xsize", ConfigMemorySize.ofBytes(0)) == ConfigMemorySize.ofBytes(0))
    assert(config.getOrElse("xusers", List[User]()) == Nil)
    assert(config.getOrElse("xmonths", List[Month]()) == Nil)
  }

  it should "be optionally read" in {
    assert(config.getOption[User]("user").contains(User(0, "root")))
    assert(config.getOption[Month]("month").contains(MARCH))
    assert(config.getOption[String]("string").contains("hello"))
    assert(config.getOption[Boolean]("boolean").contains(true))
    assert(config.getOption[Int]("int").contains(123))
    assert(config.getOption[Long]("long").contains(123456789))
    assert(config.getOption[Double]("double").contains(123.456))
    assert(config.getOption[Duration]("duration").contains(Duration.ofSeconds(10)))
    assert(config.getOption[Period]("period").contains(Period.ofDays(7)))
    assert(config.getOption[ConfigMemorySize]("size").contains(ConfigMemorySize.ofBytes(10240)))
    assert(config.getOption[Seq[User]]("users").contains(users))
    assert(config.getOption[Seq[Month]]("months").contains(months))

    assert(config.getOption[User]("xuser") == None)
    assert(config.getOption[String]("xstring") == None)
    assert(config.getOption[Boolean]("xboolean") == None)
    assert(config.getOption[Int]("xint") == None)
    assert(config.getOption[Long]("xlong") == None)
    assert(config.getOption[Double]("xdouble") == None)
    assert(config.getOption[Duration]("xduration") == None)
    assert(config.getOption[Period]("xperiod") == None)
    assert(config.getOption[ConfigMemorySize]("xsize") == None)
    assert(config.getOption[List[User]]("xusers") == None)
    assert(config.getOption[List[Month]]("xmonths") == None)
  }

  it should "try to be read" in {
    assert(config.getTry[User]("user").get == User(0, "root"))
    assert(config.getTry[Month]("month").get == MARCH)
    assert(config.getTry[String]("string").get == "hello")
    assert(config.getTry[Boolean]("boolean").get == true)
    assert(config.getTry[Int]("int").get == 123)
    assert(config.getTry[Long]("long").get == 123456789)
    assert(config.getTry[Double]("double").get == 123.456)
    assert(config.getTry[Duration]("duration").get == Duration.ofSeconds(10))
    assert(config.getTry[Period]("period").get == Period.ofDays(7))
    assert(config.getTry[ConfigMemorySize]("size").get == ConfigMemorySize.ofBytes(10240))
    assert(config.getTry[Seq[User]]("users").get.sameElements(users))
    assert(config.getTry[Seq[Month]]("months").get.sameElements(months))

    assert(config.getTry[User]("xuser").isFailure)
    assert(config.getTry[Month]("xmonth").isFailure)
    assert(config.getTry[String]("xstring").isFailure)
    assert(config.getTry[Boolean]("xboolean").isFailure)
    assert(config.getTry[Int]("xint").isFailure)
    assert(config.getTry[Long]("xlong").isFailure)
    assert(config.getTry[Double]("xdouble").isFailure)
    assert(config.getTry[Duration]("xduration").isFailure)
    assert(config.getTry[Period]("xperiod").isFailure)
    assert(config.getTry[ConfigMemorySize]("xsize").isFailure)
    assert(config.getTry[List[User]]("xusers").isFailure)
    assert(config.getTry[List[Month]]("xmonths").isFailure)
  }
}
