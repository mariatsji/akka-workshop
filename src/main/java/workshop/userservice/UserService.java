package workshop.userservice;

import javaslang.control.Either;
import javaslang.control.Option;

public interface UserService {

    Either<Throwable, Option<UserCriminalRecord>> vettUser(Long loginId);

}
