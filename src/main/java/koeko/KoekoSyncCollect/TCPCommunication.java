package koeko.KoekoSyncCollect;

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
                ReceiveFile(filename);
                bOK = true;
            }
        }
        return bOK;
    }
}
