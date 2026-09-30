package minios.memory;

public class MemoryBlock {
    private int startAddress;
    private int size;
    private boolean isFree;
    private MemoryBlock next;

    public MemoryBlock(int startAddress, int size, boolean isFree) {
        this.startAddress = startAddress;
        this.size = size;
        this.isFree = isFree;
        this.next = null;
    }

    public int getStartAddress() {
        return startAddress;
    }

    public int getSize() {
        return size;
    }

    public boolean isFree() {
        return isFree;
    }

    public void setFree(boolean free) {
        isFree = free;
    }

    public void setNext(MemoryBlock next) {
        this.next = next;
    }

    public MemoryBlock getNext() {
        return next;
    }

    public void setSize(int size) {
        this.size = size;
    }
}