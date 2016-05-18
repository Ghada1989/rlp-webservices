package sa.com.xceed.misbar.rlp.webservices;

import org.apache.commons.codec.binary.Hex;
import org.eclipse.jetty.security.MappedLoginService;
import org.eclipse.jetty.server.UserIdentity;
import sa.com.xceed.misbar.dao.MisbarUserMongoDAO;
import sa.com.xceed.misbar.model.files.MisbarUser;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MongoLoginService extends MappedLoginService {

    private MisbarUserMongoDAO misbarUserMongoDAO;
    private int _cacheTime;
    private long _lastHashPurge;

    public MongoLoginService( MisbarUserMongoDAO misbarUserMongoDAO ) {
        setName("Misbar Security Realm");
        this.misbarUserMongoDAO = misbarUserMongoDAO;
        this._cacheTime = 60*60*1000; //one hour cache time
        this._lastHashPurge = 0;
    }

    @Override
    protected UserIdentity loadUser(final String username) {
        MisbarUser user = (MisbarUser)_users.get(username); //Check the cache first
        if(user==null)
        {
            user = misbarUserMongoDAO.getUser(username); //If the user is not in the cache go fetch him from the db
            if(user!=null)
                _users.put(username,user); //And add him to the cache
        }
        return user;
    }

    @Override
    protected void loadUsers()  {
    }

    @Override
    public UserIdentity login(final String username, final Object credentials) {
        // do something here if you need to validate a user when they login

        MessageDigest digest = null;
        String password = null;
        try {
            digest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            //throw new MisbarException(e.getMessage(),MisbarErrorCode.GENERIC_SERVER_ERROR,AddCustomer.class.getName());
        }
        if(credentials!=null)    {
            byte []temp = digest.digest(((String)credentials).getBytes());
            password = new String(Hex.encodeHex(temp));
        }
        long now = System.currentTimeMillis();
        if (now - _lastHashPurge > _cacheTime || _cacheTime == 0)
        {
            _users.clear();
            _lastHashPurge = now;
        }
        return super.login(username, password);
    }

    @Override
    public void logout(final UserIdentity identity) {
        // do something here if you need to invalidate a user when they logout
        super.logout(identity);
    }
}

 