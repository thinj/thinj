package thinj.instructions;

public interface InstructionHandler {
	/**
	 * This method shall handle the instruction
	 * 
	 * @param address The address in the code where the instruction 'ins' was found
	 * @param instruction The instruction at address 'addr'
	 */
	void handle(int address, AbstractInstruction instruction);
}
