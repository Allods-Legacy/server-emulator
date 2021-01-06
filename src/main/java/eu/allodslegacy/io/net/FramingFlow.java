package eu.allodslegacy.io.net;

import akka.NotUsed;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Source;
import akka.util.ByteString;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public final class FramingFlow {

    public static Flow<ByteString, ByteString, NotUsed> in() {
        return Flow.of(ByteString.class)
                .flatMapConcat(s -> {
                    ByteBuffer buffer = s.asByteBuffer();
                    List<ByteString> source = new ArrayList<>();
                    while (buffer.hasRemaining()) {
                        int length = unpackInt(buffer);
                        if (length > 0) {
                            byte[] msg = new byte[length];
                            buffer.get(msg, 0, length);
                            source.add(ByteString.fromArray(msg));
                        }
                    }
                    return Source.from(source);
                });
    }

    public static Flow<ByteString, ByteString, NotUsed> out() {
        return Flow.of(ByteString.class).map(s -> {
            ByteBuffer sizeBuffer = ByteBuffer.allocate(5);
            packInt(sizeBuffer, s.length());
            return ByteString.fromByteBuffer(sizeBuffer.flip()).concat(s).concat(ByteString.fromInts(0));
        });
    }

    private static int unpackInt(ByteBuffer buffer) {
        int firstByte = buffer.get() & 255;
        int secondByte;
        if (firstByte >= 224) {
            secondByte = buffer.get() & 255;
            int lowShort = buffer.getShort() & 0xFFFF;
            return ((15 & firstByte) << 24) + (secondByte << 16) + lowShort;
        } else if (firstByte >= 192) {
            secondByte = buffer.getShort() & 0xFFFF;
            return ((31 & firstByte) << 16) + secondByte;
        } else if (firstByte >= 128) {
            secondByte = buffer.get() & 255;
            return ((63 & firstByte) << 8) + secondByte;
        } else {
            return firstByte;
        }
    }

    private static void packInt(ByteBuffer buffer, int val) {
        if (val >= 0 && val < 268435456) {
            if (val < 128) {
                buffer.put((byte) val);
            } else if (val < 16384) {
                buffer.putShort((short) (0x8000 | val));
            } else if (val < 2097152) {
                buffer.put((byte) (192 | val >> 16));
                buffer.putShort((short) (0xFFFF & val));
            } else {
                buffer.putInt(-536870912 | val);
            }
        } else {
            buffer.put((byte) -16);
            buffer.putInt(val);
        }
    }
}
