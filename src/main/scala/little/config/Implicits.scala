/*
 * Copyright 2021 Carlos Conyers
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

import scala.collection.Factory
import scala.jdk.javaapi.CollectionConverters.asScala
import scala.language.higherKinds
import scala.reflect.ClassTag
import scala.util.Try

import com.typesafe.config.{ Config, ConfigMemorySize }

/** Provides implicit conversions and extension methods. */
object Implicits:
  /** Gets `String` from config. */
  given stringValuator: ConfigValuator[String] with
    def get(config: Config, path: String) = config.getString(path)

  /** Gets `Boolean` from config. */
  given booleanValuator: ConfigValuator[Boolean] with
    def get(config: Config, path: String) = config.getBoolean(path)

  /** Gets `Int` from config. */
  given intValuator: ConfigValuator[Int] with
    def get(config: Config, path: String) = config.getInt(path)

  /** Gets `Long` from config. */
  given longValuator: ConfigValuator[Long] with
    def get(config: Config, path: String) = config.getLong(path)

  /** Gets `Double` from config. */
  given doubleValuator: ConfigValuator[Double] with
    def get(config: Config, path: String) = config.getDouble(path)

  /** Gets `Duration` from config. */
  given durationValuator: ConfigValuator[Duration] with
    def get(config: Config, path: String) = config.getDuration(path)

  /** Gets `Period` from config. */
  given periodValuator: ConfigValuator[Period] with
    def get(config: Config, path: String) = config.getPeriod(path)

  /** Gets `ConfigMemorySize` from config. */
  given memorySizeValuator: ConfigValuator[ConfigMemorySize] with
    def get(config: Config, path: String) = config.getMemorySize(path)

  /** Gets `Config` from config. */
  given configValuator: ConfigValuator[Config] with
    def get(config: Config, path: String) = config.getConfig(path)

  /**
   * Gets `File` from config.
   *
   * @note This gets a `String` and uses it as a pathname to create a `File`.
   */
  given fileValuator: ConfigValuator[File] with
    def get(config: Config, path: String) = File(config.getString(path))

  /** Gets collection `M[T]` from config. */
  given collectionValuator[T, M[T]](using valuator: ConfigValuator[T], factory: Factory[T, M[T]]): ConfigValuator[M[T]] =
    new ConfigValuator[M[T]]:
      def get(config: Config, path: String): M[T] =
        asScala(config.getList(path))
          .map(x => valuator.get(x.atKey("x"), "x"))
          .to(factory)

  private val enumClass = classOf[Enum[_]]
  private val enumValueOf = enumClass.getMethod("valueOf", classOf[Class[_]], classOf[String])

  /** Creates `ConfigValuator` for getting `Enum` from config. */
  given enumValuator[T <: Enum[T]](using ctag: ClassTag[T]): ConfigValuator[T] =
    new ConfigValuator[T]:
      def get(config: Config, path: String): T =
        enumValueOf.invoke(enumClass, ctag.runtimeClass, config.getString(path)).asInstanceOf[T]

  /** Adds extension methods to `com.typesafe.config.Config`. */
  implicit class ConfigType(config: Config) extends AnyVal:
    /**
     * Gets value as `File`.
     *
     * @param path config path
     */
    def getFile(path: String): File =
      File(config.getString(path))

    /**
     * Gets value as `java.util.List[File]`.
     *
     * @param path config path
     */
    def getFileList(path: String): ju.List[File] =
      val files = ju.LinkedList[File]()
      config.getStringList(path).forEach { value =>
        files.add(File(value))
      }
      files

    /** Gets `T` value at path. */
    def get[T](path: String)(using valuator: ConfigValuator[T]): T =
      valuator.get(config, path)

    /**
     * Gets `T` value at path or returns `default`.
     *
     * @note `valuator` is invoked only if path exists.
     */
    def getOrElse[T](path: String, default: => T)(using valuator: ConfigValuator[T]): T =
      getOption(path)(using valuator).getOrElse(default)

    /**
     * Optionally gets `T` value at path.
     *
     * If path exists, then its value is wrapped in an `Option` and returned;
     * otherwise `None` is returned.
     */
    def getOption[T](path: String)(using valuator: ConfigValuator[T]): Option[T] =
      config.hasPath(path) match
        case true  => Option(valuator.get(config, path))
        case false => None

    /** Tries to get `T` value at path. */
    def getTry[T](path: String)(using valuator: ConfigValuator[T]): Try[T] =
      Try(valuator.get(config, path))
