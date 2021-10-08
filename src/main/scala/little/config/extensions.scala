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
import java.{ util => ju }

import scala.util.Try

import com.typesafe.config.Config

/** Adds extension methods to `com.typesafe.config.Config`. */
implicit class ConfigExt(config: Config) extends AnyVal:
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
