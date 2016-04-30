package com.gmail.lepeska.martin.udplib.explore;

/**
 *
 * Purpose of this interface is to receive AvailableServerRecords from ExploreRunnable
 * 
 * @author Martin Lepeška
 */
public interface IExploreListener {
    /**
     * Called from ExploreRunnable whenever it obtains information about available server
     * @param record new info about available server
     */
    public void receive(AvailableServerRecord record);
    
    /**
     * Called when ExploreRunnable finished scanning.
     */
    public void finished();
}
