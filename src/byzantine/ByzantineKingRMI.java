package byzantine;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * This is the interface of Byzantine RMI call. You should implement each method defined below.
 * Round1 is the RMI that proposer sends prepare request to acceptors.
 * Round2 is the RMI that proposer sends accept request to acceptors.
 * Decide is the RMI that proposer broadcasts decision once consensus reaches.
 * Please don't change the interface.
 */
public interface ByzantineKingRMI extends Remote{
    Response Round1(Request req) throws RemoteException;
    Response Round2(Request req) throws RemoteException;
    Response Round3(Request req) throws RemoteException;
    Response Receive(Request req) throws RemoteException;
}