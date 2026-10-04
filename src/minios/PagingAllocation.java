package minios;

public class PagingAllocation implements MemoryManagement {
    public int getPageSize() {
        return pageSize;
    }

    public int getTotalMemory() {
        return totalMemory;
    }

    public int getTotalFrames() {
        return totalFrames;
    }

    private final int pageSize;
    private final int totalMemory;
    private final int totalFrames;

    PagingAllocation(int pageSize, int totalMemory) {
        this.pageSize = pageSize;
        this.totalMemory = totalMemory;
        this.totalFrames = totalMemory / pageSize;
    }


    @Override
    public boolean allocateMemory(Process process) {
        return false;
    }

    @Override
    public boolean deallocateMemory(int startAddress) {
        return false;
    }
}
