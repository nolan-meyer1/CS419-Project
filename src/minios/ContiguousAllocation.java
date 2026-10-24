package minios;

public class ContiguousAllocation implements MemoryManagement {
    protected MemoryBlock head;
    private String coalescingBefore = null;
    private String coalescingAfter = null;

    public ContiguousAllocation(int totalMemory) {
        this.head = new MemoryBlock(0, totalMemory, true);
    }

    @Override
    public boolean allocate(Process process) {

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

    @Override
    public boolean release(Process process) {
        int startAddress = process.address;
        MemoryBlock current = head;

        while (current != null) {
            if (current.getStartAddress() == startAddress && !current.isFree()) {

                // Mark memory as free
                current.setFree(true);

                // Free blocks before coalescing
                String beforeCoalescing = getFreeBlocks();


                // Merge all adjacent free blocks
                mergeFreeBlocks();

                // Free blocks after coalescing
                String afterCoalescing = getFreeBlocks();

                if (!beforeCoalescing.equals(afterCoalescing)) {
                    System.out.println("Before coalescing: " + beforeCoalescing);
                    System.out.println("After coalescing: " + afterCoalescing);
                } else {
                    coalescingBefore = null;
                    coalescingAfter = null;
                }

                process.address = -1;

                return true;
            }

            current = current.getNext();
        }

        return false;
    }

    @Override
    public int translateAddress(Process process, int virtualAddress) {
        return 0;
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

    public String getFreeBlocks() {

        StringBuilder result = new StringBuilder();
        MemoryBlock current = head;

        while (current != null) {

            if (current.isFree()) {

                result.append("[Start: ")
                        .append(current.getStartAddress())
                        .append(", Size: ")
                        .append(current.getSize())
                        .append("] ");
            }

            current = current.getNext();
        }

        if (result.length() == 0) {
            return "No free blocks";
        }

        return result.toString();
    }
    public String getCoalescingBefore() {
        return coalescingBefore;
    }

    public String getCoalescingAfter() {
        return coalescingAfter;
    }
}