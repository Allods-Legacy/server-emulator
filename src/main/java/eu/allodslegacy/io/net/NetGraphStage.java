package eu.allodslegacy.io.net;

import akka.stream.FlowShape;
import akka.stream.Inlet;
import akka.stream.Outlet;
import akka.stream.stage.GraphStage;
import akka.util.ByteString;

public abstract class NetGraphStage extends GraphStage<FlowShape<ByteString, ByteString>> {

    protected final Inlet<ByteString> in = Inlet.create("net.in");
    protected final Outlet<ByteString> out = Outlet.create("net.out");
    protected final FlowShape<ByteString, ByteString> shape = FlowShape.of(in, out);

    @Override
    public final FlowShape<ByteString, ByteString> shape() {
        return shape;
    }
}
