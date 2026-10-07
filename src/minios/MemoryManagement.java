package minios;

public interface MemoryManagement {

    boolean allocate(Process process);

    boolean release(Process process);
}
