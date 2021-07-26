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
  given stringDelegate: ConfigDelegate[String] with
    def get(config: Config, path: String) = config.getString(path)

  /** Gets `Boolean` from config. */
  given booleanDelegate: ConfigDelegate[Boolean] with
    def get(config: Config, path: String) = config.getBoolean(path)

  /** Gets `Int` from config. */
  given intDelegate: ConfigDelegate[Int] with
    def get(config: Config, path: String) = config.getInt(path)

  /** Gets `Long` from config. */
  given longDelegate: ConfigDelegate[Long] with
    def get(config: Config, path: String) = config.getLong(path)

  /** Gets `Double` from config. */
  given doubleDelegate: ConfigDelegate[Double] with
    def get(config: Config, path: String) = config.getDouble(path)

  /** Gets `Duration` from config. */
  given durationDelegate: ConfigDelegate[Duration] with
    def get(config: Config, path: String) = config.getDuration(path)

  /** Gets `Period` from config. */
  given periodDelegate: ConfigDelegate[Period] with
    def get(config: Config, path: String) = config.getPeriod(path)

  /** Gets `ConfigMemorySize` from config. */
  given memorySizeDelegate: ConfigDelegate[ConfigMemorySize] with
    def get(config: Config, path: String) = config.getMemorySize(path)

  /** Gets `Config` from config. */
  given configDelegate: ConfigDelegate[Config] with
    def get(config: Config, path: String) = config.getConfig(path)

  /**
   * Gets `File` from config.
   *
   * @note This gets a `String` and uses it as a pathname to create a `File`.
   */
  given fileDelegate: ConfigDelegate[File] with
    def get(config: Config, path: String) = File(config.getString(path))

  /** Gets collection `M[T]` from config. */
  given collectionDelegate[T, M[T]](using delegate: ConfigDelegate[T])(using factory: Factory[T, M[T]]): ConfigDelegate[M[T]] =
    new ConfigDelegate[M[T]]:
      def get(config: Config, path: String): M[T] =
        asScala(config.getList(path))
          .map(x => delegate.get(x.atKey("x"), "x"))
          .to(factory)

  private val enumClass = classOf[Enum[_]]
  private val enumValueOf = enumClass.getMethod("valueOf", classOf[Class[_]], classOf[String])

  /** Creates `ConfigDelegate` for getting `Enum` from config. */
  given enumDelegate[T <: Enum[T]](using ctag: ClassTag[T]): ConfigDelegate[T] =
    new ConfigDelegate[T]:
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

    /** Gets `T` at path. */
    def get[T](path: String)(using delegate: ConfigDelegate[T]): T =
      delegate.get(config, path)

    /**
     * Gets `T` at path or returns `default`.
     *
     * @note `delegate` is invoked only if path exists.
     */
    def getOrElse[T](path: String, default: => T)(using delegate: ConfigDelegate[T]): T =
      getOption(path)(using delegate).getOrElse(default)

    /**
     * Optionally gets `T` at path.
     *
     * If path exists, then its value is wrapped in an `Option` and returned;
     * otherwise, `None` is returned.
     */
    def getOption[T](path: String)(using delegate: ConfigDelegate[T]): Option[T] =
      config.hasPath(path) match
        case true  => Option(delegate.get(config, path))
        case false => None

    /** Tries to get `T` at path. */
    def getTry[T](path: String)(using delegate: ConfigDelegate[T]): Try[T] =
      Try(delegate.get(config, path))
