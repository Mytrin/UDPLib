package com.gmail.lepeska.martin.udplib.files;

import org.junit.Test;

import static org.junit.Assert.*;

public class ServerSharedFileTest {

    @Test
    public void testRegEx() {
        assertTrue("xxx.txt".matches(ServerSharedTextFile.TEXT_FILES));
        assertTrue("xxx.pdf".matches(ServerSharedTextFile.TEXT_FILES));
        assertTrue("xxx.docx".matches(ServerSharedTextFile.TEXT_FILES));
        
        assertFalse("xxx".matches(ServerSharedTextFile.TEXT_FILES));
        assertFalse("xxx.jar".matches(ServerSharedTextFile.TEXT_FILES));
        assertFalse("xxx.png".matches(ServerSharedTextFile.TEXT_FILES));
    }
    
}