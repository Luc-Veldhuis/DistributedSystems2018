import akka.actor.OneForOneStrategy;
import akka.actor.SupervisorStrategy;
import akka.actor.SupervisorStrategyConfigurator;
import akka.japi.pf.DeciderBuilder;
import scala.concurrent.duration.Duration;

//TODO remoting with heartbeats, make sure the application.conf is loaded

public class CustomSupervisorStrategyConfigurator implements SupervisorStrategyConfigurator {
    private static SupervisorStrategy strategy =
            new OneForOneStrategy(-1, Duration.Inf(),
                    DeciderBuilder
                            .match(GracefulFailureException.class, e -> SupervisorStrategy.stop())
                            .matchAny(o -> SupervisorStrategy.escalate())
                            .build());

    @Override
    public SupervisorStrategy create() {
        return strategy;
    }
}
