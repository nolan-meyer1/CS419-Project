package minios;

import java.util.List;

public class SJF implements SchedulingAlgo{
    @Override
    public void addProcess(List<Process> readyQueue, Process p) {
        readyQueue.add(p);
    }

    @Override
    public Process selectNextProcess(List<Process> readyQueue) {
        int minBurstLength = Integer.MAX_VALUE;
        Process minProcess = null;

        for (Process p : readyQueue){
            int burstLength = calculateBurstLength(p);
            if (burstLength < minBurstLength){
                minBurstLength = burstLength;
                minProcess = p;
            }
        }

        return minProcess;
    }

    private int calculateBurstLength(Process p){
        int burstLength = 0;
        for (Instruction inst : p.code){
            if(inst.type == Instruction.OpType.CPU){
                burstLength += inst.duration;
            }
        }
        return burstLength;
    }
}
