package ua.adeptius.myapplications;


import org.junit.Test;

import ua.adeptius.myapplications.util.Visual;

import static org.junit.Assert.assertEquals;

public class VisualTest {

    @Test
    public void getIconTest(){
        assertEquals(Visual.getIcon("Платная"), 0x7f02006e);
    }
}
