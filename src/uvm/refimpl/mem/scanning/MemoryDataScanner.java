package uvm.refimpl.mem.scanning;

import static uvm.platformsupport.Config.*;
import uvm.refimpl.facade.MicroVM;
import uvm.refimpl.mem.TypeSizes;
import uvm.type.Array;
import uvm.type.Hybrid;
import uvm.type.IRef;
import uvm.type.Ref;
import uvm.type.Struct;
import uvm.type.TagRef64;
import uvm.type.Type;
import uvm.type.WeakRef;

public class MemoryDataScanner {
    public static void scanMemoryData(long objRef, long iRef, MicroVM microVM, RefFieldHandler handler) {
        long tagAddr = objRef + TypeSizes.GC_HEADER_OFFSET_TAG;
        long tag = MEMORY_SUPPORT.loadLong(tagAddr);
        int typeID = (int) (tag & 0x00000000ffffffffL);
        Type type = microVM.getGlobalBundle().getTypeNs()
                .getByID(typeID);
        MemoryDataScanner.scanField(type, objRef, objRef, handler);
    }

    public static void scanField(Type type, long objRef, long iRef,
            RefFieldHandler handler) {
        if (type instanceof Ref || type instanceof IRef
                || type instanceof WeakRef) {
            long toObj = MEMORY_SUPPORT.loadLong(iRef);
            System.out.format("Field %d -> %d\n", iRef, toObj);
            handler.handle(false, null, objRef, iRef, toObj);
        } else if (type instanceof Struct) {
            Struct sTy = (Struct) type;
            long fieldAddr = iRef;
            for (Type fieldTy : sTy.getFieldTypes()) {
                long fieldAlign = TypeSizes.alignOf(fieldTy);
                fieldAddr = TypeSizes.alignUp(fieldAddr, fieldAlign);
                scanField(fieldTy, objRef, fieldAddr, handler);
                fieldAddr += TypeSizes.sizeOf(fieldTy);
            }
        } else if (type instanceof Array) {
            Array aTy = (Array) type;
            Type elemTy = aTy.getElemType();
            long elemSize = TypeSizes.sizeOf(elemTy);
            long elemAlign = TypeSizes.alignOf(elemTy);
            long elemAddr = iRef;
            for (int i = 0; i < aTy.getLength(); i++) {
                scanField(elemTy, objRef, elemAddr, handler);
                elemAddr = TypeSizes.alignUp(elemAddr + elemSize, elemAlign);
            }
        } else if (type instanceof Hybrid) {
            Hybrid hTy = (Hybrid) type;
            Type fixedTy = hTy.getFixedPart();
            Type varTy = hTy.getVarPart();
            long fixedSize = TypeSizes.sizeOf(fixedTy);
            long fixedAlign = TypeSizes.alignOf(fixedTy);
            long varSize = TypeSizes.sizeOf(varTy);
            long varAlign = TypeSizes.alignOf(varTy);
            long curAddr = iRef;

            long varLength = MEMORY_SUPPORT.loadLong(iRef
                    + TypeSizes.GC_HEADER_OFFSET_HYBRID_LENGTH);

            scanField(fixedTy, objRef, curAddr, handler);
            curAddr = TypeSizes.alignUp(curAddr + fixedSize, fixedAlign);

            for (long i = 0; i < varLength; i++) {
                scanField(varTy, objRef, curAddr, handler);
                curAddr = TypeSizes.alignUp(curAddr + varSize, varAlign);
            }
        } else if (type instanceof TagRef64) {
            // TODO: Despite not implemented now, it should be traced only
            // if
            // its tag indicates it is a reference.
        }
    }
}
