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

import java.time.{ Duration, Period }

import scala.collection.Factory
import scala.jdk.CollectionConverters.IterableHasAsScala
import scala.language.higherKinds
import scala.reflect.ClassTag
import scala.util.Try

import com.typesafe.config.{ Config, ConfigMemorySize }

/** Provides implicit values and types. */
object Implicits {
  /** Gets String from Config. */
  implicit val getConfigValueAsString: GetConfigValue[String] =
    (config, path) => config.getString(path)

  /** Gets Boolean from Config. */
  implicit val getConfigValueAsBoolean: GetConfigValue[Boolean] =
    (config, path) => config.getBoolean(path)

  /** Gets Int from Config. */
  implicit val getConfigValueAsInt: GetConfigValue[Int] =
    (config, path) => config.getInt(path)

  /** Gets Long from Config. */
  implicit val getConfigValueAsLong: GetConfigValue[Long] =
    (config, path) => config.getLong(path)

  /** Gets Double from Config. */
  implicit val getConfigValueAsDouble: GetConfigValue[Double] =
    (config, path) => config.getDouble(path)

  /** Gets Duration from Config. */
  implicit val getConfigValueAsDuration: GetConfigValue[Duration] =
    (config, path) => config.getDuration(path)

  /** Gets Period from Config. */
  implicit val getConfigValueAsPeriod: GetConfigValue[Period] =
    (config, path) => config.getPeriod(path)

  /** Gets ConfigMemorySize from Config. */
  implicit val getConfigValueAsMemorySize: GetConfigValue[ConfigMemorySize] =
    (config, path) => config.getMemorySize(path)

  /** Gets collection M[T] from Config. */
  implicit def getConfigValueAsCollection[T, M[T]](implicit get: GetConfigValue[T], factory: Factory[T, M[T]]) =
    new GetConfigValue[M[T]] {
      def apply(config: Config, path: String): M[T] =
        config.getList(path).asScala.map(x => get(x.atKey("x"), "x")).to(factory)
    }

  private val enumClass = classOf[Enum[_]]
  private val enumValueOf = enumClass.getMethod("valueOf", classOf[Class[_]], classOf[String])

  /** Creates GetConfigValue for getting Enum from Config. */
  implicit def getConfigValueAsEnum[T <: Enum[T]](implicit ctag: ClassTag[T]) =
    new GetConfigValue[T] {
      def apply(config: Config, path: String): T =
        enumValueOf.invoke(enumClass, ctag.runtimeClass, config.getString(path)).asInstanceOf[T]
    }

  /** Adds extension methods to {@code com.typesafe.config.Config}. */
  implicit class ConfigType(val config: Config) extends AnyVal {
    /** Gets value of type T at path. */
    def get[T](path: String)(implicit get: GetConfigValue[T]): T =
      get(config, path)

    /**
     * Gets value of type T at path or returns evaluated default.
     *
     * <strong>Note:</strong> {@code get} is invoked only if path exists.
     */
    def getOrElse[T](path: String, default: => T)(implicit get: GetConfigValue[T]): T =
      getOption(path)(get).getOrElse(default)

    /**
     * Optionally gets value of type T at path.
     *
     * If path exists, then value of type T wrapped in {@code Option} is
     * returned; otherwise {@code None} is returned.
     *
     * <strong>Note:</strong> If {@code get} returned {@code null}, then
     * {@code None} is returned ultimately.
     */
    def getOption[T](path: String)(implicit get: GetConfigValue[T]): Option[T] =
      if (config.hasPath(path)) Option(get(config, path))
      else None

    /** Tries to get value of type T at path. */
    def getTry[T](path: String)(implicit get: GetConfigValue[T]): Try[T] =
      Try(get(config, path))
  }
}
