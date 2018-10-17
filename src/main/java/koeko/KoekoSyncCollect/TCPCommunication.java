package koeko.KoekoSyncCollect;

import koeko.Koeko;
import koeko.database_management.*;
import koeko.questions_management.Test;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class TCPCommunication {

    // Constants: flow commands and codes
    String cstrOKContinue = "TROK";
    String cstrReceivedOK = "ROOK";
    String cstrTerminator = "+";
    String cstrUploadFIle = "MONT";
    String cstrUploadObjt = "SOBJ";
    String cstrDownldFIle = "DESC";
    String cstrGetSeleWeb = "GSWE";
    String cstrReGetSeleWeb = "RGSW";
    String cstrDelRelQSub = "DRQS";
    String cstrDelRelQObj = "DRQO";
    String cstrDelRelQuTe = "DRQT";
    String cstrSynCol2Web = "SC2W";
    String cstrByeByeDude = "BYEB";
    String cstrDescStep00 = "DSC0";
    String cstrDescStep01 = "DSC1";

    private Socket _socket;
    private String _imagePath;
    private InputStream _inStream;
    private OutputStream _outStream;

    public TCPCommunication(Socket socket, String imagePath) throws IOException {
        this._socket = socket;
        this._imagePath = imagePath;

        // Initialize
        _inStream = _socket.getInputStream();
        _outStream = _socket.getOutputStream();
    }

    public void EndCOmmunication() throws IOException { _socket.close(); }

    /* ********************************************* */
    /*               static utilities                */
    /* ********************************************* */

    public static byte[] ShortToByte(int value) {
        return new byte[]{
                (byte) (value >>> 8),
                (byte) value
        };
    }

    public static byte[] LongToBytes(long l) {
        byte[] result = new byte[8];
        for (int i = 7; i >= 0; i--) {
            result[i] = (byte)(l & 0xFF);
            l >>= 8;
        }
        return result;
    }

    public static long BytesToLong(byte[] b) {
        long result = 0;
        for (int i = 0; i < 8; i++) {
            result <<= 8;
            result |= (b[i] & 0xFF);
        }
        return result;
    }

    public static int ByteToShort(byte[] bytes) {
        return bytes[0]*256 + bytes[1];
    }

    /* ********************************************* */
    /*              utility methods                  */
    /* ********************************************* */

    public InetAddress GetInetAddress(){ return _socket.getInetAddress(); }

    /* ********************************************* */
    /*               methods                         */
    /* ********************************************* */

    // Message received indicating that the last step has been accomplished and
    // that the process can continue
    // Last step is OK, continue
    public boolean IsAcknowledged() throws IOException {
        byte[] rdBuffer = new byte[4];
        int rdb = _inStream.read(rdBuffer);
        String str = new String(rdBuffer);
        return str.equals(cstrOKContinue);
    }

    // Last object sent has been received and processed,
    // the result of that processing of the object is the muid
    // of the object in GLOBAL_COLLECT, other wise, the result is empty
    public String ObjectReceived() throws IOException {
        byte[] rdBuffer = new byte[20];
        int rdb = _inStream.read(rdBuffer);
        String str = new String(rdBuffer);
        if (str.substring(0,4).equals(cstrReceivedOK) && str.substring(19,20).equals(cstrTerminator))
            return str.substring(4,19);
        else
            return "";
    }

    // Send the command to the server
    public void SendCommande(String commande) throws IOException  {
        byte[] cmdBuf = commande.getBytes();
        if (cmdBuf.length != 4)
            return;
        _outStream.write(cmdBuf);
        _outStream.flush();
    }

    // Send the command to the server
    public void SendMUID(String muid) throws IOException {
        byte[] cmdBuf = muid.getBytes();
        if (cmdBuf.length != 15)
            return;
        _outStream.write(cmdBuf);
        _outStream.flush();
    }

    // Send the filename to the server
    public void SendFilename(String filename) throws IOException  {
        int lenBuf = filename.length();

        // Send filename length
        byte[] bytes = TCPCommunication.ShortToByte(lenBuf);
        _outStream.write(bytes);
        _outStream.flush();

        // Send filename
        byte[] cmdBuf = filename.getBytes();
        _outStream.write(cmdBuf);
        _outStream.flush();
    }

    public void SendBinaryFile(String fileName) throws IOException {

        //Specify the file
        File file = new File(_imagePath +fileName);
        FileInputStream fis = new FileInputStream(file);
        BufferedInputStream bis = new BufferedInputStream(fis);

        byte[] len = LongToBytes(file.length());
        _outStream.write(len);
        _outStream.flush();

        //Read File Contents into contents array
        byte[] contents;
        long fileLength = file.length();
        long current = 0;

        // long start = System.nanoTime();
        while(current!=fileLength){
            int size = 10000;
            if(fileLength - current >= size)
                current += size;
            else{
                size = (int)(fileLength - current);
                current = fileLength;
            }
            contents = new byte[size];
            bis.read(contents, 0, size);
            _outStream.write(contents);
            if ((current*100)/fileLength %10 == 0)
                System.out.println("Sending file ... "+(current*100)/fileLength+"% complete!");
        }

        _outStream.flush();
        System.out.println("File sent succesfully!");
    }

    long ReceiveBinaryFile(String fileName)  throws IOException {

        //Initialize socket
        byte[] contents = new byte[10000];

        String filePath = _imagePath + fileName;
        //Initialize the FileOutputStream to the output file's full path.
        FileOutputStream fos = new FileOutputStream(filePath);
        BufferedOutputStream bos = new BufferedOutputStream(fos);

        byte[] lenBuf = new byte[8];
        _inStream.read(lenBuf);
        long fileSize = BytesToLong(lenBuf);

        //No of bytes read in one read() call
        int bytesRead = 0;
        long readTillNow = 0;

        while(readTillNow != fileSize) {
            bytesRead = _inStream.read(contents);
            bos.write(contents, 0, bytesRead);
            readTillNow += bytesRead;
        }

        bos.flush();
        bos.close();
        System.out.println("File saved successfully (size "+fileSize+") !");
        return fileSize;
    }

    public String SendSerializableObject(Object obj) throws IOException {
        SendCommande(cstrUploadObjt);

        //Get socket's output stream and send object
        ObjectOutputStream oos = new ObjectOutputStream(_outStream);
        oos.writeObject(obj);
        oos.flush();

        // Get the acknowledgment that the object has been received
        // and the result of the processing
        String muid = ObjectReceived();
        return muid;
    }

    // Send a file coping with all the exchanges and synchronisation
    public boolean SendFile(String filename) throws IOException {
        // dirty fix because the folder "pictures" is also stored in the filename
        filename = filename.substring(filename.indexOf("/") + 1);

        boolean bOK = false;
        SendCommande(cstrUploadFIle);
        if (IsAcknowledged()) {
            SendFilename(filename);
            if (IsAcknowledged()) {
                SendBinaryFile(filename);
                bOK = IsAcknowledged();
            }
        }
        return bOK;
    }


    // Receive a file coping with all the exchanges and synchronisation
    public boolean ReceiveFile(String filename) throws IOException {
        boolean bOK = false;
        SendCommande(cstrDownldFIle);
        if (IsAcknowledged()) {
            SendFilename(filename);
            if (IsAcknowledged()) {
                ReceiveBinaryFile(filename);
                bOK = true;
            }
        }
        return bOK;
    }

    public boolean GetSelectionFromWEB(String profMuid, Boolean reDownload) {
        boolean bOK = false;
        try {
            if (reDownload) {
                SendCommande(cstrReGetSeleWeb);
            } else {
                SendCommande(cstrGetSeleWeb);
            }
            if (IsAcknowledged()) {
                SendMUID(profMuid);
                if (IsAcknowledged()) {
                    bOK = true;
                }
            }
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        return bOK;
    }

    public boolean RemoveSubjectRelation(String qcmMuid) {
        boolean bOK = false;
        try {
            SendCommande(cstrDelRelQSub);
            if (IsAcknowledged()) {
                SendMUID(qcmMuid);
                if (IsAcknowledged()) {
                    bOK = true;
                }
            }
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        return bOK;
    }

    public boolean RemoveObjectiveRelation(String qcmMuid) {
        boolean bOK = false;
        try {
            SendCommande(cstrDelRelQObj);
            if (IsAcknowledged()) {
                SendMUID(qcmMuid);
                if (IsAcknowledged()) {
                    bOK = true;
                }
            }
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        return bOK;
    }

    public boolean RemoveTestRelation(String testMuid) {
        boolean bOK = false;
        try {
            SendCommande(cstrDelRelQuTe);
            if (IsAcknowledged()) {
                SendMUID(testMuid);
                if (IsAcknowledged()) {
                    bOK = true;
                }
            }
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        return bOK;
    }

    public boolean SyncCollect2WEB() {
        boolean bOK = false;
        try {
            SendCommande(cstrSynCol2Web);
            if (IsAcknowledged()) {
                bOK = true;
            }
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        return bOK;
    }

    private Object GetObject() {
        Object obj = null;
        try {
            ObjectInputStream ois = new ObjectInputStream(_inStream);
            obj = ois.readObject();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        return obj;
    }

    public boolean DownloadSelection() {
        boolean bOK = true;

        try {
            SendCommande(cstrDescStep00);
            Object toDld = GetObject();
            if (toDld.getClass().getName().equals("koeko.view.GlobalToLocal")) {
                koeko.view.GlobalToLocal gtl = (koeko.view.GlobalToLocal)toDld;

                // Download each kind of objects
                SendCommande(cstrDescStep01);
                int cnt = 0;
                while (cnt<gtl.get_nbQuestionMultipleChoice()) {
                    Object rcv = GetObject();
                    if (rcv.getClass().getName().equals("koeko.view.QuestionMultipleChoiceView")) {
                        koeko.view.QuestionMultipleChoiceView qcm = (koeko.view.QuestionMultipleChoiceView)rcv;
                        DbTableQuestionMultipleChoice.addIfNeededMultipleChoiceQuestionFromView(qcm);
                        if (!qcm.getIMAGE().equals("none")) {
                            ReceiveBinaryFile(qcm.getIMAGE());
                        }
                        Koeko.questionSendingControllerSingleton.insertQuestionView(qcm);
                        System.out.println("Received question: " + qcm.getQUESTION());
                    }
                    cnt++;
                }

                cnt = 0;
                while (cnt<gtl.get_nbSubject()) {
                    Object rcv = GetObject();
                    if (rcv.getClass().getName().equals("koeko.view.Subject")) {
                        koeko.view.Subject sbj = (koeko.view.Subject)rcv;
                        DbTableSubject.addIfNeededSubject(sbj);
                    }
                    cnt++;
                }

                cnt = 0;
                while (cnt < gtl.get_nbRelationQcmSbj()) {
                    Object rcv = GetObject();
                    if (rcv.getClass().getName().equals("koeko.view.RelationQuestionSubject")) {
                        koeko.view.RelationQuestionSubject rqs = (koeko.view.RelationQuestionSubject)rcv;
                        DbTableRelationQuestionSubject.addIfNeededRelationQuestionSubject(rqs);
                    }
                    cnt++;
                }

                cnt = 0;
                while (cnt < gtl.get_nbObjectives()) {
                    Object rcv = GetObject();
                    if (rcv.getClass().getName().equals("koeko.view.Objective")) {
                        koeko.view.Objective obj = (koeko.view.Objective)rcv;
                        DbTableLearningObjectives.addObjective(obj);
                    }
                    cnt++;
                }

                cnt = 0;
                while (cnt < gtl.get_nbRelationsQcmObj()) {
                    Object rcv = GetObject();
                    if (rcv.getClass().getName().equals("koeko.view.RelationQuestionObjective")) {
                        koeko.view.RelationQuestionObjective roq = (koeko.view.RelationQuestionObjective)rcv;
                        DbTableRelationQuestionObjective.addRelationQuestionObjective(roq);
                    }
                    cnt++;
                }

                cnt = 0;
                while (cnt < gtl.get_nbRelationsQtoQ()) {
                    Object rcv = GetObject();
                    if (rcv.getClass().getName().equals("koeko.view.RelationQuestionQuestion")) {
                        koeko.view.RelationQuestionQuestion rqq = (koeko.view.RelationQuestionQuestion) rcv;
                        Test test = DbTableTest.getTestWithID(rqq.getTestId());
                        DbTableRelationQuestionQuestion.addRelationQuestionQuestion(rqq.getIdGlobal1(), rqq.getIdGlobal2(),
                                test.getTestName(), rqq.getTestId(), rqq.getCondition());
                    }
                    cnt++;
                }
                bOK = IsAcknowledged();
            } else
                bOK = false;
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
        return bOK;
    }

    public void EndSynchronisation() {
        try {
            SendCommande(cstrByeByeDude);
            _socket.close();
        } catch ( Exception e ) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
            System.exit(0);
        }
    }
}
