package servermodel.routes.masterroute

import akka.http.scaladsl.server.{Directives, Route}
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.{Content, Schema}
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import javax.ws.rs.core.MediaType
import javax.ws.rs.{Consumes, POST, Path, Produces}
import servermodel.routes.subroute.RisultatoRoute._

/**
 * @author Francesco Cassano
 * This object manage routes that act on the Risultato entity and its related entities
 */
object MasterRouteRisultato extends Directives {

  @Path("/replaceshift")
  @POST
  @Consumes(Array(MediaType.APPLICATION_JSON))
  @Produces(Array(MediaType.APPLICATION_JSON))
  @Operation(summary = "Replace shift", description = "Reassign a shift to another employee",
    requestBody = new RequestBody(content = Array(new Content(schema = new Schema(implementation = classOf[(Int, Int)])))),
    responses = Array(
      new ApiResponse(responseCode = "200", description = "replace success"),
      new ApiResponse(responseCode = "500", description = "Internal server error"))
  )
  def replaceShift(): Route =
    path("replaceshift") {
      updateShift()
    }

  val routeRisultato: Route =
    concat(
      replaceShift()
    )
}