/* File Name: Main.java
 *
 * This is the main file that will hold the main class for display.
 * The simulator class will hold all methods that will be called in the main method.
 * 
 * Class Simulator:
 * - Contains register methods
 * - Contains loader methods
 * - Contains memory methods
 *
 * The main class will call the Simulator as an object for encapsulation purposes.
 * The main class will read in the contents of the .dat file and load the information, 
 * which will then linearly update each register/memory.
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Simulator {
    private static final int INSTRUCTION_MEMORY_SIZE = 1024;
    private static final int DATA_MEMORY_SIZE = 1024;

    private int[] registers = new int[32];
    private int[] dataMemory = new int[DATA_MEMORY_SIZE];
    private int[] instructionMemory = new int[INSTRUCTION_MEMORY_SIZE];

    private int pc = 0;
    private List<Integer> breakpoints = new ArrayList<>();
    private boolean isRunning = false;
    private long startTime;

    // Main method
    public static void main(String[] args) {
        Simulator simulator = new Simulator();
        simulator.loadInstructions("arith_mean.dat");
        simulator.start();
    }

    // Load instructions from a file into the instruction memory
    private void loadInstructions(String filePath) {
        int i = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null && i < INSTRUCTION_MEMORY_SIZE) {
                // Trim leading/trailing whitespaces and remove non-binary characters
                line = line.trim().replaceAll("[^01]", "");
    
                if (!line.isEmpty()) {
                    // Print the binary instruction before parsing
                    System.out.println("Binary Instruction: " + line);
    
                    // Parse binary instruction and store in instruction memory
                    long instructionValue = Long.parseLong(line, 2);
                    
                    // Print the parsed instruction value
                    System.out.println("Parsed Instruction Value: " + instructionValue);
    
                    // Ensure the value fits within the integer range
                    instructionMemory[i] = (int) instructionValue;
                    i++;
                }
            }
            System.out.println("Program loading is complete.");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NumberFormatException e) {
            System.err.println("Error parsing instruction at line " + (i + 1));
            e.printStackTrace();
        }
    }
        
    // Start the simulator
    private void start() {
        Scanner scanner = new Scanner(System.in);

        // User command loop
        while (true) {
            displayOptions();
            System.out.print("Enter your choice: ");
            String choice = scanner.nextLine().trim();

            if (choice.equals("exit")) {
                break;
            }

            executeCommand(choice);
        }

        scanner.close();
    }

    // Display user command options
    private void displayOptions() {
        System.out.println("Options:");
        System.out.println("1. 'r' - Run the entire program");
        System.out.println("2. 's' - Run the next instruction and stop");
        System.out.println("3. 'c' - Continue execution");
        System.out.println("4. 'insn' - Print the assembly of the next instruction");
        System.out.println("5. 'pc' - Return the value of the PC");
        System.out.println("6. 'x[0-31]' - Return the contents of the specified register");
        System.out.println("7. '0x[address]' - Return the contents from the specified memory address");
        System.out.println("8. 'b [pc]' - Set a breakpoint at a specific [pc]");
        System.out.println("9. 'exit' - Exit the program");
    }

    // Execute user command
    private void executeCommand(String command) {
        switch (command) {
            case "r":
                isRunning = true;
                startTimer();
                runWithBreakpoints();
                break;
            case "s":
                isRunning = true;
                startTimer();
                step();
                break;
            case "c":
                isRunning = true;
                startTimer();
                continueExecution();
                break;
            case "insn":
                System.out.println("Assembly of the instruction: " + getInstructionAssembly());
                break;
            case "pc":
                System.out.println("PC: " + pc);
                break;
            default:
                handleExtendedCommands(command);
                break;
        }
    }

    private void handleExtendedCommands(String command) {
        if (command.startsWith("x")) {
            int registerIndex = Integer.parseInt(command.substring(1));
            printRegisterValue(registerIndex);
        } else if (command.startsWith("0x")) {
            int address = Integer.parseInt(command.substring(2), 16);
            printMemoryValue(address);
        } else if (command.startsWith("b")) {
            int breakpointAddress = Integer.parseInt(command.substring(2));
            setBreakpoint(breakpointAddress);
            System.out.println("Breakpoint set at PC = " + breakpointAddress);
        } else {
            System.out.println("Unhandled command: " + command);
        }
    }    

    private void printRegisterValue(int registerIndex) {
        if (registerIndex < 0 || registerIndex >= registers.length)
            throw new IllegalArgumentException("Invalid Register Index");
    
        System.out.println("Value of x" + registerIndex + ": " + getRegister(registerIndex));
    }
    
    private void printMemoryValue(int address) {
        validateMemoryAddress(address);
        System.out.println("Value at address 0x" + Integer.toHexString(address) + ": " + memoryReadWord(address));
    }
    

    // Run the entire program
    private void runWithBreakpoints() {
        while (isRunning && pc < INSTRUCTION_MEMORY_SIZE && !breakpoints.contains(pc)) {
            executeInstruction();
        }
    
        if (pc >= INSTRUCTION_MEMORY_SIZE) {
            System.out.println("Program execution completed.");
        } else {
            System.out.println("Breakpoint hit at PC = " + pc);
        }
    
        stopTimer();
    }    

    private void step() {
        if (pc < INSTRUCTION_MEMORY_SIZE) {
            executeInstruction();
        } else {
            System.out.println("Program has already finished.");
        }
    }

    // Continue execution until the next breakpoint or the program ends
    // Continue execution until the next breakpoint or the program ends
    private void continueExecution() {
        while (isRunning && pc < INSTRUCTION_MEMORY_SIZE && (!breakpoints.contains(pc) || pc == INSTRUCTION_MEMORY_SIZE - 1)) {
            executeInstruction();
            
            // Check if the current PC is a breakpoint
            if (breakpoints.contains(pc)) {
                System.out.println("Breakpoint hit at PC = " + pc);
                break;
            }
        }

        if (pc >= INSTRUCTION_MEMORY_SIZE) {
            System.out.println("Program execution completed.");
        }

        stopTimer();
    }

    private void executeInstruction() {

        if (pc >= INSTRUCTION_MEMORY_SIZE) {
            System.out.println("End of instruction memory reached. Program execution completed.");
            stopTimer();
            return;
        }
        int instruction = instructionMemory[pc];
        int opcode = instruction & 0x7F; // Extract opcode bits
        System.out.println("Opcode before sign-extension: " + opcode);
        if ((instruction & 0x80000000) != 0) {
            opcode |= 0xFFFFFF80; // Sign-extend for negative values
        }
        System.out.println("Opcode after sign-extension: " + opcode);
        int rd = (instruction >>> 7) & 0x1F;        // Extract destination register bits
        int funct3 = (instruction >>> 12) & 0x7;    // Extract funct3 bits
        int rs1 = (instruction >>> 15) & 0x1F;      // Extract source register 1 bits
        int rs2 = (instruction >>> 20) & 0x1F;      // Extract source register 2 bits
        int funct7 = (instruction >>> 25);          // Extract funct7 bits for some instructions
        int immI = signExtend(instruction >>> 20, 12); // Extract immediate value for I-type instructions
        int immS = signExtend(((instruction >>> 25) << 5) | (instruction >>> 7), 12); // Extract immediate value for S-type instructions
        int immB = signExtend(((instruction >>> 31) << 11) | ((instruction >>> 7) << 5) | ((instruction >>> 25) << 1) | ((instruction >>> 8) << 12), 13); // Extract immediate value for B-type instructions
        int immU = (instruction & 0xFFFFF000);     // Extract immediate value for U-type instructions
        int immJ = signExtend(((instruction >>> 31) << 19) | ((instruction >>> 12) << 11) | ((instruction >>> 20) << 1) | ((instruction >>> 21) << 12), 21); // Extract immediate value for J-type instructions

        switch (opcode) {
            case 0x37: // LUI
                registers[rd] = immU;
                break;
    
            case 0x17: // AUIPC
                registers[rd] = pc + immU;
                break;
    
            case 0x6F: // JAL
                registers[rd] = pc + 4; // Store the return address in rd
                int offset = (immJ & 0x80000000) != 0 ? (immJ | 0xFFFFF000) : immJ;
                pc += offset; // Jump to the calculated address
                break;
            
    
            case 0x67: // JALR
                int jumpAddr = (registers[rs1] + immI) & 0xFFFFFFFE; // Make sure it's even
                registers[rd] = pc + 4;
                pc = jumpAddr - 1;
                break;
    
            case 0x63: // Branch Instructions (BEQ, BNE, BLT, BGE, BLTU, BGEU)
                switch (funct3) {
                    case 0x0: // BEQ
                        if (registers[rs1] == registers[rs2]) {
                            pc += immB;
                        }
                        break;
                    case 0x1: // BNE
                        if (registers[rs1] != registers[rs2]) {
                            pc += immB;
                        }
                        break;
                    case 0x4: // BLT
                        if (registers[rs1] < registers[rs2]) {
                            pc += immB;
                        }
                        break;
                    case 0x5: // BGE
                        if (registers[rs1] >= registers[rs2]) {
                            pc += immB;
                        }
                        break;
                    case 0x6: // BLTU
                        if (Integer.compareUnsigned(registers[rs1], registers[rs2]) < 0) {
                            pc += immB;
                        }
                        break;
                    case 0x7: // BGEU
                        if (Integer.compareUnsigned(registers[rs1], registers[rs2]) >= 0) {
                            pc += immB;
                        }
                        break;
                    default:
                        handleUnsupportedOpcode(opcode);
                        break;
                }
                break;        
    
            case 0x03: // Load Instructions (LB, LH, LW, LBU, LHU)
                switch (funct3) {
                    case 0x0: // LB
                        registers[rd] = signExtend(memoryReadByte(registers[rs1] + immI), 8);
                        break;
                    case 0x1: // LH
                        registers[rd] = signExtend(memoryReadHalfWord(registers[rs1] + immI), 8);
                        break;
                    case 0x2: // LW
                        registers[rd] = memoryReadWord(registers[rs1] + immI);
                        break;
                    case 0x4: // LBU
                        registers[rd] = memoryReadByte(registers[rs1] + immI) & 0xFF;
                        break;
                    case 0x5: // LHU
                        registers[rd] = memoryReadHalfWord(registers[rs1] + immI) & 0xFFFF;
                        break;
    
                    default:
                        handleUnsupportedOpcode(opcode);
                        break;
                }
                break;
    
            case 0x23: // Store Instructions (SB, SH, SW)
                switch (funct3) {
                    case 0x0: // SB
                        memoryWriteByte(registers[rs1] + immS, registers[rs2]);
                        break;
                    case 0x1: // SH
                        memoryWriteHalfWord(registers[rs1] + immS, registers[rs2]);
                        break;
                    case 0x2: // SW
                        memoryWriteWord(registers[rs1] + immS, registers[rs2]);
                        break;
    
                    default:
                        handleUnsupportedOpcode(opcode);
                        break;
                }
                break;
    
            case 0x13: // Immediate Instructions (ADDI, SLTI, SLTIU, XORI, ORI, ANDI)
                switch (funct3) {
                    case 0x0: // ADDI
                        registers[rd] = registers[rs1] + immI;
                        break;
                    case 0x2: // SLTI
                        registers[rd] = (registers[rs1] < immI) ? 1 : 0;
                        break;
                    case 0x3: // SLTIU
                        registers[rd] = (Integer.compareUnsigned(registers[rs1], immI) < 0) ? 1 : 0;
                        break;
                    case 0x4: // XORI
                        registers[rd] = registers[rs1] ^ immI;
                        break;
                    case 0x6: // ORI
                        registers[rd] = registers[rs1] | immI;
                        break;
                    case 0x7: // ANDI
                        registers[rd] = registers[rs1] & immI;
                        break;
    
                    default:
                        handleUnsupportedOpcode(opcode);
                        break;
                }
                break;
    
            case 0x33: // Register-Register Instructions (ADD, SUB, SLL, SLT, SLTU, XOR, SRL, SRA, OR, AND)
                switch (funct3) {
                    case 0x0:
                        switch (funct7) {
                            case 0x0: // ADD
                                registers[rd] = registers[rs1] + registers[rs2];
                                break;
                            case 0x20: // SUB
                                registers[rd] = registers[rs1] - registers[rs2];
                                break;
                            default:
                                handleUnsupportedOpcode(opcode);
                                break;
                        }
                        break;
                    case 0x1: // SLL
                        registers[rd] = registers[rs1] << (registers[rs2] & 0x1F);
                        break;
                    case 0x2: // SLT
                        registers[rd] = (registers[rs1] < registers[rs2]) ? 1 : 0;
                        break;
                    case 0x3: // SLTU
                        registers[rd] = (Integer.compareUnsigned(registers[rs1], registers[rs2]) < 0) ? 1 : 0;
                        break;
                    case 0x4: // XOR
                        registers[rd] = registers[rs1] ^ registers[rs2];
                        break;
                    case 0x5: // SRL
                        registers[rd] = registers[rs1] >>> (registers[rs2] & 0x1F);
                        break;
                    case 0x20: // SRA
                        registers[rd] = registers[rs1] >> (registers[rs2] & 0x1F);
                        break;
                    case 0x6: // OR
                        registers[rd] = registers[rs1] | registers[rs2];
                        break;
                    case 0x7: // AND
                        registers[rd] = registers[rs1] & registers[rs2];
                        break;
    
                    default:
                        handleUnsupportedOpcode(opcode);
                        break;
                }
                break;
    
            default:
                handleUnsupportedOpcode(opcode);
                break;
        }
    
        pc++;
        logInstruction();
    }
    
    private void handleUnsupportedOpcode(int opcode) {
        System.err.println("Unsupported opcode: " + opcode);
        // Handle the unsupported opcode as needed (throw an exception, exit the program, etc.)
    }
    // Get the assembly of the next instruction
    private String getInstructionAssembly() {
        int instruction = instructionMemory[pc];
        int opcode = instruction & 0x7F;   // Extract opcode bits
        int rd = (instruction >>> 7) & 0x1F;        // Extract destination register bits
        int funct3 = (instruction >>> 12) & 0x7;    // Extract funct3 bits
        int rs1 = (instruction >>> 15) & 0x1F;      // Extract source register 1 bits
        int rs2 = (instruction >>> 20) & 0x1F;      // Extract source register 2 bits
        int funct7 = (instruction >>> 25);          // Extract funct7 bits for some instructions
        int immI = signExtend(instruction >>> 20, 12); // Extract immediate value for I-type instructions
        int immS = signExtend(((instruction >>> 25) << 5) | (instruction >>> 7), 12); // Extract immediate value for S-type instructions
        int immB = signExtend(((instruction >>> 31) << 11) | ((instruction >>> 7) << 5) | ((instruction >>> 25) << 1) | ((instruction >>> 8) << 12), 13); // Extract immediate value for B-type instructions
        int immU = (instruction & 0xFFFFF000);     // Extract immediate value for U-type instructions
        int immJ = signExtend(((instruction >>> 31) << 19) | ((instruction >>> 12) << 11) | ((instruction >>> 20) << 1) | ((instruction >>> 21) << 12), 21); // Extract immediate value for J-type instructions
    
        switch (opcode) {
            case 0x37: // LUI
                return "LUI x" + rd + ", " + immU;
    
            case 0x17: // AUIPC
                return "AUIPC x" + rd + ", " + immU;
    
            case 0x6F: // JAL
                return "JAL x" + rd + ", " + (pc + immJ);
    
            case 0x67: // JALR
                return "JALR x" + rd + ", x" + rs1 + ", " + immI;
    
            case 0x63: // Branch Instructions (BEQ, BNE, BLT, BGE, BLTU, BGEU)
                switch (funct3) {
                    case 0x0: // BEQ
                        return "BEQ x" + rs1 + ", x" + rs2 + ", " + (pc + immB);
                    case 0x1: // BNE
                        return "BNE x" + rs1 + ", x" + rs2 + ", " + (pc + immB);
                    case 0x4: // BLT
                        return "BLT x" + rs1 + ", x" + rs2 + ", " + (pc + immB);
                    case 0x5: // BGE
                        return "BGE x" + rs1 + ", x" + rs2 + ", " + (pc + immB);
                    case 0x6: // BLTU
                        return "BLTU x" + rs1 + ", x" + rs2 + ", " + (pc + immB);
                    case 0x7: // BGEU
                        return "BGEU x" + rs1 + ", x" + rs2 + ", " + (pc + immB);
                    default:
                        return "Unsupported opcode: " + opcode;
                }
    
            case 0x03: // Load Instructions (LB, LH, LW, LBU, LHU)
                switch (funct3) {
                    case 0x0: // LB
                        return "LB x" + rd + ", " + immI + "(x" + rs1 + ")";
                    case 0x1: // LH
                        return "LH x" + rd + ", " + immI + "(x" + rs1 + ")";
                    case 0x2: // LW
                        return "LW x" + rd + ", " + immI + "(x" + rs1 + ")";
                    case 0x4: // LBU
                        return "LBU x" + rd + ", " + immI + "(x" + rs1 + ")";
                    case 0x5: // LHU
                        return "LHU x" + rd + ", " + immI + "(x" + rs1 + ")";
                    default:
                        return "Unsupported opcode: " + opcode;
                }
    
            case 0x23: // Store Instructions (SB, SH, SW)
                switch (funct3) {
                    case 0x0: // SB
                        return "SB x" + rs2 + ", " + immS + "(x" + rs1 + ")";
                    case 0x1: // SH
                        return "SH x" + rs2 + ", " + immS + "(x" + rs1 + ")";
                    case 0x2: // SW
                        return "SW x" + rs2 + ", " + immS + "(x" + rs1 + ")";
                    default:
                        return "Unsupported opcode: " + opcode;
                }
    
            case 0x13: // Immediate Instructions (ADDI, SLTI, SLTIU, XORI, ORI, ANDI, SLLI, SRLI, SRAI)
                switch (funct3) {
                    case 0x0: // ADDI
                        return "ADDI x" + rd + ", x" + rs1 + ", " + immI;
                    case 0x2: // SLTI
                        return "SLTI x" + rd + ", x" + rs1 + ", " + immI;
                    case 0x3: // SLTIU
                        return "SLTIU x" + rd + ", x" + rs1 + ", " + immI;
                    case 0x4: // XORI
                        return "XORI x" + rd + ", x" + rs1 + ", " + immI;
                    case 0x6: // ORI
                        return "ORI x" + rd + ", x" + rs1 + ", " + immI;
                    case 0x7: // ANDI
                        return "ANDI x" + rd + ", x" + rs1 + ", " + immI;
                    case 0x1: // SLLI
                        return "SLLI x" + rd + ", x" + rs1 + ", " + (immI & 0x1F);
                    case 0x5: // SRLI, SRAI
                        switch (funct7) {
                            case 0x0: // SRLI
                                return "SRLI x" + rd + ", x" + rs1 + ", " + (immI & 0x1F);
                            case 0x20: // SRAI
                                return "SRAI x" + rd + ", x" + rs1 + ", " + (immI & 0x1F);
                            default:
                                return "Unsupported opcode: " + opcode;
                        }
                    default:
                        return "Unsupported opcode: " + opcode;
                }
    
            case 0x33: // Register-Register Instructions (ADD, SUB, SLL, SLT, SLTU, XOR, SRL, SRA, OR, AND)
                switch (funct3) {
                    case 0x0: // ADD, SUB
                        switch (funct7) {
                            case 0x0: // ADD
                                return "ADD x" + rd + ", x" + rs1 + ", x" + rs2;
                            case 0x20: // SUB
                                return "SUB x" + rd + ", x" + rs1 + ", x" + rs2;
                            default:
                                return "Unsupported opcode: " + opcode;
                        }
                    case 0x1: // SLL
                        return "SLL x" + rd + ", x" + rs1 + ", x" + rs2;
                    case 0x2: // SLT
                        return "SLT x" + rd + ", x" + rs1 + ", x" + rs2;
                    case 0x3: // SLTU
                        return "SLTU x" + rd + ", x" + rs1 + ", x" + rs2;
                    case 0x4: // XOR
                        return "XOR x" + rd + ", x" + rs1 + ", x" + rs2;
                    case 0x5: // SRL, SRA
                        switch (funct7) {
                            case 0x0: // SRL
                                return "SRL x" + rd + ", x" + rs1 + ", x" + rs2;
                            case 0x20: // SRA
                                return "SRA x" + rd + ", x" + rs1 + ", x" + rs2;
                            default:
                                return "Unsupported opcode: " + opcode;
                        }
                    case 0x6: // OR
                        return "OR x" + rd + ", x" + rs1 + ", x" + rs2;
                    case 0x7: // AND
                        return "AND x" + rd + ", x" + rs1 + ", x" + rs2;
                    default:
                        return "Unsupported opcode: " + opcode;
                }
    
            default:
                return "Unsupported opcode: " + opcode;
        }
    }

    // Log the current instruction to a file
    private void logInstruction() {
        try (FileWriter writer = new FileWriter("assembly.asm", true)) {
            writer.write("PC: " + pc + ", " + getInstructionAssembly() + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Start the execution timer
    private void startTimer() {
        startTime = System.nanoTime();
    }

    // Stop the execution timer and print the execution time
    private void stopTimer() {
        long endTime = System.nanoTime();
        long executionTime = (endTime - startTime) / 1000000;
        System.out.println("Execution Time: " + executionTime + " ms");
    }

    // Get the content of a register
    private int getRegister(int registerIndex) {
        if (registerIndex < 0 || registerIndex >= registers.length)
            throw new IllegalArgumentException("Invalid Register Index");
        return registers[registerIndex];
    }

    // Get the content from a memory address
    private int getMemory(int address) {
        validateMemoryAddress(address);
        return dataMemory[address];
    }

    // Set the content at a memory address
    private void setMemory(int address, int value) {
        validateMemoryAddress(address);
        dataMemory[address] = value;
    }

    // Read a byte from memory
    private int memoryReadByte(int address) {
        validateMemoryAddress(address);
        return Byte.toUnsignedInt((byte) getMemory(address));
    }

    // Read a half-word from memory
    private int memoryReadHalfWord(int address) {
        validateMemoryAddress(address);
        return Short.toUnsignedInt((short) getMemory(address));
    }

    // Read a word from memory
    private int memoryReadWord(int address) {
        validateMemoryAddress(address);
        return getMemory(address);
    }

    // Write a byte to memory
    private void memoryWriteByte(int address, int value) {
        validateMemoryAddress(address);
        value = value & 0xFF; // Ensure only the least significant byte is considered
        setMemory(address, value);
    }

    // Write a half-word to memory
    private void memoryWriteHalfWord(int address, int value) {
        validateMemoryAddress(address);
        value = value & 0xFFFF; // Ensure only the least significant two bytes are considered
        setMemory(address, value);
    }

    // Write a word to memory
    private void memoryWriteWord(int address, int value) {
        validateMemoryAddress(address);
        setMemory(address, value);
    }

    // Validate that the memory address is within the valid range
    private void validateMemoryAddress(int address) {
        if (address < 0 || address >= DATA_MEMORY_SIZE) {
            throw new IllegalArgumentException("Invalid Memory Address: " + address);
        }
    }
    

    // Sign extend a 16-bit value to 32 bits
    private int signExtend(int value, int bits) {
        int signBit = value & (1 << (bits - 1));
        int mask = (1 << bits) - 1;
        return (signBit != 0) ? (value | ~mask) : (value & mask);
    }
    

    // Set a breakpoint at a specific PC value
    private void setBreakpoint(int pcValue) {
        if (!breakpoints.contains(pcValue) && breakpoints.size() < 5) {
            breakpoints.add(pcValue);
        } else {
            System.out.println("Unable to set breakpoint at PC = " + pcValue);
        }
    }

    // Handle user input not covered by predefined commands
    private void handleUserInput(String input) {
        System.out.println("Unhandled command: " + input);
    }
}