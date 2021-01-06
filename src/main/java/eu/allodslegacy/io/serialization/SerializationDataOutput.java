package eu.allodslegacy.io.serialization;

import org.jetbrains.annotations.NotNull;

import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class SerializationDataOutput implements DataOutput {

    private final DataOutputStream out;

    public SerializationDataOutput(OutputStream outputStream) {
        this.out = new DataOutputStream(outputStream);
    }

    @Override
    public void write(int b) throws IOException {
        out.write(b);
    }

    @Override
    public void write(@NotNull byte[] b) throws IOException {
        this.writeInt(b.length);
        out.write(b, 0, b.length);
    }

    @Override
    public void write(@NotNull byte[] b, int off, int len) throws IOException {
        this.writeInt(len);
        out.write(b, off, len);
    }

    @Override
    public void writeBoolean(boolean v) throws IOException {
        out.writeBoolean(v);
    }

    @Override
    public void writeByte(int v) throws IOException {
        out.writeByte(v);
    }

    @Override
    public void writeShort(int v) throws IOException {
        if (v < 0 || v > 16383) {
            out.writeByte(192);
            out.writeShort(v);
        } else if (v <= 127) {
            out.writeByte(v);
        } else {
            out.writeByte(0x8000 | v);
        }
    }

    @Override
    public void writeChar(int v) throws IOException {
        out.writeChar(v);
    }

    @Override
    public void writeInt(int v) throws IOException {
        if (v < 0 || v > 0xFFFFFFF) {
            this.writeByte(0xF0);
            out.writeInt(v);
        } else if (v <= 0x7F) {
            this.writeByte(v);
        } else if (v <= 0x3FFF) {
            out.writeShort(0x8000 | v);
        } else if (v <= 0x1FFFFF) {
            this.writeByte(192 | 31 & v);
            out.writeShort(v >>> 5);
        } else {
            this.writeByte(224 | 15 & v);
            out.writeShort(v >>> 4 & 0xFFFF);
            this.writeByte(v >>> 20);
        }
    }

    @Override
    public void writeLong(long v) throws IOException {
        out.writeLong(v);
    }

    @Override
    public void writeFloat(float v) throws IOException {
        out.writeFloat(v);
    }

    @Override
    public void writeDouble(double v) throws IOException {
        out.writeDouble(v);
    }

    @Override
    public void writeBytes(@NotNull String s) throws IOException {
        out.writeBytes(s);
    }

    @Override
    public void writeChars(@NotNull String s) throws IOException {
        for (int i = 0; i < s.length(); i++) {
            this.writeShort(s.charAt(i));
        }
    }

    @Override
    public void writeUTF(@NotNull String s) throws IOException {
        int length = s.length();
        this.writeShort(length);
        this.writeChars(s);
    }
}
