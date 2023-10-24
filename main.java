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

class Simulator{
    private String[] registers = new String[10]; //Store each string value with each index representing a register
    private String[] memory = new String[10]; //Store memory strings values from .dat in memory spots
    
    private void setRegisters(String regValue){
        for(int i = 0; i < registers.length; i++)
        {

        }
    }
    private void setMemory(String memValue){
        for(int i = 0; i < memory.length; i++)
        {

        }
    }
    private void setLoader(String loadValue){
        
    }

    String getRegister(){
        return "";
    }
    String getMemory(){
        return "";
    }
    String getLoader(){
        return "";
    }
}
public class main{
    public static void main(String[] args){
        //Call simulator class in order to update mutators or display accessors

    }   
}