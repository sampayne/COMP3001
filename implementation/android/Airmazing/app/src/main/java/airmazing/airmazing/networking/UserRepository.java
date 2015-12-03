package airmazing.airmazing.networking;

/**
 * Created by Sam on 02/12/2015.
 */

import com.strongloop.android.loopback.User;

public class UserRepository extends com.strongloop.android.loopback.UserRepository<User> {

    public interface LoginCallBack extends com.strongloop.android.loopback.UserRepository.LoginCallback<User> {

    }

    public UserRepository(){

        super("airmazinguser", null, User.class);

    }

}
