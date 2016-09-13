package ua.adeptius.myapplications;

import org.junit.Assert;
import org.junit.Test;

import ua.adeptius.myapplications.connection.Network;
import ua.adeptius.myapplications.util.Utilites;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class LoginTest {

    @Test
    public void loginRightTest(){
        assertFalse(Network.isAuthorizationOk("FalseLogin", "FalsePassword"));
    }


}