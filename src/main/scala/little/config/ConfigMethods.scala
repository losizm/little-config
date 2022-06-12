/*
 * Copyright 2022 Carlos Conyers
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
import java.util as ju

import scala.util.Try

import com.typesafe.config.Config

/** Provides extension methods for `com.typesafe.config.Config`. */
implicit class ConfigMethods(config: Config) extends AnyVal:
  /**
   * Gets `File` at path.
   *
   * @param path config path
   */
  def getFile(path: String): File =
    File(config.getString(path))

  /**
   * Gets `java.util.List[File]` at path.
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
   * Gets `Option[T]` at path.
   *
   * @note `delegate` is invoked only if path exists.
   */
  def getOption[T](path: String)(using delegate: ConfigDelegate[T]): Option[T] =
    config.hasPath(path) match
      case true  => Option(delegate.get(config, path))
      case false => None

  /**
   * Gets `Try[T]` at path.
   *
   * @return `Success[T]` if path exists and its value is successfully
   * converted; `Failure` otherwise
   */
  def getTry[T](path: String)(using delegate: ConfigDelegate[T]): Try[T] =
    Try(delegate.get(config, path))
