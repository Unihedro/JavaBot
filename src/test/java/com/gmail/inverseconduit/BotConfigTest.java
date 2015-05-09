package com.gmail.inverseconduit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Properties;

import org.junit.Test;

/**
 * @author Michael Angstadt
 */
public class BotConfigTest {

    @Test
    public void defaults() {
        Properties props = new Properties();
        BotConfig config = new BotConfig(props);
        assertNull(config.getLoginEmail());
        assertNull(config.getLoginPassword());
        assertEquals("!!", config.getTrigger());
        assertEquals(Paths.get("javadocs"), config.getJavadocsDir());
        assertEquals(Arrays.asList(1), config.getRooms());
        assertEquals(SESite.STACK_OVERFLOW, config.getSite());
    }

    @Test
    public void values() {
        Properties props = new Properties();
        props.setProperty("LOGIN-EMAIL", "email");
        props.setProperty("PASSWORD", "password");
        props.setProperty("TRIGGER", "**");
        props.setProperty("JAVADOCS", "dir");
        props.setProperty("ROOMS", "1,2 , 3");
        props.setProperty("SITE", "meta.stackexchange");

        BotConfig config = new BotConfig(props);
        assertEquals("email", config.getLoginEmail());
        assertEquals("password", config.getLoginPassword());
        assertEquals("**", config.getTrigger());
        assertEquals(Paths.get("dir"), config.getJavadocsDir());
        assertEquals(Arrays.asList(1, 2, 3), config.getRooms());
        assertEquals(SESite.META_STACK_EXCHANGE, config.getSite());
    }

    @Test()
    public void invalid_room_gets_ignored() {
        Properties props = new Properties();
        props.setProperty("ROOMS", "1,foo,#123");

        BotConfig config = new BotConfig(props);

        assertEquals(1, config.getRooms().size());
        assertEquals((Integer) 1, config.getRooms().get(0));
    }

    @Test
    public void site_ignores_casing() {
        Properties props = new Properties();
        props.setProperty("SITE", "STACKEXCHANGE");

        BotConfig config = new BotConfig(props);

        assertEquals(SESite.STACK_EXCHANGE, config.getSite());
    }
}
