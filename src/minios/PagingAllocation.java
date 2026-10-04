package minios;

import minios.MemoryManagement;
import minios.Process;

public class PagingAllocation implements MemoryManagement {
    @Override
    public boolean allocateMemory(Process process) {
        return false;
    }

    @Override
    public boolean deallocateMemory(int startAddress) {
        return false;
    }
}
