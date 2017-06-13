package workshop.userservice;

import javaslang.control.Either;
import javaslang.control.Option;

public class UserServiceImpl implements UserService {

    @Override
    public Either<Throwable, Option<UserCriminalRecord>> vettUser(Long loginId) {
        if (loginId < 100000) {
            return Either.right(Option.of(UserCriminalRecord.GOOD));
        } else if (loginId < 200000) {
            return Either.right(Option.of(UserCriminalRecord.EVIL));
        } else {
            return Either.right(Option.none());
        }
    }


}
