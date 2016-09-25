import com.google.inject.AbstractModule
import net.codingwell.scalaguice.ScalaModule
import offices.TimeEntryOfficeActor
import play.api.libs.concurrent.AkkaGuiceSupport
import views.TimeEntryQueryDatabase

class Module extends AbstractModule with AkkaGuiceSupport with ScalaModule{
  override def configure(): Unit = {
    bind[TimeEntryQueryDatabase].asEagerSingleton()
    bindActor[TimeEntryOfficeActor]("time-entry-office")
  }
}
