package controllers

import domain.Id
import domain.model.customer.{Customer, CustomerName}
import javax.inject._
import play.api.libs.json.{JsValue, Json, OWrites, Reads}
import play.api.mvc.{Action, AnyContent, BaseController, ControllerComponents}
import services.{ComplicatedService, CustomerService}
import services.CustomerService.RegisterCustomerCommand

@Singleton
class CustomerController @Inject() (
  service: CustomerService,
  complicatedService: ComplicatedService,
  val controllerComponents: ControllerComponents
) extends BaseController {
  import CustomerController._

  def get(rawCustomerId: String): Action[AnyContent] =
    Action { implicit request =>
      service.findCustomer(Id(rawCustomerId)).map(customer => Ok(Json.toJson(customer)))
        .getOrElse(NotFound)
    }

  def post(): Action[JsValue] =
    Action(parse.json) { implicit request =>
      request.body.validate[RegisterCustomerCommand].fold(
        _ => BadRequest,
        command => {
          service.registerCustomer(command)
          Created
        }
      )
    }

  def put(): Action[AnyContent] =
    Action { implicit request =>
      val answer = complicatedService.calcAnswer(100)
      Ok(Json.obj("answer" -> answer))
    }
}

object CustomerController extends BaseFormats {

  implicit val customerNameWrites: OWrites[CustomerName] = Json.writes[CustomerName]
  implicit val customerWrites: OWrites[Customer] = Json.writes[Customer]
  implicit val registerCustomerCommandReads: Reads[RegisterCustomerCommand] =
    Json.reads[RegisterCustomerCommand]
}
