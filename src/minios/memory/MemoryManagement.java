package minios.memory;

import minios.Process;

import java.util.LinkedList;
import java.util.Queue;

public class MemoryManagement {
    protected MemoryBlock head;

    protected Queue<Process> waitingQueue;

    public MemoryManagement(int totalMemory) {
        this.head = new MemoryBlock(0, totalMemory, true);
        waitingQueue = new LinkedList<>();
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

        //Adds to wait queue if not able to allocate
        waitingQueue.add(process);

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

                //Tries to allocate memory for processes in the waiting queue
                if(!waitingQueue.isEmpty()) {
                    allocateMemory(waitingQueue.poll());
                }

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