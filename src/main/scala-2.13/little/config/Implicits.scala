/*
 * Copyright 2019 Carlos Conyers
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
import scala.jdk.CollectionConverters.IterableHasAsScala
import scala.language.higherKinds
import scala.reflect.ClassTag
import scala.util.Try

import com.typesafe.config.{ Config, ConfigMemorySize }

/** Provides implicit values and types. */
object Implicits {
  /** Gets String from Config. */
  implicit val stringValuator: ConfigValuator[String] =
    (config, path) => config.getString(path)

  /** Gets Boolean from Config. */
  implicit val booleanValuator: ConfigValuator[Boolean] =
    (config, path) => config.getBoolean(path)

  /** Gets Int from Config. */
  implicit val intValuator: ConfigValuator[Int] =
    (config, path) => config.getInt(path)

  /** Gets Long from Config. */
  implicit val longValuator: ConfigValuator[Long] =
    (config, path) => config.getLong(path)

  /** Gets Double from Config. */
  implicit val doubleValuator: ConfigValuator[Double] =
    (config, path) => config.getDouble(path)

  /** Gets Duration from Config. */
  implicit val durationValuator: ConfigValuator[Duration] =
    (config, path) => config.getDuration(path)

  /** Gets Period from Config. */
  implicit val periodValuator: ConfigValuator[Period] =
    (config, path) => config.getPeriod(path)

  /** Gets ConfigMemorySize from Config. */
  implicit val memorySizeValuator: ConfigValuator[ConfigMemorySize] =
    (config, path) => config.getMemorySize(path)

  /**
   * Gets File from Config.
   *
   * This is a convenience method that first gets a String value and then
   * creates a File from it.
   */
  implicit val fileValuator: ConfigValuator[File] =
    (config, path) => new File(config.getString(path))

  /** Gets collection M[T] from Config. */
  implicit def collectionValuator[T, M[T]](implicit valuator: ConfigValuator[T], factory: Factory[T, M[T]]) =
    new ConfigValuator[M[T]] {
      def get(config: Config, path: String): M[T] =
        config.getList(path).asScala.map(x => valuator.get(x.atKey("x"), "x")).to(factory)
    }

  private val enumClass = classOf[Enum[_]]
  private val enumValueOf = enumClass.getMethod("valueOf", classOf[Class[_]], classOf[String])

  /** Creates ConfigValuator for getting Enum from Config. */
  implicit def enumValuator[T <: Enum[T]](implicit ctag: ClassTag[T]) =
    new ConfigValuator[T] {
      def get(config: Config, path: String): T =
        enumValueOf.invoke(enumClass, ctag.runtimeClass, config.getString(path)).asInstanceOf[T]
    }

  /** Adds extension methods to {@code com.typesafe.config.Config}. */
  implicit class ConfigType(private val config: Config) extends AnyVal {
    /**
     * Gets value as `File`.
     *
     * @param path config path
     */
    def getFile(path: String): File =
      new File(config.getString(path))

    /**
     * Gets list value as `File` elements.
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

    /** Gets value of type T at path. */
    def get[T](path: String)(implicit valuator: ConfigValuator[T]): T =
      valuator.get(config, path)

    /**
     * Gets value of type T at path or returns evaluated default.
     *
     * <strong>Note:</strong> {@code valuator} is invoked only if path exists.
     */
    def getOrElse[T](path: String, default: => T)(implicit valuator: ConfigValuator[T]): T =
      getOption(path)(valuator).getOrElse(default)

    /**
     * Optionally gets value of type T at path.
     *
     * If path exists, then value of type T wrapped in {@code Option} is
     * returned; otherwise {@code None} is returned.
     *
     * <strong>Note:</strong> If {@code valuator} returned {@code null}, then
     * {@code None} is returned ultimately.
     */
    def getOption[T](path: String)(implicit valuator: ConfigValuator[T]): Option[T] =
      if (config.hasPath(path)) Option(valuator.get(config, path))
      else None

    /** Tries to get value of type T at path. */
    def getTry[T](path: String)(implicit valuator: ConfigValuator[T]): Try[T] =
      Try(valuator.get(config, path))
  }
}
