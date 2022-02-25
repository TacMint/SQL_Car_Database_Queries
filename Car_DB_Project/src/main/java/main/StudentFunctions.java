package main;

import hashdb.HashFile;
import hashdb.HashHeader;
import hashdb.Vehicle;
import misc.MutableInteger;
import misc.ReturnCodes;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Array;
import java.util.Arrays;

import static misc.ReturnCodes.RC_NOT_IMPLEMENTED;
import static misc.ReturnCodes.RC_REC_NOT_FOUND;

public class StudentFunctions {
    /**
     * hashCreate
     * This funcAon creates a hash file containing only the HashHeader record.
     * • If the file already exists, return RC_FILE_EXISTS
     * • Create the binary file by opening it.
     * • Write the HashHeader record to the file at RBN 0.
     * • close the file.
     * • return RC_OK.
     */
    public static int hashCreate(String fileName, HashHeader hashHeader) {
        String HashFileName = fileName;
        File file = new File(HashFileName);
        if(file.exists()) {
            System.out.println("File already exists\n");
            return ReturnCodes.RC_FILE_EXISTS;
        }

        try {
            RandomAccessFile inFile = new RandomAccessFile(HashFileName, "rw");
            try {
                inFile.writeInt(hashHeader.getMaxHash());
                inFile.writeInt(hashHeader.getMaxProbe());
                inFile.writeInt(hashHeader.getRecSize());
            }catch(IOException e){

            }
            HashFile hashFile = new HashFile();
            hashFile.setFile(inFile);
            hashFile.setHashHeader(hashHeader);
            try{
                inFile.close();
            }catch (IOException e) {
                e.printStackTrace();
                return ReturnCodes.RC_FILE_NOT_FOUND;
            }
            return ReturnCodes.RC_OK;
        } catch (FileNotFoundException e) {
            System.out.println("\n\nCan't create in hashCreate\n\n");
            return ReturnCodes.RC_FILE_NOT_FOUND;
        }

    }

    /**
     * hashOpen
     * This function opens an existing hash file which must contain a HashHeader record
     * , and sets the file member of hashFile
     * It returns the HashHeader record by setting the HashHeader member in hashFile
     * If it doesn't exist, return RC_FILE_NOT_FOUND.
     * Read the HashHeader record from file and return it through the parameter.
     * If the read fails, return RC_HEADER_NOT_FOUND.
     * return RC_OK
     */
    public static int hashOpen(String fileName, HashFile hashFile) {
     String VehicleFile = fileName;
        File file = new File(VehicleFile);
        if(!file.exists()) {
            //System.out.println("File doesn't exist\n");
            return ReturnCodes.RC_FILE_NOT_FOUND;
            //System.exit(0); // exit in a nicer way than this!!!
        }
        //hashFile.setFile();

        try {
            RandomAccessFile inFile = new RandomAccessFile(VehicleFile, "rw");
            hashFile.setFile(inFile);
            HashHeader header = new HashHeader();
            try {
                header.setMaxHash(inFile.readInt());
                header.setMaxProbe(inFile.readInt());
                header.setRecSize(inFile.readInt());
                hashFile.setHashHeader(header);
            }catch(IOException e){

            }
            int rba = hashFile.getHashHeader().getRecSize();
            return ReturnCodes.RC_OK;

        } catch (FileNotFoundException e) {
            //System.out.println("Can't set file in hashOpen");
            return ReturnCodes.RC_HEADER_NOT_FOUND;
            //e.printStackTrace();
        }
    }

    /**
     * readRec(
     * This function reads a record at the specified RBN in the specified file.
     * Determine the RBA based on RBN and the HashHeader's recSize
     * Use seek to position the file in that location.
     * Read that record and return it through the vehicle parameter.
     * If the location is not found, return RC_LOC_NOT_FOUND.  Otherwise, return RC_OK.
     * Note: if the location is found, that does NOT imply that a vehicle
     * was written to that location.  Why?
      */
    public static int readRec(HashFile hashFile, int rbn, Vehicle vehicle) {
        int rba = rbn * hashFile.getHashHeader().getRecSize();
        try {
            hashFile.getFile().seek(rba);
            byte [] bytes = new byte[Vehicle.sizeOf() * 2];
            hashFile.getFile().read(bytes, 0, Vehicle.sizeOf() *2);
            if(bytes[1] != 0){
                vehicle.fromByteArray(bytes);
            }
            else{
                return ReturnCodes.RC_LOC_NOT_FOUND;
            }

        }catch (IOException | java.nio.BufferUnderflowException e){
            return ReturnCodes.RC_LOC_NOT_FOUND;
        }
        //return ReturnCodes.RC_LOC_NOT_FOUND;
        return ReturnCodes.RC_OK;
    }

    /**
     * writeRec
     * This function writes a record to the specified RBN in the specified file.
     * Determine the RBA based on RBN and the HashHeader's recSize
     * Use seek to position the file in that location.
     * Write that record to the file.
     * If the write fails, return RC_LOC_NOT_WRITTEN.
     * Otherwise, return RC_OK.
     */
    public static int writeRec(HashFile hashFile, int rbn, Vehicle vehicle) {
        int rba = rbn * hashFile.getHashHeader().getRecSize();
        try{
            hashFile.getFile().seek(rba);
            char [] chars = vehicle.toFileChars();
            for(int i = 0; i < chars.length; i++){
                hashFile.getFile().writeChar(chars[i]);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return ReturnCodes.RC_LOC_NOT_FOUND;
        }
        return ReturnCodes.RC_OK;
    }

    /**
     * vehicleInsert
     * This function inserts a vehicle into the specified file.
     * Determine the RBN using the Main class' hash function.
     * Use readRec to read the record at that RBN.
     * If that location doesn't exist
     * OR the record at that location has a blank vehicleId (i.e., empty string):
     * THEN Write this new vehicle record at that location using writeRec.
     * If that record exists and that vehicle's szVehicleId matches, return RC_REC_EXISTS.
     * (Do not update it.)
     * Otherwise, return RC_SYNONYM. a SYNONYM is the same thing as a HASH COLLISION
     * Note that in program #2, we will actually insert synonyms.
     */

    public static int vehicleInsert(HashFile hashFile, Vehicle vehicle) {
        Vehicle temp = new Vehicle();
        int RBN = main.P2Main.hash(vehicle.getVehicleId(), hashFile.getHashHeader().getMaxHash());
        for (int k = 0; k < hashFile.getHashHeader().getMaxProbe(); k++) {
            //System.out.println("TestVehicle.getVehicleID: " + temp.getVehicleIdAsString() + "\n vehicle.getVehicelID: " + vehicle.getVehicleIdAsString());
            int bytesRead = readRec(hashFile, RBN + k, temp);
            if (bytesRead == ReturnCodes.RC_LOC_NOT_FOUND || temp.getVehicleId()[0]==0){
                writeRec(hashFile, RBN + k, vehicle);
                //System.out.println(" VEHICLE INSERTED ");
                System.out.println("");
                return ReturnCodes.RC_OK;
            }
            else if(Arrays.equals(vehicle.getVehicleId(), temp.getVehicleId())) {
                return ReturnCodes.RC_REC_EXISTS;
            }
        }
        return ReturnCodes.RC_TOO_MANY_COLLISIONS;
    }
    /**
     * vehicleRead
     * This function reads the specified vehicle by its vehicleId.
     * Since the vehicleId was provided,
     * determine the RBN using the Main class' hash function.
     * Use readRec to read the record at that RBN.
     * If the vehicle at that location matches the specified vehicleId,
     * return the vehicle via the parameter and return RC_OK.
     * Otherwise, return RC_REC_NOT_FOUND
     */
    /*
                        //PART 1 VEHICLE READ
    public static int vehicleRead(HashFile hashFile, MutableInteger rbn, Vehicle vehicle) {
        String vehicleID = vehicle.getVehicleIdAsString();
        rbn.set(P2Main.hash(vehicle.getVehicleId(), hashFile.getHashHeader().getMaxHash()));
        Vehicle tempVehicle = new Vehicle();
        readRec(hashFile, rbn.intValue(), tempVehicle);

        if (vehicleID.equals(tempVehicle.getVehicleIdAsString())){

            vehicle.setVehicleId(tempVehicle.getVehicleId());
            vehicle.setMake(tempVehicle.getMake());
            vehicle.setYear(Integer.parseInt(tempVehicle.getYearAsString()));
            vehicle.setModel(tempVehicle.getModel());

            return ReturnCodes.RC_OK;
        }
        else { //vehicle is a synonym
            for (int k = 1; k < hashFile.getHashHeader().getMaxProbe(); k++) {
                int bytesRead = readRec(hashFile, rbn.intValue() + k, tempVehicle);
                if (bytesRead != 0 || tempVehicle.getVehicleId()[0]==' ') writeRec(hashFile, rbn.intValue() + k, vehicle);
                else if(Arrays.equals(vehicle.getVehicleId(), tempVehicle.getVehicleId()))
                    return ReturnCodes.RC_REC_EXISTS;
            }
        }

        return ReturnCodes.RC_REC_NOT_FOUND;

    }
*/
    public static int vehicleRead(HashFile hashFile, MutableInteger rbn, Vehicle vehicle) {
        Vehicle temp = new Vehicle();
        int tempRBN = main.P2Main.hash(vehicle.getVehicleId(), hashFile.getHashHeader().getMaxHash());
        for(int k = 0; k < hashFile.getHashHeader().getMaxProbe();k++){
            if((tempRBN + k) > hashFile.getHashHeader().getMaxHash()) return ReturnCodes.RC_REC_NOT_FOUND;
            int read = readRec(hashFile, tempRBN + k, temp);
            if(Arrays.equals(temp.getVehicleId(), vehicle.getVehicleId())) {
                vehicle.setVehicleId(temp.getVehicleId());
                vehicle.setMake(temp.getMake());
                vehicle.setYear(Integer.parseInt(temp.getYearAsString()));
                vehicle.setModel(temp.getModel());

                rbn.set(tempRBN+k);
                return ReturnCodes.RC_OK;
            }
        }
        return ReturnCodes.RC_REC_NOT_FOUND;
    }



    /**
    This function tries to find the given vehicle using its …getVehicleId(). If found, it updates the contents of
    the vehicle in the hash file. If not found, it returns RC_REC_NOT_FOUND. Note that this function must
    understand probing
    NOTE: You can make your life easier with this function if you use MutableInteger and call some of your
    other functions to help out
 */
public static int vehicleUpdate(HashFile hashFile, Vehicle vehicle){
    Vehicle temp=new Vehicle();
    int tempRBN=main.P2Main.hash( vehicle.getVehicleId(), hashFile.getHashHeader().getMaxHash());
    MutableInteger rbn= new MutableInteger(tempRBN);
    int read= vehicleRead(hashFile, rbn, temp);
    //if(read==0){
        writeRec(hashFile, rbn.intValue(), vehicle);
        return ReturnCodes.RC_OK;
    //}
    //return ReturnCodes.RC_REC_NOT_FOUND;
    //return RC_REC_NOT_FOUND;
}

public static int vehicleDelete(HashFile hashFile, char [] vehicleId){
    System.out.println("\ntest\n");
    return ReturnCodes.RC_NOT_IMPLEMENTED;
}



}























