package it.unipi.dii.inginf.dsmt.covidtracker.intfs;

import it.unipi.dii.inginf.dsmt.covidtracker.communication.CommunicationMessage;

import javax.ejb.Remote;

@Remote
public interface Producer {

    void enqueue(final String consumerName, final CommunicationMessage cMsg);

}
