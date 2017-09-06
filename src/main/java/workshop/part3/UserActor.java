package workshop.part3;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import scala.PartialFunction;
import scala.runtime.BoxedUnit;
import workshop.common.userservice.UserCriminalRecord;
import workshop.common.userservice.UserService;

public class UserActor extends AbstractActor {

    private final UserService userService;

    public UserActor(UserService userService) {
        this.userService = userService;
    }

    @Override
    public PartialFunction<Object, BoxedUnit> receive() {
        return ReceiveBuilder.create()
            .match(CheckUser.class, m -> {
                UserCriminalRecord result = userService.vettUser(m.userId);
                sender().tell(new CheckUserResult(result), sender());
            })
            .build();
    }

    public static class CheckUserResult {
        public final UserCriminalRecord record;

        public CheckUserResult(UserCriminalRecord record) {
            this.record = record;
        }
    }

    public static class CheckUser {
        public final Integer userId;

        public CheckUser(Integer userId) {
            this.userId = userId;
        }
    }
}
