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

import java.time.{ Duration, Period }

import scala.collection.convert.ImplicitConversionsToScala.`iterable AsScalaIterable`
import scala.collection.generic.CanBuildFrom
import scala.language.higherKinds
import scala.util.Try

import com.typesafe.config.{ Config, ConfigMemorySize }

/** Provides implicit values and types. */
object Implicits {
  /** Gets String from Config. */
  implicit val GetString: GetValue[String] =
    (config, path) => config.getString(path)

  /** Gets Boolean from Config. */
  implicit val GetBoolean: GetValue[Boolean] =
    (config, path) => config.getBoolean(path)

  /** Gets Int from Config. */
  implicit val GetInt: GetValue[Int] =
    (config, path) => config.getInt(path)

  /** Gets Long from Config. */
  implicit val GetLong: GetValue[Long] =
    (config, path) => config.getLong(path)

  /** Gets Long from Config. */
  implicit val GetDouble: GetValue[Double] =
    (config, path) => config.getDouble(path)

  /** Gets Duration from Config. */
  implicit val GetDuration: GetValue[Duration] =
    (config, path) => config.getDuration(path)

  /** Gets Period from Config. */
  implicit val GetPeriod: GetValue[Period] =
    (config, path) => config.getPeriod(path)

  /** Gets ConfigMemorySize from Config. */
  implicit val GetMemorySize: GetValue[ConfigMemorySize] =
    (config, path) => config.getMemorySize(path)

  /** Gets collection M[T] from Config. */
  implicit def GetCollection[T, M[T]](implicit gv: GetValue[T], build: CanBuildFrom[Nothing, T, M[T]]) = new GetValue[M[T]] {
    def apply(config: Config, path: String): M[T] =
      config.getList(path).map(x => gv(x.atKey("x"), "x")).to[M]
  }

  /** Adds extension methods to {@code com.typesafe.config.Config}. */
  implicit class ConfigType(val config: Config) extends AnyVal {
    /** Gets value of type T at path. */
    def get[T](path: String)(implicit gv: GetValue[T]): T =
      gv(config, path)

    /**
     * Gets value of type T at path or returns evaluated default.
     *
     * <strong>Note:</strong> {@code gv} is invoked only if path exists.
     */
    def getOrElse[T](path: String, default: => T)(implicit gv: GetValue[T]): T =
      getOption(path)(gv).getOrElse(default)

    /**
     * Optionally gets value of type T at path.
     *
     * If path exists, then value of type T wrapped in {@code Option} is
     * returned; otherwise {@code None} is returned.
     *
     * <strong>Note:</strong> If {@code gv} returned {@code null}, then
     * {@code None} is returned ultimately.
     */
    def getOption[T](path: String)(implicit gv: GetValue[T]): Option[T] =
      if (config.hasPath(path)) Option(gv(config, path))
      else None

    /** Tries to get value of type T at path. */
    def getTry[T](path: String)(implicit gv: GetValue[T]): Try[T] =
      Try(gv(config, path))
  }
}
