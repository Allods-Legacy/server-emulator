package eu.allodslegacy.io.serialization;

import org.jetbrains.annotations.NotNull;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class SerializationDataInput implements DataInput {

    private final static int DATA_MAX_SIZE = 2000;

    private final DataInputStream in;

    public SerializationDataInput(InputStream inputStream) {
        this.in = new DataInputStream(inputStream);
    }

    @Override
    public void readFully(@NotNull byte[] b) throws IOException {
        in.readFully(b);
    }

    @Override
    public void readFully(@NotNull byte[] b, int off, int len) throws IOException {
        in.readFully(b, off, len);
    }

    @Override
    public int skipBytes(int n) throws IOException {
        return in.skipBytes(n);
    }

    @Override
    public boolean readBoolean() throws IOException {
        return in.readBoolean();
    }

    @Override
    public byte readByte() throws IOException {
        return in.readByte();
    }

    @Override
    public int readUnsignedByte() throws IOException {
        return in.readUnsignedByte();
    }

    @Override
    public short readShort() throws IOException {
        int firstByte = this.readByte();
        if ((firstByte & 128) == 0) {
            return (short) firstByte;
        } else if ((firstByte & 64) == 0) {
            int secondByte = this.readByte();
            return (short) ((firstByte & 127) << 8 | secondByte);
        } else {
            return in.readShort();
        }
    }

    @Override
    public int readUnsignedShort() throws IOException {
        return this.readShort();
    }

    @Override
    public char readChar() throws IOException {
        return in.readChar();
    }

    @Override
    public int readInt() throws IOException {
        int firstByte = this.readByte();
        if ((firstByte & 128) == 0) {
            return firstByte;
        } else {
            int mediumBits;
            if ((firstByte & 64) == 0) {
                mediumBits = this.readByte() & 255;
                return (firstByte & 127) << 8 | mediumBits;
            } else if ((firstByte & 32) == 0) {
                mediumBits = in.readShort();
                return mediumBits << 5 | firstByte & 31;
            } else if ((firstByte & 16) == 0) {
                mediumBits = in.readShort();
                int highBits = this.readByte();
                return highBits << 20 | mediumBits << 4 | firstByte & 15;
            } else {
                return in.readInt();
            }
        }
    }

    @Override
    public long readLong() throws IOException {
        return in.readLong();
    }

    @Override
    public float readFloat() throws IOException {
        return in.readFloat();
    }

    @Override
    public double readDouble() throws IOException {
        return in.readDouble();
    }

    @Override
    public String readLine() {
        return "";
    }

    @NotNull
    @Override
    public String readUTF() throws IOException {
        int length = this.readShort();
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < length; i++) {
            short current = this.readShort();
            result.append((char) current);
        }
        return result.toString();
    }

    public byte[] readByteArray() throws IOException {
        int dataSize = this.readInt();
        if (dataSize > 0 && dataSize < DATA_MAX_SIZE) {
            byte[] data = new byte[dataSize];
            for (int i = 0; i < dataSize; i++) {
                data[i] = in.readByte();
            }
            return data;
        } else {
            throw new IOException();
        }
    }
}
