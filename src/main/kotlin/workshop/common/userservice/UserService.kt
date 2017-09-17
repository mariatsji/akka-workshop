package workshop.common.userservice

class UserService {

    fun vettUser(loginId: Int?): UserCriminalRecord {
        return if (loginId!! < 100000) {
            UserCriminalRecord.GOOD
        } else {
            UserCriminalRecord.EVIL
        }
    }

}
