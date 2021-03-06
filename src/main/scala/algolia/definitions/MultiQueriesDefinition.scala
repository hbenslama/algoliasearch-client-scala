/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2016 Algolia
 * http://www.algolia.com/
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package algolia.definitions

import algolia.http.{HttpPayload, POST}
import algolia.inputs._
import algolia.objects.MultiQueries
import algolia.responses.MultiQueriesResult
import algolia.{AlgoliaClient, Executable}
import org.json4s.Formats
import org.json4s.native.Serialization._

import scala.concurrent.{ExecutionContext, Future}

case class MultiQueriesDefinition(definitions: Traversable[SearchDefinition], strategy: Option[MultiQueries.Strategy] = None)(implicit val formats: Formats) extends Definition {

  def strategy(strategy: MultiQueries.Strategy) = copy(strategy = Some(strategy))

  override private[algolia] def build(): HttpPayload = {
    val parameters =
      strategy
        .map(s => Some(Map("strategy" -> s.name)))
        .getOrElse(None)

    HttpPayload(
      POST,
      Seq("1", "indexes", "*", "queries"),
      queryParameters = parameters,
      body = Some(write(MultiQueriesRequests(definitions.map(transform)))),
      isSearch = true
    )
  }

  private def transform(definition: SearchDefinition): MultiQueriesRequest = {
    MultiQueriesRequest(
      indexName = definition.index,
      params = definition.query.map(_.toParam)
    )
  }

}


trait MultiQueriesDefinitionDsl {

  implicit val formats: Formats

  def multiQueries(queries: Traversable[SearchDefinition]): MultiQueriesDefinition = {
    MultiQueriesDefinition(queries)
  }

  def multiQueries(queries: SearchDefinition*): MultiQueriesDefinition = {
    MultiQueriesDefinition(queries)
  }

  implicit object MultiQueriesExecutable extends Executable[MultiQueriesDefinition, MultiQueriesResult] {
    override def apply(client: AlgoliaClient, query: MultiQueriesDefinition)(implicit executor: ExecutionContext): Future[MultiQueriesResult] = {
      client request[MultiQueriesResult] query.build()
    }
  }

}
