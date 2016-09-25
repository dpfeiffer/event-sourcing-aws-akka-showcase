import com.google.inject.AbstractModule
import offices.TimeEntryOfficeActor
import play.api.libs.concurrent.AkkaGuiceSupport

class Module extends AbstractModule with AkkaGuiceSupport{
  override def configure(): Unit = {
    bindActor[TimeEntryOfficeActor]("time-entry-office")
  }
}
