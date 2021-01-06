package eu.allodslegacy.io.net;

import akka.stream.Attributes;
import akka.stream.FlowShape;
import akka.stream.Inlet;
import akka.stream.Outlet;
import akka.stream.javadsl.Source;
import akka.stream.stage.*;
import akka.util.ByteString;
import eu.allodslegacy.account.msg.PositionInQueueMsg;
import eu.allodslegacy.io.serialization.CppSerializer;
import eu.allodslegacy.io.serialization.SerializationException;

import java.time.Duration;
import java.util.LinkedList;
import java.util.Queue;

public class ClientQueue<T extends Client> extends GraphStage<FlowShape<T, T>> {

    private final Inlet<T> in = Inlet.create("ClientQueue.in");
    private final Outlet<T> out = Outlet.create("ClientQueue.out");
    private final FlowShape<T, T> shape = FlowShape.of(in, out);

    private final Queue<T> queue = new LinkedList<>();

    @Override
    public GraphStageLogic createLogic(Attributes inheritedAttributes) {
        return new TimerGraphStageLogicWithLogging(shape) {
            {
                setHandler(in, new AbstractInHandler() {
                    @Override
                    public void onPush() throws Exception {
                        T newClient = grab(in);
                        if (isAvailable(out) && queue.isEmpty()) {
                            sendPos(newClient, 0);
                            push(out, newClient);
                        } else {
                            queue.add(newClient);
                            sendPos(newClient, queue.size());
                        }
                        pull(in);
                    }
                });

                setHandler(out, new AbstractOutHandler() {
                    @Override
                    public void onPull() throws Exception {
                        T next = queue.poll();
                        if (next != null) {
                            sendPos(next, 0);
                            push(out, next);
                        }
                    }
                });
            }

            @Override
            public void preStart() {
                pull(in);
                scheduleAtFixedRate("timer", Duration.ZERO, Duration.ofSeconds(10));
            }

            @Override
            public void onTimer(Object key) throws SerializationException {
                int i = 1;
                for (T client : queue) {
                    sendPos(client, i++);
                }
            }
        };
    }

    @Override
    public FlowShape<T, T> shape() {
        return shape;
    }

    private void sendPos(T client, int pos) throws SerializationException {
        client.attachSource(Source.single(ByteString.fromArray(CppSerializer.serializeWithId(new PositionInQueueMsg(pos)))));
    }
}
