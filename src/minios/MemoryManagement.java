package minios;

public interface MemoryManagement {

    boolean allocate(Process process);

    boolean release(Process process);

    int translate(Process process, int virtualAddress);
}
