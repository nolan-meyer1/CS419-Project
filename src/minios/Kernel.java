package minios;

import java.util.*;

public class Kernel {
    private final SchedulingAlgo algo;
    private final List<Process> readyQueue = new ArrayList<>();
    private final List<Process> waitQueue = new ArrayList<>();
    private final MemoryManagement memoryManagement;
    protected final Queue<Process> memoryWaitQueue = new LinkedList<>();
    private final List<Process> allProcesses = new ArrayList<>();
    private Process runningProcess = null;
    protected int timeQuantum; // Time quantum for RR
    private int quantumRemaining; // Remaining time for the current process
    private int[] physicalMemory; // Represents the physical memory

    public Kernel(SchedulingAlgo algo, MemoryManagement memoryManagement, int totalMemory) {
        this.algo = algo;
        this.memoryManagement = memoryManagement;

        if(algo instanceof RR){
            this.timeQuantum = 2;
            this.quantumRemaining = timeQuantum;
        }else{
            this.timeQuantum = Integer.MAX_VALUE; // Effectively no quantum for non-RR algorithms
            this.quantumRemaining = timeQuantum;
        }

        physicalMemory = new int[totalMemory]; // Initialize physical memory
    }

    public void admitProcess(Process p) {
        admitProcess(p, 0);
    }
    public void admitProcess(Process p, int currentTime) {

        // tracks every process that arrives
        allProcesses.add(p);

        if (memoryManagement.allocate(p)) {

            p.state = Process.State.READY;
            algo.addProcess(readyQueue, p);

            System.out.println("[Tick " + currentTime
                    + "] Process " + p.pid
                    + " admitted into memory.");

            if (memoryManagement instanceof ContiguousAllocation) {

                System.out.println("[Tick " + currentTime
                        + "] Process " + p.pid
                        + " allocated at address " + p.address
                        + " with size " + p.memorySize);
            }else if (memoryManagement instanceof PagingAllocation) {

                System.out.println("[Tick " + currentTime
                        + "] Process " + p.pid
                        + " page table: "
                        + Arrays.toString(p.pageTable));
            }else if (memoryManagement instanceof PagingAllocation) {

                PagingAllocation paging =
                        (PagingAllocation) memoryManagement;

                System.out.println("[Tick " + currentTime
                        + "] Process " + p.pid
                        + " cannot be admitted. Free frames: "
                        + paging.freeFrames());
            }

        } else {

            // Process remains in a NEW state while waiting for memory
            p.state = Process.State.NEW;
            memoryWaitQueue.add(p);

            System.out.println("[Tick " + currentTime
                    + "] Process " + p.pid
                    + " placed in memory waiting queue.");

            if (memoryManagement instanceof ContiguousAllocation) {

                ContiguousAllocation contiguous =
                        (ContiguousAllocation) memoryManagement;

                System.out.println("[Tick " + currentTime
                        + "] Available free blocks: "
                        + contiguous.getFreeBlocks());
            }
        }
    }

    // Called on every clock tick
    public void onClockTick(int currentTime) {
        // check all processes currently waiting in I/O wait queue
        serviceWaitQueue(currentTime);

        boolean cpuCycleUsed = false;
        // Execute one CPU cycle
        // (One clock tick is one CPU cycle)
        while (!cpuCycleUsed) {
            // No process currently running
            if (runningProcess == null) {
                if (dispatchNextProcess(currentTime) == null){
                    // No more process to schedule; simulation finishes.
                    break;
                }
            }

            Instruction inst = runningProcess.getCurrentInstruction();

            // Current process has finished.
            if (inst == null) {
                System.out.println("[Tick " + currentTime + "] Process " + runningProcess.pid + " Terminates.");
                terminateProcess(runningProcess, currentTime);
                // In this case, no instruction is executed, so no CPU cycle
                // is used. Can't advance the simulation clock. Loop
                // back and grab the next process to execute.
                continue;
            }

            // I/O instruction: process enters I/O and no longer uses CPU
            // (Assume this transition itself doesn't consume CPU cycles.)
            else if (inst.type == Instruction.OpType.IO) {
                System.out.println("[Tick " + currentTime + "] Process " + runningProcess.pid + " enters I/O wait.");
                runningProcess.state = Process.State.BLOCKED;
                waitQueue.add(runningProcess);
                runningProcess = null;
                // Can't advance the simulation clock. Loop back
                // and grab the next process to execute.
                continue;
            }

            // LOAD or STORE instruction
            else if (inst.type == Instruction.OpType.STORE) {

                for (Process p : readyQueue) {
                    p.waitTime++;
                }

                try {
                    int physicalAddress =
                            memoryManagement.translateAddress(
                                    runningProcess,
                                    inst.address
                            );

                    System.out.println("[Tick " + currentTime
                            + "] Process " + runningProcess.pid
                            + " translated virtual address "
                            + inst.address
                            + " to physical address "
                            + physicalAddress);

                    physicalMemory[physicalAddress] = inst.value;

                    System.out.println("[Tick " + currentTime
                            + "] Process " + runningProcess.pid
                            + " stores value "
                            + inst.value
                            + " at physical address "
                            + physicalAddress);

                    inst.remainingTicks--;

                    if (algo instanceof RR) {
                        quantumRemaining--;
                    }

                    cpuCycleUsed = true;
                    runningProcess.programCounter++;

                    if (algo instanceof RR && quantumRemaining == 0) {
                        preemptProcess(runningProcess);
                    }

                } catch (IllegalArgumentException e) {

                    inst.remainingTicks--;

                    if (algo instanceof RR) {
                        quantumRemaining--;
                    }

                    cpuCycleUsed = true;

                    System.out.println("[Tick " + currentTime
                            + "] WARNING: Process "
                            + runningProcess.pid
                            + " attempted invalid instruction STORE "
                            + inst.address + " " + inst.value);

                    terminateProcess(runningProcess, currentTime);
                }
            }

            else if (inst.type == Instruction.OpType.LOAD) {

                for (Process p : readyQueue) {
                    p.waitTime++;
                }

                try {
                    int physicalAddress =
                            memoryManagement.translateAddress(
                                    runningProcess,
                                    inst.address
                            );

                    System.out.println("[Tick " + currentTime
                            + "] Process " + runningProcess.pid
                            + " translated virtual address "
                            + inst.address
                            + " to physical address "
                            + physicalAddress);

                    int value = physicalMemory[physicalAddress];

                    System.out.println("[Tick " + currentTime
                            + "] Process " + runningProcess.pid
                            + " loads value "
                            + value
                            + " from physical address "
                            + physicalAddress);

                    inst.remainingTicks--;

                    if (algo instanceof RR) {
                        quantumRemaining--;
                    }

                    cpuCycleUsed = true;
                    runningProcess.programCounter++;

                    if (algo instanceof RR && quantumRemaining == 0) {
                        preemptProcess(runningProcess);
                    }

                } catch (IllegalArgumentException e) {

                    inst.remainingTicks--;

                    if (algo instanceof RR) {
                        quantumRemaining--;
                    }

                    cpuCycleUsed = true;

                    System.out.println("[Tick " + currentTime
                            + "] WARNING: Process "
                            + runningProcess.pid
                            + " attempted invalid instruction LOAD "
                            + inst.address);

                    terminateProcess(runningProcess, currentTime);
                }
            }
            // CPU instruction: the instruction has not finished;
            // Utilize one CPU cycle
            else if(inst.remainingTicks > 0){

                for (Process p : readyQueue) {
                    p.waitTime++;
                }

                inst.remainingTicks--;

                if(algo instanceof RR) {
                    quantumRemaining--;
                }

                cpuCycleUsed = true;
                if (inst.remainingTicks == 0) {
                    // The instruction now finishes; load the next
                    // instruction.
                    runningProcess.programCounter++;
                }

                if(algo instanceof RR && quantumRemaining == 0){
                    //Process has used up its time quantum; preempt it
                    preemptProcess(runningProcess);
                }
            }

            // CPU instruction: the instruction has finished
            // (should never come here as this is already handled above,
            // but just in case...)
            else{
                runningProcess.programCounter++;
                // Can't advance the simulation clock. Loop back
                // and move to the next instruction.
                continue;
            }
        }

        // Updates memory waiting time for processes
        // that are waiting for memory allocation
        for (Process p : memoryWaitQueue) {
            p.memoryWaitTime++;
        }
    }

    private void serviceWaitQueue(int currentTime) {
        Iterator<Process> it = waitQueue.iterator();
        while (it.hasNext()) {
            Process p = it.next();
            Instruction inst = p.getCurrentInstruction();
            if (inst != null) {
                inst.remainingTicks--;
                if (inst.remainingTicks <= 0) {
                    // The I/O wait has completed;
                    // load the next instruction.
                    System.out.println("[Tick " + currentTime + "] Process " + p.pid + " I/O completes.");
                    p.programCounter++;
                    p.state = Process.State.READY;
                    // Move the process to Ready Queue
                    algo.addProcess(readyQueue, p);
                    it.remove();
                }
            }
        }
    }

    private void terminateProcess(Process p, int currentTime) {
        p.state = Process.State.TERMINATED;
        runningProcess = null;

        boolean released = memoryManagement.release(p);

        if (released) {
            System.out.println("[Tick " + currentTime
                    + "] Process " + p.pid
                    + " memory released.");
        } if (released && memoryManagement instanceof ContiguousAllocation) {

            ContiguousAllocation contiguous =
                    (ContiguousAllocation) memoryManagement;

            if (contiguous.getCoalescingBefore() != null) {

                System.out.println("[Tick " + currentTime
                        + "] Before coalescing: "
                        + contiguous.getCoalescingBefore());

                System.out.println("[Tick " + currentTime
                        + "] After coalescing: "
                        + contiguous.getCoalescingAfter());
            }
        }

        scanMemoryWaitQueue(currentTime);

    }
    private void scanMemoryWaitQueue(int currentTime){
        Iterator<Process> iterator = memoryWaitQueue.iterator();

        while (iterator.hasNext()) {
            Process waitingProcess = iterator.next();

            if (memoryManagement.allocate(waitingProcess)){
                iterator.remove();
                waitingProcess.state= Process.State.READY;
                algo.addProcess(readyQueue, waitingProcess);
                System.out.println("[Tick " + currentTime
                        + "] Process " + waitingProcess.pid
                        + " admitted from memory waiting queue."
                        + " Memory size: " + waitingProcess.memorySize);

                if (memoryManagement instanceof ContiguousAllocation) {

                    System.out.println("[Tick " + currentTime
                            + "] Process " + waitingProcess.pid
                            + " allocated at address " + waitingProcess.address
                            + " with size " + waitingProcess.memorySize);

                } else if (memoryManagement instanceof PagingAllocation) {

                    System.out.println("[Tick " + currentTime
                            + "] Process " + waitingProcess.pid
                            + " page table: "
                            + Arrays.toString(waitingProcess.pageTable));
                }
            }
        }
    }

    private Process dispatchNextProcess(int currentTime) {
        runningProcess = algo.selectNextProcess(readyQueue);

        if(algo instanceof RR){
            quantumRemaining = timeQuantum;
        }

        if (runningProcess != null) {
            System.out.println("[Tick " + currentTime + "] Process " + runningProcess.pid + " executes.");
            runningProcess.state = Process.State.RUNNING;
            return runningProcess;
        }else{
            return null;
        }

    }

    public boolean isIdle() {
        return runningProcess == null &&
                readyQueue.isEmpty() &&
                waitQueue.isEmpty() &&
                memoryWaitQueue.isEmpty();
    }

    private void preemptProcess(Process p){

        if(runningProcess != null){
            p.state = Process.State.READY;
            algo.addProcess(readyQueue,p);
            runningProcess = null;
            quantumRemaining = timeQuantum;
        }
    }

    public double calculateAverageWaitTime() {
        int totalWaitTime = 0;
        int processCount = allProcesses.size();

        for (Process p: allProcesses) {
            totalWaitTime += p.waitTime;
        }

        return processCount > 0 ? (double) totalWaitTime / processCount : 0.0;
    }

    public double calculateAverageMemoryWaitTime() {

        int totalMemoryWaitTime = 0;
        int processCount = allProcesses.size();

        for (Process p : allProcesses) {
            totalMemoryWaitTime += p.memoryWaitTime;
        }

        return processCount > 0
                ? (double) totalMemoryWaitTime / processCount : 0.0;
    }
}