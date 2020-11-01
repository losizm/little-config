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
package little

import scala.collection.JavaConverters.asScalaBuffer
import scala.language.higherKinds

import com.typesafe.config.Config

package object config {
  /** Defines alias to `scala.collection.generic.CanBuildFrom`. */
  type Factory[-Elem, +To] = scala.collection.generic.CanBuildFrom[_, Elem, To]

  @inline
  private[config] def CollectionValuator[T, M[T]](valuator: ConfigValuator[T], factory: Factory[T, M[T]]) =
    new ConfigValuator[M[T]] {
      def get(config: Config, path: String): M[T] =
        asScalaBuffer(config.getList(path))
          .map(x => valuator.get(x.atKey("x"), "x"))
          .to(factory)
    }
}
