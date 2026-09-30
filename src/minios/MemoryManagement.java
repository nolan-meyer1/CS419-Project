package minios;

public class MemoryManagement {
    protected MemoryBlock head;

    public MemoryManagement(int totalMemory) {
        this.head = new MemoryBlock(0, totalMemory, true);
    }

    public boolean allocateMemory(Process process) {

        MemoryBlock current = head;

        while (current != null) {
            if (current.isFree() && current.getSize() >= process.memorySize) {

                // Calculate remaining memory
                int remainingSize = current.getSize() - process.memorySize;

                // Allocate requested memory
                current.setFree(false);
                current.setSize(process.memorySize);

                //Set the process's start address
                process.address = current.getStartAddress();

                // Create a new block for remaining free memory
                if (remainingSize > 0) {
                    MemoryBlock newBlock = new MemoryBlock(current.getStartAddress() + process.memorySize, remainingSize, true);

                    // Link new block into the list
                    newBlock.setNext(current.getNext());
                    current.setNext(newBlock);
                }

                return true;
            }

            current = current.getNext();
        }

        return false;
    }

    public boolean deallocateMemory(int startAddress) {
        MemoryBlock current = head;

        while (current != null) {
            if (current.getStartAddress() == startAddress && !current.isFree()) {

                // Mark memory as free
                current.setFree(true);

                // Merge all adjacent free blocks
                mergeFreeBlocks();

                return true;
            }

            current = current.getNext();
        }

        return false;
    }

    private void mergeFreeBlocks() {
        MemoryBlock current = head;

        while (current != null && current.getNext() != null) {
            MemoryBlock next = current.getNext();

            if (current.isFree() && next.isFree()) {
                // Combine adjacent free blocks
                current.setSize(current.getSize() + next.getSize());

                // Remove the next block from the list
                current.setNext(next.getNext());
            } else {
                current = current.getNext();
            }
        }
    }
}