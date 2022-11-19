package uk.ac.starlink.fits;

import java.io.EOFException;
import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

/**
 * Random-access BasicInput implementation based on a ByteBuffer.
 *
 * @author   Mark Taylor
 * @since    1 Dec 2014
 */
public class ByteBufferInput implements BasicInput {

    private final ByteBuffer niobuf_;

    /**
     * Constructor.
     *
     * @param   niobuf  byte buffer
     */
    public ByteBufferInput( ByteBuffer niobuf ) {
        niobuf_ = niobuf;
    }

    public boolean isRandom() {
        return true;
    }

    public void seek( long pos ) throws EOFException {
        int ipos = (int) pos;
        if ( ipos == pos ) {
            try {
                niobuf_.position( ipos );
            }
            catch ( IllegalArgumentException e ) {
                if ( ipos > niobuf_.limit() ) {
                    throw (EOFException) new EOFException().initCause( e );
                }
                else {
                    throw e;
                }
            }
        }
        else {
            throw new EOFException( "Out of bounds: " + pos );
        }
    }

    public long getOffset() {
        return niobuf_.position();
    }

    public void skip( long nbyte ) throws IOException {
        seek( getOffset() + nbyte );
    }

    public byte readByte() throws EOFException {
        try {
            return niobuf_.get();
        }
        catch ( BufferUnderflowException e ) {
            throw (EOFException) new EOFException().initCause( e );
        }
    }

    public short readShort() throws EOFException {
        try {
            return niobuf_.getShort();
        }
        catch ( BufferUnderflowException e ) {
            throw (EOFException) new EOFException().initCause( e );
        }
    }

    public int readInt() throws EOFException {
        try {
            return niobuf_.getInt();
        }
        catch ( BufferUnderflowException e ) {
            throw (EOFException) new EOFException().initCause( e );
        }
    }

    public long readLong() throws EOFException {
        try {
            return niobuf_.getLong();
        }
        catch ( BufferUnderflowException e ) {
            throw (EOFException) new EOFException().initCause( e );
        }
    }

    public float readFloat() throws EOFException {
        try {
            return niobuf_.getFloat();
        }
        catch ( BufferUnderflowException e ) {
            throw (EOFException) new EOFException().initCause( e );
        }
    }
 
    public double readDouble() throws EOFException {
        try {
            return niobuf_.getDouble();
        }
        catch ( BufferUnderflowException e ) {
            throw (EOFException) new EOFException().initCause( e );
        }
    }

    public void readBytes( byte[] bbuf ) throws EOFException {
        try {
            niobuf_.get( bbuf );
        }
        catch ( BufferUnderflowException e ) {
            throw (EOFException) new EOFException().initCause( e );
        }
    }

    public void close() {
    }
}
