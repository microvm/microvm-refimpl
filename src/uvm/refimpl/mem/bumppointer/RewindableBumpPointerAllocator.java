package uvm.refimpl.mem.bumppointer;

import static uvm.platformsupport.Config.*;
import uvm.refimpl.facade.MicroVM;
import uvm.refimpl.itpr.HasObjRef;
import uvm.refimpl.mem.Allocator;
import uvm.refimpl.mem.MemUtils;
import uvm.refimpl.mem.TypeSizes;
import uvm.refimpl.mem.scanning.MemoryDataScanner;
import uvm.refimpl.mem.scanning.ObjectMarker;
import uvm.refimpl.mem.scanning.RefFieldHandler;
import uvm.type.Hybrid;
import uvm.type.Type;
import uvm.util.ErrorUtils;

/**
 * A bump-pointer allocator with "rewinding" capability. Used to implement
 * global memory and stack memory. Both global space and stack also delegate
 * scanning to this class. Data in this "space" are scanned, but are never moved
 * or garbage collected.
 * <p>
 * It allocates data one after another. It maintains a singly-linked list of
 * objects. It can "rewinded" its end to a previous location so that all data
 * allocated after a certain location are invalidated and will not be scanned.
 * <p>
 * 'top' always points to a location where there is a pointer to the IRef of the
 * last data.
 * 
 * <pre>
 *          |        |     ^ high addr     v low addr
 *          +--------+
 *          | toIRef | -----+
 * top ---> +--------+      |
 *          | (body) |      | points to iRef
 *          | (body) |      |
 *          +--------+  <---+
 *    +---- | hdr    |
 *    |     +--------+
 *    |     | varhdr |
 *    |     +--------+
 *    |     | toIRef | -----+
 *    +-->  +--------+      |
 * inferred | (body) |      | points to iRef
 *          | (body) |      |
 *          +--------+  <---+
 *    +---- | hdr    |
 *    |     +--------+
 *    |     | varhdr |
 *    |     +--------+
 *    |     | toIRef | -----+
 *    +-->  +--------+      |
 * inferred | (body) |      | points to iRef
 *          | (body) |      |
 *          +--------+  <---+
 *    +---- | hdr    |
 *    |     +--------+
 *    |     | varhdr |
 *    |     +--------+
 *    |     | toIRef | -----> NULL
 *    +-->  +--------+
 * inferred
 * 
 * </pre>
 */
public class RewindableBumpPointerAllocator implements Allocator {

    private long begin;
    private long extend;
    private long top;
    private MicroVM microVM;

    public RewindableBumpPointerAllocator(long begin, long extend,
            MicroVM microVM) {
        this.begin = begin;
        this.extend = extend;
        this.top = begin;
        this.microVM = microVM;
    }

    @Override
    public long alloc(long size, long align, long headerSize) {
        long dataStart = top + 8;
        long iRef = dataStart + headerSize;
        long dataEnd = iRef + size;
        long nextTop = TypeSizes.alignUp(dataEnd, 8);
        if (nextTop >= begin + extend) {
            ErrorUtils
                    .uvmError("Stack overflow or insufficient global memory.");
            return 0; // unreachable
        }

        MemUtils.zeroRegion(dataStart, nextTop - dataStart);
        MEMORY_SUPPORT.storeLong(nextTop, iRef);
        top = nextTop;

        return iRef;
    }

    public void rewind(long newTop) {
        top = newTop;
    }

    public void scan(final ObjectMarker marker) {
        long curTopLoc = top;

        RefFieldHandler handler = new RefFieldHandler() {
            @Override
            public boolean handle(boolean fromClient, HasObjRef fromBox,
                    long fromObj, long fromIRef, long toObj) {
                marker.markObjRef(fromObj);
                return false;
            }
        };

        while (curTopLoc != 0) {
            long iRef = MEMORY_SUPPORT.loadLong(curTopLoc);

            long hdr = MEMORY_SUPPORT.loadLong(curTopLoc
                    + TypeSizes.GC_HEADER_OFFSET_TAG);
            int typeID = (int) (hdr ^ 0xffffffffL);
            Type type = microVM.getGlobalBundle().getTypeNs().getByID(typeID);
            MemoryDataScanner.scanField(type, 0, iRef, handler);

            long prevTopLoc;
            if (type instanceof Hybrid) {
                prevTopLoc = curTopLoc - 16;
            } else {
                prevTopLoc = curTopLoc - 8;
            }
            
            curTopLoc = prevTopLoc;
        }
    }

    public long getTop() {
        return top;
    }

    // getters and setters

}
