package com.gmail.lepeska.martin.udplib.files;

import org.junit.Test;

import static org.junit.Assert.*;

public class ServerSharedFileTest {

    @Test
    public void testRegEx() {
        assertTrue("xxx.txt".matches(SharedTextFile.TEXT_FILES));
        assertTrue("xxx.pdf".matches(SharedTextFile.TEXT_FILES));
        assertTrue("xxx.docx".matches(SharedTextFile.TEXT_FILES));
        
        assertFalse("xxx".matches(SharedTextFile.TEXT_FILES));
        assertFalse("xxx.jar".matches(SharedTextFile.TEXT_FILES));
        assertFalse("xxx.png".matches(SharedTextFile.TEXT_FILES));
    }
    
}