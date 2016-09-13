package ua.adeptius.myapplications;

import org.junit.Test;

import ua.adeptius.myapplications.util.Utilites;

import static org.junit.Assert.assertEquals;

public class UtilitesTest {

    @Test
    public void getMD5Test(){
        assertEquals(Utilites.createMd5("some value"), "5946210c9e93ae37891dfe96c3e39614");
    }
}
