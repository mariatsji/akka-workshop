package workshop.common.userservice

open class UserService {

    open fun vettUser(loginId: Int?): UserCriminalRecord {
        return if (loginId!! < 100000) {
            UserCriminalRecord.GOOD
        } else {
            UserCriminalRecord.EVIL
        }
    }

}
