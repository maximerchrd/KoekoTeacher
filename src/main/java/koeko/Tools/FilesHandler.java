package koeko.Tools;

import javax.xml.bind.DatatypeConverter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FilesHandler {
    static public String mediaDirectory = "media/";
    static public String mediaDirectoryNoSlash = "media";
    static public String[] supportedMediaExtensions = {"*.mp3", "*.wav", "*.mp4", "*.avi", "*.html"};
    static public String exportDirectory = "questions/";

    static public void createMediaDirIfNotExists() {
        File theDir = new File(mediaDirectory);
        // if the directory does not exist, create it
        if (!theDir.exists()) {
            System.out.println("creating directory: " + theDir.getName());
            boolean result = false;

            try{
                theDir.mkdir();
                result = true;
            }
            catch(SecurityException se){
                //handle it
                se.printStackTrace();
            }
            if(result) {
                System.out.println("DIR created");
            }
        }
    }

    static public Boolean createExportMediaDirIfNotExists() {
        File dir = new File(FilesHandler.exportDirectory + FilesHandler.mediaDirectoryNoSlash);
        if (!dir.exists()) {
            try {
                Files.createDirectories(Paths.get(FilesHandler.exportDirectory + FilesHandler.mediaDirectoryNoSlash));
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    static public File saveMediaFile (File sourceFile) {
        File dest_file = new File(mediaDirectory + sourceFile.getName());
        File hashedFile = new File(mediaDirectory + sourceFile.getName());
        try {
            Files.copy(sourceFile.toPath(), dest_file.toPath(), StandardCopyOption.REPLACE_EXISTING);

            //get file extension
            String[] splitFile = dest_file.toString().split("\\.");
            String extension = "";
            if (splitFile.length > 0) {
                extension = splitFile[splitFile.length - 1];
            }

            //change name to hash
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = Files.readAllBytes(dest_file.toPath());
            messageDigest.update(hashedBytes);
            String encryptedString = DatatypeConverter.printHexBinary(messageDigest.digest());

            List<String> extensions = Arrays.asList(supportedMediaExtensions);

            if (extensions.contains("*." + extension)) {
                encryptedString = encryptedString + "." + extension;
            }
            hashedFile = new File(mediaDirectory + encryptedString);
            Files.move(dest_file.toPath(), hashedFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return hashedFile;
    }

    static public File getMediaFile (String fileName) {
        File mediaFile = new File(mediaDirectory + fileName);
        return mediaFile;
    }

    static public String getFileExtension (String fileName) {
        String[] splitFile = fileName.split(".");
        String extension = "";
        if (splitFile.length > 0) {
            extension = splitFile[splitFile.length - 1];
        }

        return extension;
    }
}
