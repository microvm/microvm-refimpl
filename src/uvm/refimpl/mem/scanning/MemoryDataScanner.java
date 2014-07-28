package uvm.refimpl.mem.scanning;

import static uvm.platformsupport.Config.*;
import uvm.refimpl.facade.MicroVM;
import uvm.refimpl.itpr.OpHelper;
import uvm.refimpl.mem.HeaderUtils;
import uvm.refimpl.mem.TypeSizes;
import uvm.type.Array;
import uvm.type.Hybrid;
import uvm.type.IRef;
import uvm.type.Ref;
import uvm.type.Struct;
import uvm.type.TagRef64;
import uvm.type.Type;
import uvm.type.WeakRef;
import uvm.util.LogUtil;
import uvm.util.Logger;

public class MemoryDataScanner {
    private static final Logger logger = LogUtil.getLogger("MDS");
    private static final Logger loggerParanoid = LogUtil
            .getLogger("MDSParanoia");

    public static void scanMemoryData(long objRef, long iRef, MicroVM microVM,
            RefFieldHandler handler) {
        long tag = HeaderUtils.getTag(objRef);
        Type type = HeaderUtils.getType(microVM, tag);
        MemoryDataScanner.scanField(type, objRef, objRef, handler);
    }

    public static void scanField(Type type, long objRef, long iRef,
            RefFieldHandler handler) {
        if (type instanceof Ref) {
            long toObj = MEMORY_SUPPORT.loadLong(iRef);
            logger.format("Ref field %d -> %d", iRef, toObj);
            handler.handle(false, null, objRef, iRef, toObj, false, false);
        } else if (type instanceof IRef) {
            long toObj = MEMORY_SUPPORT.loadLong(iRef);
            logger.format("IRef field %d -> %d", iRef, toObj);
            handler.handle(false, null, objRef, iRef, toObj, false, false);
        } else if (type instanceof WeakRef) {
            long toObj = MEMORY_SUPPORT.loadLong(iRef);
            logger.format("WeakRef field %d -> %d", iRef, toObj);
            handler.handle(false, null, objRef, iRef, toObj, true, false);
        } else if (type instanceof TagRef64) {
            long bits = MEMORY_SUPPORT.loadLong(iRef);
            if (loggerParanoid.isEnabled()) {
                loggerParanoid.format("Tagref bits %d", bits);
                if (OpHelper.tr64IsFp(bits)) {
                    loggerParanoid.format("Tagref is FP: %f",
                            OpHelper.tr64ToFp(bits));
                } else if (OpHelper.tr64IsInt(bits)) {
                    loggerParanoid.format("Tagref is Int: %d",
                            OpHelper.tr64ToInt(bits));
                } else if (OpHelper.tr64IsRef(bits)) {
                    loggerParanoid.format("Tagref is Ref: %d tag: %d",
                            OpHelper.tr64ToRef(bits), OpHelper.tr64ToTag(bits));
                }
            }
            if (OpHelper.tr64IsRef(bits)) {
                long toObj = OpHelper.tr64ToRef(bits);
                if (logger.isEnabled()) {
                    logger.format("TagRef64 field %d -> %d tag: %d", iRef,
                            toObj, OpHelper.tr64ToTag(bits));
                }
                handler.handle(false, null, objRef, iRef, toObj, false, true);
            }
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

            long varLength = HeaderUtils.getVarLength(iRef);

            scanField(fixedTy, objRef, curAddr, handler);
            curAddr = TypeSizes.alignUp(curAddr + fixedSize, fixedAlign);

            for (long i = 0; i < varLength; i++) {
                scanField(varTy, objRef, curAddr, handler);
                curAddr = TypeSizes.alignUp(curAddr + varSize, varAlign);
            }
        }
    }

}
