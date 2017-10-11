package workshop.part2;

import akka.actor.AbstractActor;
import workshop.common.userservice.UserCriminalRecord;
import workshop.common.userservice.UserService;

public class UserActor extends AbstractActor {

    private final UserService userService;

    public UserActor(UserService userService) {
        this.userService = userService;
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
            .build();
    }

    // Reply message type
    public static class CheckUserResult {
        public final UserCriminalRecord record;

        public CheckUserResult(UserCriminalRecord record) {
            this.record = record;
        }
    }

    // Request message type
    public static class CheckUser {
        public final Integer userId;

        public CheckUser(Integer userId) {
            this.userId = userId;
        }
    }
}
