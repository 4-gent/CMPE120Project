/* File Name main.java
 * 
 * This is the main file that will hold the main class for display
 * 
 * The simulator class will hold all methods that will be called in the main method
 * class simulator
 *  - register methods
 *  - loader methods
 *  - memory methods
 * 
 * the main class will call simulator as an object for encapsulation purposes
 * main class will read in the contents of the .dat file and load the information which will then linearly update each register/memory
 * 
 */

import java.io.*;
import java.util.*;

class Simulator{
    private static final int INSTRUCTION_MEMORY_SIZE = 1024;
    private static final int DATA_MEMORY_SIZE = 1024;

    private int[] registers = new int[32]; //Store each string value with each index representing a register
    private int[] dataMemory = new int[DATA_MEMORY_SIZE]; //Store memory strings values from .dat in memory spots
    private int[] instructionMemory = new int[INSTRUCTION_MEMORY_SIZE];

    private int pc = 0;
    private List<Integer> breakpoints = new ArrayList<>();
    private boolean isRunning = false;

    //load the instructions from the file
    private void loader(String filePath){
        try{
            //use buffered reader to read in file contents
            BufferedReader reader = new BufferedReader(new FileReader("filePathhere"));
            String line;
            int i = 0;
            while((line = reader.readLine()) != null && i < INSTRUCTION_MEMORY_SIZE)
            {
                instructionMemory[i] = Integer.parseInt(line, 16);
                i++;
            }
        }catch(IOException e){
            e.printStackTrace();
        }
    }

    private void setRegister(int registerIndex, int value){
        if(registerIndex < 0 || registerIndex >= registers.length)
            throw new IllegalArgumentException("Invalid Register Index");
        registers[registerIndex] = value;
    }
    private void setMemory(int address, int value){
        if(address < 0 || address >=dataMemory.length)
            throw new IllegalArgumentException("Invalid Memory Index");
        dataMemory[address] = value;
    }

    private int getRegister(int registerIndex){
        if(registerIndex < 0 || registerIndex >= registers.length)
            throw new IllegalArgumentException("Invalid Memory Address");
        return registers[registerIndex];
    }
    private int getMemory(int address){
        if(address < 0 || address >= dataMemory.length)
            throw new IllegalArgumentException("Invalid Memory Address");
        return dataMemory[address];
    }
}
public class main{
    public static void main(String[] args){
        //Call simulator class in order to update mutators or display accessors

    }   
}