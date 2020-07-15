package controllers

import domain.Id
import domain.order.{Order, OrderStatus}
import javax.inject._
import play.api.libs.json._
import play.api.mvc._
import services.OrderService
import services.OrderService.PlaceOrderCommand
import strangepackage.WeirdClass

@Singleton
class OrderController @Inject() (
  service: OrderService,
  val controllerComponents: ControllerComponents
) extends BaseController {
  import OrderController._

  def get(rawOrderId: String): Action[AnyContent] =
    Action { implicit request =>
      service.findOrder(Id(rawOrderId))
        .map(order => Ok(Json.toJson(order)))
        .getOrElse(NotFound)
    }

  def post(): Action[JsValue] =
    Action(parse.json) { implicit request =>
      request.body.validate[PlaceOrderCommand].fold(
        _ => BadRequest,
        command => {
          service.placeOrder(command)
          Created
        }
      )
    }
}

object OrderController {

  private implicit def idReads[T]: Reads[Id[T]] = Reads.StringReads.map(Id[T])
  private implicit def idWrites[T]: Writes[Id[T]] = Writes.StringWrites.contramap(_.value)
  private implicit def commandReads: Reads[PlaceOrderCommand] = Json.reads[PlaceOrderCommand]
  private implicit def orderStatusWrites: Writes[OrderStatus] =
    Writes {
      case OrderStatus.Placed    => JsString("placed")
      case OrderStatus.Completed => JsString("completed")
    }
  private implicit val weirdClassWrites: Writes[WeirdClass] = Writes(_ => JsString("weird"))
  private implicit def orderWrites: Writes[Order] = Json.writes[Order]
}
