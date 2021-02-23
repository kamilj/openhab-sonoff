package org.openhab.binding.sonoff.internal.helpers;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import org.openhab.binding.sonoff.internal.listeners.MessageListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageQueue implements Runnable, MessageListener {

    private final Logger logger = LoggerFactory.getLogger(MessageQueue.class);
    private final ConcurrentMap<Long, CountDownLatch> latchMap = new ConcurrentHashMap<Long, CountDownLatch>();
    /** Queue of messages to send */
    private final BlockingDeque<CommandMessage> queue = new LinkedBlockingDeque<CommandMessage>();
    /** Map of Integers so we can count retry attempts. */
    private final ConcurrentMap<Long, Integer> retryCountMap = new ConcurrentHashMap<Long, Integer>();
    // Map of our messages
    private final MessageListener listener;
    private final int timeoutForOkMessagesMs = 1000;

    /** Boolean to indicate if we are running */
    private boolean running;

    public MessageQueue(MessageListener listener) {
        this.listener = listener;
    }

    public void clearQueue() {
        queue.clear();
        retryCountMap.clear();
        latchMap.clear();
    }

    public synchronized void stopRunning() {
        running = false;
    }

    public synchronized void startRunning() {
        running = true;
    }

    public void sendCommand(CommandMessage message) {
        try {
            if (running) {
                queue.put(message);
            } else {
                logger.info("Message not added to queue as we are shutting down");
            }
        } catch (InterruptedException e) {
            logger.error("Error adding command to queue:{}", e);
        }
    }

    @Override
    public void run() {
        try {
            CommandMessage message = queue.take();
            if (message.getSequence() == null) {
                message.setSequence();
            }

            CountDownLatch latch = new CountDownLatch(1);
            latchMap.putIfAbsent(message.getSequence(), latch);
            retryCountMap.putIfAbsent(message.getSequence(), Integer.valueOf(1));
            listener.sendMessage(message);
            boolean unlatched = latch.await(timeoutForOkMessagesMs, TimeUnit.MILLISECONDS);
            latchMap.remove(message.getSequence());

            if (!unlatched) {
                Integer sendCount = retryCountMap.get(message.getSequence());
                if (sendCount.intValue() >= 3) {
                    logger.warn("Unable to send transaction {}, command was {}, after {} retry attempts",
                            message.getSequence(), message.getCommand(), 3);
                    return;
                }
                if (!running) {
                    logger.error("Not retrying transactionId {} as we are stopping", message.getSequence());
                    return;

                }
                Integer newRetryCount = Integer.valueOf(sendCount.intValue() + 1);
                logger.warn(
                        "Ok message not received for transaction: {}, command was {}, retrying again. Retry count {}",
                        message.getSequence(), message.getCommand(), newRetryCount);
                retryCountMap.put(message.getSequence(), newRetryCount);
                queue.addFirst(message);
            }
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void okMessage(Long sequence) {
        CountDownLatch latch = latchMap.get(sequence);
        if (latch != null) {
            latch.countDown();
        }
    }

    @Override
    public void sendMessage(CommandMessage message) {
        // not applicable here
    }
}
