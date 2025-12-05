import java.util.*;

public class Main {

    static class Process {

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

    } // end of class

    static class GanttEntry {

        int pid;        // which process ran
        int start;      // start time of slice
        int end;        // end time of slice

        public GanttEntry(int pid, int start, int end) {
            this.pid = pid;
            this.start = start;
            this.end = end;
        }

    } // end of class

    // main
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // read quantum
        System.out.print("Enter time quantum: ");
        int quantum = scanner.nextInt();

        // read process
        System.out.println("Enter processes (pid arrivalTime burstTime priority");
        System.out.println("Enter 0 0 0 0 to stop");
        ArrayList<Process> processesList = new ArrayList<>();

        // processes input loop
        while (true) {

            int pid = scanner.nextInt();
            int arrivalTime = scanner.nextInt();
            int burstTime = scanner.nextInt();
            int priority = scanner.nextInt();

            if (pid == 0 && arrivalTime == 0 && burstTime == 0 && priority == 0)
                break;

            processesList.add(new Process(pid, arrivalTime, burstTime, priority));

        } // end of while

        if (processesList.isEmpty()){
            System.out.println("No processes entered. exiting");
            return;
        }

        // perform scheduling and return gantt entries list as gantt
        ArrayList<GanttEntry> gantt = schedule(processesList, quantum);

        // print gantt chart, print stats
        printGanttChart(gantt);
        printStats(processesList);

    } // end of main

    public static ArrayList<GanttEntry> schedule(ArrayList<Process> processesList, int quantum){

        // sort list of processes by arrivalTime
        processesList.sort(Comparator.comparingInt(process -> process.arrivalTime));

        // queue of queues of processes sorted by priority
        TreeMap<Integer, Queue<Process>> readyQueues = new TreeMap<>();

        // to store processes as gant entries
        ArrayList<GanttEntry> ganttEntries = new ArrayList<>();

        int currentTime = processesList.getFirst().arrivalTime;     // start time of simulation
        int nextIndex = 0;                                          // next process to move into ready
        int finishedCount = 0;                                      // number of completed processes
        int totalProcesses = processesList.size();                  // number of processes

        // main scheduling loop
        while (finishedCount < totalProcesses){

            // 1 - move all processes that have arrived into readyQueues
            while (nextIndex < totalProcesses && processesList.get(nextIndex).arrivalTime <= currentTime){

                Process arrivingProcess = processesList.get(nextIndex);
                nextIndex++;

                arrivingProcess.remainingBurstTime = arrivingProcess.burstTime;

                // get or create queue for arrived process' priority
                Queue<Process> arrivalPriorityQueue = readyQueues.get(arrivingProcess.priority);
                if (arrivalPriorityQueue == null){
                    arrivalPriorityQueue = new LinkedList<>();
                    readyQueues.put(arrivingProcess.priority, arrivalPriorityQueue);
                }
                arrivalPriorityQueue.add(arrivingProcess);

            } // end of inner loop

            // 2 - if no ready processes, jump time to next arrival time
            if (readyQueues.isEmpty()){
                if (nextIndex < totalProcesses){
                    currentTime = processesList.get(nextIndex).arrivalTime;
                    continue;
                } else{
                    break;
                }
            }

            // 3 - pick highest priority queue from readyQueues
            Map.Entry<Integer, Queue<Process>> highestPriorityEntry = readyQueues.firstEntry();
            Queue<Process> highestPriorityQueue = highestPriorityEntry.getValue();

            // take next process from that queue
            Process currentProcess = highestPriorityQueue.poll();

            // if that queue is empty, remove it from the map
            // we check after poll to know if queue is empty after poll
            if (highestPriorityQueue.isEmpty()) {
                readyQueues.remove(highestPriorityEntry.getKey());
            }

            // if this is the first time the process gets CPU, set startTime
            if (currentProcess.startTime == -1) 
                currentProcess.startTime = currentTime;

            // 4 - run the process for one quantum or until it finishes
            int slice = Math.min(quantum, currentProcess.remainingBurstTime);
            int start = currentTime;
            int end = currentTime + slice;

            // record in Gantt chart
            ganttEntries.add(new GanttEntry(currentProcess.pid, start, end));

            // increment time, decrement remaining burst time
            currentTime = end;
            currentProcess.remainingBurstTime -= slice;

            // 5 - add any new arrivals that showed during this time slice
             while (nextIndex < totalProcesses && processesList.get(nextIndex).arrivalTime <= currentTime){
                 Process arrivingProcess = processesList.get(nextIndex);
                 nextIndex++;

                 arrivingProcess.remainingBurstTime = arrivingProcess.burstTime;

                 Queue<Process> arrivalPriorityQueue = readyQueues.get(arrivingProcess.priority);
                 if (arrivalPriorityQueue == null){
                     arrivalPriorityQueue = new LinkedList<>();
                     readyQueues.put(arrivingProcess.priority, arrivalPriorityQueue);
                 }
                 arrivalPriorityQueue.add(arrivingProcess);

            } // end of inner loop

            // 6 - if process still has time left, move to back of same priority queue
            if (currentProcess.remainingBurstTime > 0) {

                Queue<Process> samePriorityQueue = readyQueues.get(currentProcess.priority);
                if (samePriorityQueue == null) {
                    samePriorityQueue = new LinkedList<>();
                    readyQueues.put(currentProcess.priority, samePriorityQueue);
                }
                samePriorityQueue.add(currentProcess);

            } else{
                currentProcess.finishTime = currentTime;
                finishedCount++;
            }

        } // end of main loop

        return ganttEntries;

    } // end of schedule

    public static void printGanttChart(ArrayList<GanttEntry> gantt) {

        System.out.println("\nGantt chart: ");

        if (gantt.isEmpty()) {
            System.out.println("No CPU activity");
            return;
        }

        for (GanttEntry g : gantt){
            System.out.print("[" + g.start + " - P" + g.pid + " - " + g.end + "] ");
        }

    } // end of printGanttChart


    public static void printStats(ArrayList<Process> processesList) {

        System.out.println("\nProcess Statistics: ");

        double totalTAT = 0;
        double totalWT  = 0;
        double totalRT  = 0;

        // Compute stats for each process
        for (Process p : processesList) {
            p.turnAroundTime = p.finishTime - p.arrivalTime;
            p.waitingTime    = p.turnAroundTime - p.burstTime;
            p.responseTime   = p.startTime - p.arrivalTime;

            totalTAT += p.turnAroundTime;
            totalWT  += p.waitingTime;
            totalRT  += p.responseTime;
        }

        // Print table header ONCE
        System.out.printf("%-5s %-12s %-10s %-10s%n",
                "PID", "Turnaround", "Waiting", "Response");

        // Print each process row
        for (Process p : processesList) {
            System.out.printf("%-5s %-12d %-10d %-10d%n",
                    "P" + p.pid, p.turnAroundTime, p.waitingTime, p.responseTime);
        }

        // Print averages
        int n = processesList.size();
        System.out.println("Average Turnaround Time = " + (totalTAT / n));
        System.out.println("Average Waiting Time    = " + (totalWT / n));
        System.out.println("Average Response Time   = " + (totalRT / n));

    } // end of printStats

} // end of class