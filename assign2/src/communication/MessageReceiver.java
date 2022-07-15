package communication;

import communication.election.CoordinatorElection;
import communication.message.parsing.MessageParser;
import store.Store;
import utils.DatagramUtils;

import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.*;

public class MessageReceiver implements Runnable{
    private final Store store;
    private final ThreadPoolExecutor service;
    private final ScheduledExecutorService scheduler;
    private ScheduledFuture<?> membershipTimeout;
    private ScheduledFuture<?> electionTimeout;
    private ScheduledFuture<?> coordinatorTimeout;
    private ScheduledFuture<?> membershipSender;

    public MessageReceiver(Store store) {
        this.store = store;
        this.service =  (ThreadPoolExecutor) Executors.newCachedThreadPool();
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    public ThreadPoolExecutor getService() {
        return service;
    }

    public ScheduledExecutorService getScheduler() {
        return scheduler;
    }

    public void setMembershipTimeoutService(Runnable command) {
        this.membershipTimeout = scheduler.schedule(command, 2, TimeUnit.SECONDS);
    }

    public void setElectionTimeoutService(Runnable command) {
        this.electionTimeout = scheduler.schedule(command, 2, TimeUnit.SECONDS);
    }

    public void setCoordinatorTimeout(Runnable command) {
        this.coordinatorTimeout = scheduler.schedule(command, 2, TimeUnit.SECONDS);
    }

    public void setMembershipSender(Runnable command) {
        this.membershipSender = scheduler.scheduleAtFixedRate(command, 0, 1, TimeUnit.SECONDS);
    }

    public void cancelElectionTimeoutService() {
        Runnable canceller = () -> electionTimeout.cancel(false);
        var cancelTask = scheduler.submit(canceller);
        try {
            cancelTask.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void cancelCoordinatorTimeoutService() {
        Runnable canceller = () -> coordinatorTimeout.cancel(true);
        var cancelTask = scheduler.submit(canceller);
        try {
            cancelTask.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void cancelMembershipSenderService() {
        Runnable canceller = () -> membershipSender.cancel(false);
        var cancelTask = scheduler.submit(canceller);
        try {
            cancelTask.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    public void cancelMembershipTimeoutService() {
        Runnable canceller = () -> membershipTimeout.cancel(true);
        scheduler.execute(canceller);
    }

    public synchronized void refreshMembershipTimeoutService() {
        if(membershipTimeout == null) return;
        Runnable canceller = () -> membershipTimeout.cancel(true);
        var cancelTask = scheduler.submit(canceller);
        try {
            cancelTask.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        setMembershipTimeoutService(CoordinatorElection.startElection(store));
    }

    public synchronized void refreshElectionTimeoutService() {
        cancelElectionTimeoutService();
        setElectionTimeoutService(CoordinatorElection.startCoordinatorSend(store));
    }

    public synchronized void refreshCoordinatorTimeout() {
        cancelCoordinatorTimeoutService();
        setCoordinatorTimeout(CoordinatorElection.startMembershipSend(store));
    }

    public boolean isElectionOver() {
        return electionTimeout.getDelay(TimeUnit.MILLISECONDS) <= 0;
    }

    public ScheduledFuture<?> getMembershipTimeout() {
        return membershipTimeout;
    }

    public ScheduledFuture<?> getElectionTimeout() {
        return electionTimeout;
    }


    @Override
    public void run() {
        setMembershipTimeoutService(CoordinatorElection.startElection(store));

        while (true) {
            System.out.println("\nWaiting for a new multicast packet:");
            String message = DatagramUtils.receiveMessage(store.getDatagramSocket());
            if (message == null) continue;

            System.out.println("Received multicast message: " + message);
            parseMessage(message, null);
        }
    }

    public void parseMessage(String message, AsynchronousSocketChannel channel) {
        var parser = MessageParser.createParser(message, store, channel);
        service.execute(parser);
    }
}
