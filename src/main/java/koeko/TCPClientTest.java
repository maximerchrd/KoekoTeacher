package koeko;

import koeko.KoekoSyncCollect.TCPCommunication;
import sun.util.resources.cldr.tg.CalendarData_tg_Cyrl_TJ;

import java.io.IOException;
import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;
import koeko.view.Subject;
import koeko.view.Professor;
import koeko.IniFile;

public class TCPClientTest {

    private Scanner scanner;
    private TCPCommunication _tcpcom;

    private TCPClientTest(InetAddress serverAddress, int serverPort) throws Exception {
        Socket socket = new Socket(serverAddress, serverPort);
        this.scanner = new Scanner(System.in);

        IniFile ini = new IniFile(".\\src\\main\\java\\koeko\\koeko.ini");
        String imagePath = ini.getString("File", "SourceImagePath", ".\\");
        _tcpcom = new TCPCommunication(socket, imagePath);

    }

    public InetAddress getInetAddress() {
        return _tcpcom.GetInetAddress();
    }

    private void SendOneObject(int loopCount) throws IOException  {
        Object toSend = null;
        if (loopCount == 1) {
//            QuestionMultipleChoice qcm = new QuestionMultipleChoice();
//            qcm.setIMAGE("koala.jpg");
//            qcm.setLEVEL("1");
//            qcm.setQUESTION("Quel est l'animal marsupial ressemblant à un ours");
//            qcm.setQCM_MUID("201801010000001");
//            toSend = qcm;
            Professor prf = Professor.createProfessor("1", "AliBaba", "" );
            toSend = prf;
        } else {
            Subject sbj = new Subject("Cryptography", 1, "201801010000002", null);
            toSend = sbj;
        }
        String muid = _tcpcom.SendSerializableObject(toSend);
        System.out.println("Object sent successfully! muid is " + muid);
    }

    private void start() throws IOException {

        String input;
        Boolean bContinue = true;
        int loopCount = 0;

        while (bContinue) {

            input = scanner.nextLine();
            String code = input.substring(0,4);
            String file = input.substring(4);
            switch (code) {
                case "MONT":
                    _tcpcom.SendFile(file);
                    System.out.println("File sent " + file);
                    break;
                case "DESC":
                    _tcpcom.ReceiveFile(file);
                    System.out.println("File received " + file);
                    break;
                case "SOBJ":
                    SendOneObject(loopCount);
                    loopCount++;
                    break;
                case "ROBJ":
                    break;
                default:
                    bContinue = !input.isEmpty();
                    break;
            }
        }
        _tcpcom.EndCOmmunication();
    }

    public static void main(String[] args) throws Exception {
        TCPClientTest client = new TCPClientTest(
                InetAddress.getByName(args[0]),
                Integer.parseInt(args[1]));
        System.out.println("\r\nConnected to Server: " + client.getInetAddress());
        client.start();
    }

}
