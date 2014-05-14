package uvm.ir.textoutput;

import static uvm.ir.textoutput.WritingHelper.g;
import static uvm.ir.textoutput.WritingHelper.l;

import java.util.Map;

import uvm.BasicBlock;
import uvm.ssavalue.CallLike;
import uvm.ssavalue.Constant;
import uvm.ssavalue.DoubleConstant;
import uvm.ssavalue.FloatConstant;
import uvm.ssavalue.FunctionConstant;
import uvm.ssavalue.GlobalDataConstant;
import uvm.ssavalue.HandlesException;
import uvm.ssavalue.HasArgs;
import uvm.ssavalue.HasKeepAlives;
import uvm.ssavalue.InstAlloca;
import uvm.ssavalue.InstAllocaHybrid;
import uvm.ssavalue.InstAtomicRMW;
import uvm.ssavalue.InstBinOp;
import uvm.ssavalue.InstBranch;
import uvm.ssavalue.InstBranch2;
import uvm.ssavalue.InstCCall;
import uvm.ssavalue.InstCall;
import uvm.ssavalue.InstCmp;
import uvm.ssavalue.InstCmpXchg;
import uvm.ssavalue.InstConversion;
import uvm.ssavalue.InstExtractValue;
import uvm.ssavalue.InstFence;
import uvm.ssavalue.InstGetElemIRef;
import uvm.ssavalue.InstGetFieldIRef;
import uvm.ssavalue.InstGetFixedPartIRef;
import uvm.ssavalue.InstGetIRef;
import uvm.ssavalue.InstGetVarPartIRef;
import uvm.ssavalue.InstICall;
import uvm.ssavalue.InstIInvoke;
import uvm.ssavalue.InstInsertValue;
import uvm.ssavalue.InstInvoke;
import uvm.ssavalue.InstLandingPad;
import uvm.ssavalue.InstLoad;
import uvm.ssavalue.InstNew;
import uvm.ssavalue.InstNewHybrid;
import uvm.ssavalue.InstNewStack;
import uvm.ssavalue.InstPhi;
import uvm.ssavalue.InstRet;
import uvm.ssavalue.InstRetVoid;
import uvm.ssavalue.InstSelect;
import uvm.ssavalue.InstShiftIRef;
import uvm.ssavalue.InstStore;
import uvm.ssavalue.InstSwitch;
import uvm.ssavalue.InstTailCall;
import uvm.ssavalue.InstThrow;
import uvm.ssavalue.InstTrap;
import uvm.ssavalue.InstWatchPoint;
import uvm.ssavalue.IntConstant;
import uvm.ssavalue.NullConstant;
import uvm.ssavalue.Parameter;
import uvm.ssavalue.StructConstant;
import uvm.ssavalue.UseBox;
import uvm.ssavalue.Value;
import uvm.ssavalue.ValueVisitor;

/**
 * Writes a value. For IRTextWriter use only.
 */
public class ValueWriter implements ValueVisitor<String> {
    private IRTextWriter irTextWriter;

    public ValueWriter(IRTextWriter irTextWriter) {
        this.irTextWriter = irTextWriter;
    }

    public static String v(Value value) {
        if (value instanceof Constant) {
            return g(value);
        } else {
            return l(value);
        }
    }

    @Override
    public String visitIntConstant(IntConstant constant) {
        return Long.toString(constant.getValue());
    }

    @Override
    public String visitFloatConstant(FloatConstant floatConstant) {
        float value = floatConstant.getValue();
        int bitsValue = Float.floatToIntBits(value);
        String rv = String.format("bitsf(0x%x)", bitsValue);
        irTextWriter.addComment(String.format("%s = %f", rv, value));
        return rv;
    }

    @Override
    public String visitDoubleConstant(DoubleConstant doubleConstant) {
        double value = doubleConstant.getValue();
        long bitsValue = Double.doubleToLongBits(value);
        String rv = String.format("bitsd(0x%x)", bitsValue);
        irTextWriter.addComment(String.format("%s = %f", rv, value));
        return rv;
    }

    @Override
    public String visitStructConstant(StructConstant constant) {
        StringBuilder sb = new StringBuilder();
        sb.append("{ ");
        for (Constant field : constant.getValues()) {
            sb.append(g(field));
            sb.append(" ");
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    public String visitNullConstant(NullConstant constant) {
        return "NULL";
    }

    @Override
    public String visitGlobalDataConstant(GlobalDataConstant constant) {
        // Implicitly created only. Cannot be written.
        return null;
    }

    @Override
    public String visitFunctionConstant(FunctionConstant functionConstant) {
        // Implicitly created only. Cannot be written.
        return null;
    }

    @Override
    public String visitParameter(Parameter parameter) {
        // Implicitly created only. Cannot be written.
        return null;
    }

    @Override
    public String visitBinOp(InstBinOp inst) {
        return String.format("%s <%s> %s %s", inst.getOptr().toString(),
                g(inst.getType()), v(inst.getOp1()), v(inst.getOp2()));
    }

    @Override
    public String visitCmp(InstCmp inst) {
        return String.format("%s <%s> %s %s", inst.getOptr().toString(),
                g(inst.getOpndType()), v(inst.getOp1()), v(inst.getOp2()));
    }

    @Override
    public String visitConversion(InstConversion inst) {
        return String.format("%s <%s %s> %s", inst.getOptr().toString(),
                g(inst.getFromType()), g(inst.getToType()), v(inst.getOpnd()));
    }

    @Override
    public String visitSelect(InstSelect inst) {
        return String.format("SELECT <%s> %s %s", g(inst.getType()),
                v(inst.getIfTrue()), v(inst.getIfFalse()));
    }

    @Override
    public String visitBranch(InstBranch inst) {
        return String.format("BRANCH %s", l(inst.getDest()));
    }

    @Override
    public String visitBranch2(InstBranch2 inst) {
        return String.format("BRANCH2 %s %s %s", v(inst.getCond()),
                l(inst.getIfTrue()), l(inst.getIfFalse()));
    }

    @Override
    public String visitSwitch(InstSwitch inst) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("SWITCH <%s> %s %s { ", g(inst.getOpnd()),
                v(inst.getOpnd()), l(inst.getDefaultDest())));
        for (Map.Entry<UseBox, BasicBlock> e : inst.getCases().entrySet()) {
            sb.append(String.format("%s: %s; ", v(e.getKey().getDst()),
                    l(e.getValue())));
        }
        sb.append("}");
        return sb.toString();
    }

    @Override
    public String visitPhi(InstPhi inst) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("PHI <%s> { ", g(inst.getType())));
        for (Map.Entry<BasicBlock, UseBox> e : inst.getValueMap().entrySet()) {
            sb.append(String.format("%s: %s; ", l(e.getKey()), v(e.getValue()
                    .getDst())));
        }
        sb.append("}");
        return sb.toString();
    }

    private String printArgs(HasArgs inst) {
        StringBuilder sb = new StringBuilder("(");
        for (UseBox arg : inst.getArgs()) {
            sb.append(" ").append(v(arg.getDst()));
        }
        sb.append(" )");
        return sb.toString();
    }

    private String printFuncCallBody(CallLike inst) {
        return String.format("<%s> %s %s", g(inst.getSig()), v(inst.getFunc()),
                printArgs(inst));
    }

    private String printKeepAlive(HasKeepAlives inst) {
        StringBuilder sb = new StringBuilder("KEEPALIVE (");
        for (UseBox ka : inst.getKeepAlives()) {
            sb.append(" ").append(v(ka.getDst()));
        }
        sb.append(" )");
        return sb.toString();
    }

    @Override
    public String visitCall(InstCall inst) {
        return String.format("CALL %s %s", printFuncCallBody(inst),
                printKeepAlive(inst));
    }

    private String printNorExc(HandlesException inst) {
        return String.format("%s %s", l(inst.getNor()), l(inst.getExc()));
    }

    @Override
    public String visitInvoke(InstInvoke inst) {
        return String.format("INVOKE %s %s %s", printFuncCallBody(inst),
                printNorExc(inst), printKeepAlive(inst));
    }

    @Override
    public String visitTailCall(InstTailCall inst) {
        return String.format("TAILCALL %s", printFuncCallBody(inst));
    }

    @Override
    public String visitRet(InstRet inst) {
        return String.format("RET <%s> %s", g(inst.getRetType()),
                v(inst.getRetVal()));
    }

    @Override
    public String visitRetVoid(InstRetVoid inst) {
        return "RETVOID";
    }

    @Override
    public String visitThrow(InstThrow inst) {
        return String.format("THROW %s", v(inst.getException()));
    }

    @Override
    public String visitLandingPad(InstLandingPad inst) {
        return "LANDINGPAD";
    }

    @Override
    public String visitExtractValue(InstExtractValue inst) {
        return String.format("EXTRACTVALUE <%s %d> %s",
                g(inst.getStructType()), inst.getIndex(), v(inst.getOpnd()));
    }

    @Override
    public String visitInsertValue(InstInsertValue inst) {
        return String.format("INSERTVALUE <%s %d> %s", g(inst.getStructType()),
                inst.getIndex(), v(inst.getOpnd()), v(inst.getNewVal()));
    }

    @Override
    public String visitNew(InstNew inst) {
        return String.format("NEW <%s>", g(inst.getAllocType()));
    }

    @Override
    public String visitNewHybrid(InstNewHybrid inst) {
        return String.format("NEWHYBRID <%s> %s", g(inst.getAllocType()),
                v(inst.getLength()));
    }

    @Override
    public String visitAlloca(InstAlloca inst) {
        return String.format("ALLOCA <%s>", g(inst.getAllocType()));
    }

    @Override
    public String visitAllocaHybrid(InstAllocaHybrid inst) {
        return String.format("ALLOCAHYBRID <%s> %s", g(inst.getAllocType()),
                v(inst.getLength()));
    }

    @Override
    public String visitGetIRef(InstGetIRef inst) {
        return String.format("GETIREF <%s> %s", g(inst.getReferentType()),
                v(inst.getOpnd()));
    }

    @Override
    public String visitGetFieldIRef(InstGetFieldIRef inst) {
        return String.format("GETFIELDIREF <%s %d> %s",
                g(inst.getReferentType()), inst.getIndex(), v(inst.getOpnd()));
    }

    @Override
    public String visitGetElemIRef(InstGetElemIRef inst) {
        return String.format("GETELEMIREF <%s> %s %s",
                g(inst.getReferentType()), v(inst.getOpnd()),
                v(inst.getIndex()));
    }

    @Override
    public String visitShiftIRef(InstShiftIRef inst) {
        return String.format("SHIFTIREF <%s> %s %s", g(inst.getReferentType()),
                v(inst.getOpnd()), v(inst.getOffset()));
    }

    @Override
    public String visitGetFixedPartIRef(InstGetFixedPartIRef inst) {
        return String.format("GETFIXEDPARTIREF <%s> %s",
                g(inst.getReferentType()), v(inst.getOpnd()));
    }

    @Override
    public String visitGetVarPartIRef(InstGetVarPartIRef inst) {
        return String.format("GETVARPARTIREF <%s> %s",
                g(inst.getReferentType()), v(inst.getOpnd()));
    }

    @Override
    public String visitLoad(InstLoad inst) {
        return String.format("LOAD %s <%s> %s", inst.getOrdering().toString(),
                g(inst.getReferentType()), v(inst.getLocation()));
    }

    @Override
    public String visitStore(InstStore inst) {
        return String.format("STORE %s <%s> %s", inst.getOrdering().toString(),
                g(inst.getReferentType()), v(inst.getLocation()),
                v(inst.getNewVal()));
    }

    @Override
    public String visitCmpXchg(InstCmpXchg inst) {
        return String.format("CMPXCHG %s %s <%s> %s %s %s", inst
                .getOrderingSucc().toString(), inst.getOrderingFail()
                .toString(), g(inst.getReferentType()), v(inst.getLocation()),
                v(inst.getExpected()), v(inst.getDesired()));
    }

    @Override
    public String visitAtomicRMW(InstAtomicRMW inst) {
        return String.format("ATOMICRMW %s %s <%s> %s %s", inst
                .getOrdering().toString(), inst.getOptr().toString(), g(inst
                .getReferentType()), v(inst.getLocation()), v(inst.getOpnd()));
    }

    @Override
    public String visitFence(InstFence inst) {
        return String.format("FENCE %s", inst.getOrdering().toString());
    }

    @Override
    public String visitTrap(InstTrap inst) {
        return String.format("TRAP <%s> %s %s", g(inst.getType()),
                printNorExc(inst), printKeepAlive(inst));
    }

    @Override
    public String visitWatchPoint(InstWatchPoint inst) {
        return String.format("WATCHPOINT %d <%s> %s %s %s",
                inst.getWatchPointId(), g(inst.getType()),
                l(inst.getDisabled()), printNorExc(inst), printKeepAlive(inst));
    }

    @Override
    public String visitCCall(InstCCall inst) {
        return String.format("CCALL %s %s", inst.getCallConv().toString(),
                printFuncCallBody(inst));

    }

    @Override
    public String visitNewStack(InstNewStack inst) {
        return String.format("NEWSTACK %s", printFuncCallBody(inst));
    }

    @Override
    public String visitICall(InstICall inst) {
        return String.format("ICALL %s %s %s", g(inst.getIFunc()),
                printArgs(inst), printKeepAlive(inst));
    }

    @Override
    public String visitIInvoke(InstIInvoke inst) {
        return String.format("IINVOKE %s %s %s %s", g(inst.getIFunc()),
                printArgs(inst), printNorExc(inst), printKeepAlive(inst));
    }

}
