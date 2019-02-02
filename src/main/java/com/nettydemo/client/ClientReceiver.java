package com.nettydemo.client;

/**
 * Interface for both AsyncClientHandler and SyncClientHandler,
 * so ClientController class could work with both of them not
 * even knowing which implementation is at the moment
 */
public interface ClientReceiver {
    void messageReceived(String msg);
}
