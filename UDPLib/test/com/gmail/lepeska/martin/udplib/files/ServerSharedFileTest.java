/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.gmail.lepeska.martin.udplib.files;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author mytrin
 */
public class ServerSharedFileTest {

    @Test
    public void testRegEx() {
        assertTrue("xxx.txt".matches(ServerSharedFile.TEXT_FILES));
        assertTrue("xxx.pdf".matches(ServerSharedFile.TEXT_FILES));
        assertTrue("xxx.docx".matches(ServerSharedFile.TEXT_FILES));
        
        assertFalse("xxx".matches(ServerSharedFile.TEXT_FILES));
        assertFalse("xxx.jar".matches(ServerSharedFile.TEXT_FILES));
        assertFalse("xxx.png".matches(ServerSharedFile.TEXT_FILES));
    }
    
}
