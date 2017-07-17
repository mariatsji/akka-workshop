package workshop.userservice;

public class UserService {

    public UserCriminalRecord vettUser(Integer loginId) {
        if (loginId < 100000) {
            return UserCriminalRecord.GOOD;
        } else {
            return UserCriminalRecord.EVIL;
        }
    }

}
