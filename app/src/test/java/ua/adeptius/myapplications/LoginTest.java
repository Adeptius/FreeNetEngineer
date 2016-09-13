package ua.adeptius.myapplications;

import org.junit.Test;

import ua.adeptius.myapplications.connection.Network;

import static org.junit.Assert.*;

public class LoginTest {

    @Test
    public void loginFalseTest(){
        assertFalse(Network.isAuthorizationOk("FalseLogin", "FalsePassword"));
    }

    @Test
    public void loginOkTest(){
        if (!Network.isAuthorizationOk("xactive", "36e1a5072c78359066ed7715f5ff3da85b4eadaed0662599d2f1cae336757aa0")){
            assertTrue(Network.isAuthorizationOk("xactive", "780e26bf8d6b06e923ccdac8b2d2f5295b4eadaed0662599d2f1cae336757aa0"));
        }
    }



}