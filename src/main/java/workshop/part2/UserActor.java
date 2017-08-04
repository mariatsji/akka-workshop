package workshop.part2;

import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;
import workshop.common.userservice.UserCriminalRecord;
import workshop.common.userservice.UserService;

public class UserActor extends AbstractActor {

    private final UserService userService;

    public UserActor(UserService userService) {
        this.userService = userService;
    }

    @Override
    public Receive createReceive() {
        return ReceiveBuilder.create()
            .match(CheckUser.class, m -> {
                UserCriminalRecord result = userService.vettUser(m.userId);
                sender().tell(new CheckUserResult(result), sender());
            })
            .build();
    }

    static class CheckUserResult {
        final UserCriminalRecord record;

        public CheckUserResult(UserCriminalRecord record) {
            this.record = record;
        }
    }

    static class CheckUser {
        final Integer userId;

        public CheckUser(Integer userId) {
            this.userId = userId;
        }
    }
}
