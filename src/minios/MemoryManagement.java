package minios;

public interface MemoryManagement {
    boolean allocateMemory(Process process);
    boolean deallocateMemory(int startAddress);
}
