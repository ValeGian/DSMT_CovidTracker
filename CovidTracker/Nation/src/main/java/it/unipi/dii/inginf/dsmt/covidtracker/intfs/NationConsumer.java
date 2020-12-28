package it.unipi.dii.inginf.dsmt.covidtracker.intfs;

import it.unipi.dii.inginf.dsmt.covidtracker.communication.CommunicationMessage;
import it.unipi.dii.inginf.dsmt.covidtracker.communication.DailyReport;
import javafx.util.Pair;

import javax.ejb.Local;
import java.util.List;

@Local
public interface NationConsumer {

    void initializeParameters(String name, List<String> childrenAreas);

    List<DailyReport> handleDailyReport(CommunicationMessage cMsg);

    Pair<String, CommunicationMessage> handleConnectionRequest(CommunicationMessage cMsg);

}