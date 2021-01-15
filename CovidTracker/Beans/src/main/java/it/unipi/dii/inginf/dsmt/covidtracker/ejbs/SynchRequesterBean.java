package it.unipi.dii.inginf.dsmt.covidtracker.ejbs;

import com.google.gson.Gson;
import it.unipi.dii.inginf.dsmt.covidtracker.communication.AggregationRequest;
import it.unipi.dii.inginf.dsmt.covidtracker.communication.AggregationResponse;
import it.unipi.dii.inginf.dsmt.covidtracker.communication.CommunicationMessage;
import it.unipi.dii.inginf.dsmt.covidtracker.enums.MessageType;
import it.unipi.dii.inginf.dsmt.covidtracker.intfs.SynchRequester;

import javax.ejb.Stateless;
import javax.jms.*;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

@Stateless(name = "SynchRequesterEJB")
public class SynchRequesterBean implements SynchRequester {

    static final String QC_FACTORY_NAME = "jms/__defaultConnectionFactory";
    JMSContext myJMSContext; //initialized in constructor
    Context ic;              //initialized in constructor
    TemporaryQueue tmpQueue; //initialized in constructor

    Gson gson = new Gson();

    public SynchRequesterBean() {
        try{
            ic = new InitialContext();
            QueueConnectionFactory qcf = (QueueConnectionFactory)ic.lookup(QC_FACTORY_NAME);
            myJMSContext = qcf.createContext();
            tmpQueue = myJMSContext.createTemporaryQueue();
        }
        catch (NamingException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public AggregationResponse requestAndReceiveAggregation(final String consumerName, final AggregationRequest requestMsg) throws Exception {
        CommunicationMessage outMsg = new CommunicationMessage();
        outMsg.setMessageType(MessageType.AGGREGATION_REQUEST);
        outMsg.setSenderName("tmp"); // handle "tmp" sender differently than others
        outMsg.setMessageBody(gson.toJson(requestMsg, AggregationRequest.class));

        Message inMsg = requestAndReceive(consumerName, outMsg);
        CommunicationMessage cMsg = (CommunicationMessage) ((ObjectMessage) inMsg).getObject();
        return gson.fromJson(cMsg.getMessageBody(), AggregationResponse.class);
    }

    //------------------------------------------------------------------------------------------------------------------

    /**
     * @return the next message produced for this JMSConsumer,
     *         or null if the timeout expires or this JMSConsumer is concurrently closed
     */
    private Message requestAndReceive(final String consumerName, final CommunicationMessage cMsg) throws JMSException, NamingException {
        ObjectMessage outMsg = myJMSContext.createObjectMessage();
        outMsg.setObject(cMsg);
        outMsg.setJMSReplyTo(tmpQueue);
        Queue consumerQueue = (Queue)ic.lookup(consumerName);
        myJMSContext.createProducer().send(consumerQueue, outMsg);
        return myJMSContext.createConsumer(tmpQueue).receive(1000);  // receive with a 1 second timeout
    }
}
