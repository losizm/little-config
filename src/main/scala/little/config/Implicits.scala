/*
 * Copyright 2020 Carlos Conyers
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

import java.io.File
import java.time.{ Duration, Period }
import java.{ util => ju }

import scala.language.higherKinds
import scala.reflect.ClassTag
import scala.util.Try

import com.typesafe.config.{ Config, ConfigMemorySize }

/** Provides implicit values and types. */
object Implicits {
  /** Gets `String` from config. */
  implicit val stringValuator: ConfigValuator[String] =
    (config, path) => config.getString(path)

  /** Gets `Boolean` from config. */
  implicit val booleanValuator: ConfigValuator[Boolean] =
    (config, path) => config.getBoolean(path)

  /** Gets `Int` from config. */
  implicit val intValuator: ConfigValuator[Int] =
    (config, path) => config.getInt(path)

  /** Gets `Long` from config. */
  implicit val longValuator: ConfigValuator[Long] =
    (config, path) => config.getLong(path)

  /** Gets `Double` from config. */
  implicit val doubleValuator: ConfigValuator[Double] =
    (config, path) => config.getDouble(path)

  /** Gets `Duration` from config. */
  implicit val durationValuator: ConfigValuator[Duration] =
    (config, path) => config.getDuration(path)

  /** Gets `Period` from config. */
  implicit val periodValuator: ConfigValuator[Period] =
    (config, path) => config.getPeriod(path)

  /** Gets `ConfigMemorySize` from config. */
  implicit val memorySizeValuator: ConfigValuator[ConfigMemorySize] =
    (config, path) => config.getMemorySize(path)

  /**
   * Gets `File` from config.
   *
   * @note This gets a `String` and uses it as a pathname to create a `File`.
   */
  implicit val fileValuator: ConfigValuator[File] =
    (config, path) => new File(config.getString(path))

  /** Gets collection `M[T]` from config. */
  implicit def collectionValuator[T, M[T]](implicit valuator: ConfigValuator[T], factory: Factory[T, M[T]]) =
    CollectionValuator(valuator, factory)

  private val enumClass = classOf[Enum[_]]
  private val enumValueOf = enumClass.getMethod("valueOf", classOf[Class[_]], classOf[String])

  /** Creates `ConfigValuator` for getting `Enum` from config. */
  implicit def enumValuator[T <: Enum[T]](implicit ctag: ClassTag[T]) =
    new ConfigValuator[T] {
      def get(config: Config, path: String): T =
        enumValueOf.invoke(enumClass, ctag.runtimeClass, config.getString(path)).asInstanceOf[T]
    }

  /** Adds extension methods to `com.typesafe.config.Config`. */
  implicit class ConfigType(private val config: Config) extends AnyVal {
    /**
     * Gets value as `File`.
     *
     * @param path config path
     */
    def getFile(path: String): File =
      new File(config.getString(path))

    /**
     * Gets value as `java.util.List[File]`.
     *
     * @param path config path
     */
    def getFileList(path: String): ju.List[File] = {
      val files = new ju.LinkedList[File]
      config.getStringList(path).forEach { value =>
        files.add(new File(value))
      }
      files
    }

    /** Gets `T` value at path. */
    def get[T](path: String)(implicit valuator: ConfigValuator[T]): T =
      valuator.get(config, path)

    /**
     * Gets `T` value at path or returns `default`.
     *
     * @note `valuator` is invoked only if path exists.
     */
    def getOrElse[T](path: String, default: => T)(implicit valuator: ConfigValuator[T]): T =
      getOption(path)(valuator).getOrElse(default)

    /**
     * Optionally gets `T` value at path.
     *
     * If path exists, then its value is wrapped in an `Option` and returned;
     * otherwise `None` is returned.
     */
    def getOption[T](path: String)(implicit valuator: ConfigValuator[T]): Option[T] =
      config.hasPath(path) match {
        case true  => Option(valuator.get(config, path))
        case false => None
      }

    /** Tries to get `T` value at path. */
    def getTry[T](path: String)(implicit valuator: ConfigValuator[T]): Try[T] =
      Try(valuator.get(config, path))
  }
}
