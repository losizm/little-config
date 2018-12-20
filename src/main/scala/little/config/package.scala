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
package little

import com.typesafe.config.Config

package object config {
  /**
   * Gets value of type T from Config.
   *
   * {{{
   * import com.typesafe.config.ConfigFactory
   * import little.config.GetConfigValue
   * import little.config.Implicits.ConfigType
   *
   * case class User(id: Int, name: String)
   *
   * // Define how to get User from Config
   * implicit val getUser: GetConfigValue[User] = { (config, path) =>
   *   val user = config.getConfig(path)
   *   User(user.getInt("id"), user.getString("name"))
   * }
   *
   * val config = ConfigFactory.parseString("""user { id = 0,  name = root }""")
   *
   * // Get User from Config
   * val user = config.get[User]("user")
   * }}}
   *
   * @see [[Implicits.ConfigType ConfigType]]
   */
  trait GetConfigValue[T] {
    /**
     * Gets value of type T at specified path in config.
     *
     * @param config config from which to get value
     * @param path path at which to get value
     */
    def apply(config: Config, path: String): T
  }
}
