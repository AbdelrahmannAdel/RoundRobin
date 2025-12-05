public class Process {
    
    int pid;
    int arrivalTime;
    int burstTime;
    int priority;

    int remainingBurstTime;
    int startTime = -1;
    int finishTime = -1;

    int turnAroundTime;
    int waitingTime;
    int responseTime;

    Process(int pid, int arrivalTime, int burstTime, int priority){
        this.pid = pid;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.priority = priority;
        this.remainingBurstTime = burstTime; // when first created
    }

    @Override
    public String toString(){
        return "P" + pid;
    }

}
