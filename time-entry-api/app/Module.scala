import com.google.inject.AbstractModule
import net.codingwell.scalaguice.ScalaModule
import offices.TimeEntryOfficeActor
import play.api.libs.concurrent.AkkaGuiceSupport
import views.{SNSPublisher, TimeEntryQueryDatabase}

class Module extends AbstractModule with AkkaGuiceSupport with ScalaModule {
  override def configure(): Unit = {
    bind[TimeEntryQueryDatabase].asEagerSingleton()
    bind[SNSPublisher].asEagerSingleton()
    bindActor[TimeEntryOfficeActor]("time-entry-office")
  }
}
