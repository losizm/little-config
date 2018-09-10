package little

import com.typesafe.config.Config

package object config {
  /** Gets value of type T in Config. */
  trait GetValue[T] {
    /**
     * Gets value of type T at specified path in config.
     *
     * @param config config from which to get value
     * @param path path at which to get value
     */
    def apply(config: Config, path: String): T
  }
}
