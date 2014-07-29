package uvm.ir.text.input;

import static uvm.ir.text.input.ParserHelper.parseError;

import java.util.List;

import org.antlr.v4.runtime.RuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

import parser.uIRBaseVisitor;
import parser.uIRParser.ArgsContext;
import parser.uIRParser.FuncCallBodyContext;
import parser.uIRParser.InstAllocaContext;
import parser.uIRParser.InstAllocaHybridContext;
import parser.uIRParser.InstAtomicRMWContext;
import parser.uIRParser.InstBinOpContext;
import parser.uIRParser.InstBranch2Context;
import parser.uIRParser.InstBranchContext;
import parser.uIRParser.InstCCallContext;
import parser.uIRParser.InstCallContext;
import parser.uIRParser.InstCmpContext;
import parser.uIRParser.InstCmpXchgContext;
import parser.uIRParser.InstContext;
import parser.uIRParser.InstConversionContext;
import parser.uIRParser.InstExtractValueContext;
import parser.uIRParser.InstFenceContext;
import parser.uIRParser.InstGetElemIRefContext;
import parser.uIRParser.InstGetFieldIRefContext;
import parser.uIRParser.InstGetFixedPartIRefContext;
import parser.uIRParser.InstGetIRefContext;
import parser.uIRParser.InstGetVarPartIRefContext;
import parser.uIRParser.InstICallContext;
import parser.uIRParser.InstIInvokeContext;
import parser.uIRParser.InstInsertValueContext;
import parser.uIRParser.InstInvokeContext;
import parser.uIRParser.InstLandingPadContext;
import parser.uIRParser.InstLoadContext;
import parser.uIRParser.InstNewContext;
import parser.uIRParser.InstNewHybridContext;
import parser.uIRParser.InstNewStackContext;
import parser.uIRParser.InstPhiContext;
import parser.uIRParser.InstRetContext;
import parser.uIRParser.InstRetVoidContext;
import parser.uIRParser.InstSelectContext;
import parser.uIRParser.InstShiftIRefContext;
import parser.uIRParser.InstStoreContext;
import parser.uIRParser.InstSwitchContext;
import parser.uIRParser.InstTailCallContext;
import parser.uIRParser.InstThrowContext;
import parser.uIRParser.InstTrapContext;
import parser.uIRParser.InstWatchPointContext;
import parser.uIRParser.KeepAliveContext;
import parser.uIRParser.ValueContext;
import uvm.BasicBlock;
import uvm.ssavalue.CallLike;
import uvm.ssavalue.HandlesException;
import uvm.ssavalue.HasArgs;
import uvm.ssavalue.HasKeepAlives;
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
import uvm.ssavalue.InstGetElemIRef;
import uvm.ssavalue.InstGetFieldIRef;
import uvm.ssavalue.InstGetFixedPartIRef;
import uvm.ssavalue.InstGetIRef;
import uvm.ssavalue.InstGetVarPartIRef;
import uvm.ssavalue.InstICall;
import uvm.ssavalue.InstIInvoke;
import uvm.ssavalue.InstInsertValue;
import uvm.ssavalue.InstInvoke;
import uvm.ssavalue.InstLoad;
import uvm.ssavalue.InstNewHybrid;
import uvm.ssavalue.InstNewStack;
import uvm.ssavalue.InstPhi;
import uvm.ssavalue.InstRet;
import uvm.ssavalue.InstSelect;
import uvm.ssavalue.InstShiftIRef;
import uvm.ssavalue.InstStore;
import uvm.ssavalue.InstSwitch;
import uvm.ssavalue.InstTailCall;
import uvm.ssavalue.InstThrow;
import uvm.ssavalue.InstTrap;
import uvm.ssavalue.InstWatchPoint;
import uvm.ssavalue.Value;
import uvm.type.Int;
import uvm.type.Type;

/**
 * Private for FuncBuilder use.
 * <p>
 * Populate instructions after all instruction instances are created and all
 * names are put into the local name space.
 */
public class PopulateInstruction extends uIRBaseVisitor<Void> {

    private static final Type WORD_TYPE = new Int(64);
    private FuncBuilder fb;

    public PopulateInstruction(FuncBuilder funcBuilder) {
        this.fb = funcBuilder;
    }

    @SuppressWarnings("unchecked")
    private <T> T ctxToInst(RuleContext ctx) {
        return (T) fb.ctxToInst.get(ctx);
    }

    private Value value(ValueContext ctx, Type hint) {
        return fb.value(ctx, hint);
    }

    private Value value(ValueContext ctx) {
        return value(ctx, null);
    }

    private Value localVal(ValueContext ctx) {
        return fb.localVal(ctx);
    }

    private BasicBlock bb(String name) {
        BasicBlock bb = fb.cfg.getBBNs().getByName(name);
        if (bb == null) {
            parseError("Undefined label " + name);
        }
        return bb;
    }

    private BasicBlock bb(TerminalNode tn) {
        String name = tn.getText();
        return bb(name);
    }

    @Override
    public Void visitInst(InstContext ctx) {
        return visit(ctx.instBody());
    }

    @Override
    public Void visitInstBinOp(InstBinOpContext ctx) {
        InstBinOp inst = ctxToInst(ctx);
        inst.setOp1(value(ctx.value(0), inst.getType()));
        inst.setOp2(value(ctx.value(1), inst.getType()));
        return null;
    }

    @Override
    public Void visitInstCmp(InstCmpContext ctx) {
        InstCmp inst = ctxToInst(ctx);
        inst.setOp1(value(ctx.value(0), inst.getOpndType()));
        inst.setOp2(value(ctx.value(1), inst.getOpndType()));
        return null;
    }

    @Override
    public Void visitInstConversion(InstConversionContext ctx) {
        InstConversion inst = ctxToInst(ctx);
        inst.setOpnd(value(ctx.value(), inst.getFromType()));
        return null;
    }

    private static Int INT1 = new Int(1);

    @Override
    public Void visitInstSelect(InstSelectContext ctx) {
        InstSelect inst = ctxToInst(ctx);
        inst.setCond(value(ctx.value(0), INT1));
        inst.setIfTrue(value(ctx.value(1), inst.getType()));
        inst.setIfFalse(value(ctx.value(2), inst.getType()));
        return null;
    }

    @Override
    public Void visitInstBranch(InstBranchContext ctx) {
        InstBranch inst = ctxToInst(ctx);
        inst.setDest(bb(ctx.LOCAL_ID()));
        return null;
    }

    @Override
    public Void visitInstBranch2(InstBranch2Context ctx) {
        InstBranch2 inst = ctxToInst(ctx);
        inst.setCond(value(ctx.value(), INT1));
        inst.setIfTrue(bb(ctx.LOCAL_ID(0)));
        inst.setIfFalse(bb(ctx.LOCAL_ID(1)));
        return null;
    }

    @Override
    public Void visitInstSwitch(InstSwitchContext ctx) {
        InstSwitch inst = ctxToInst(ctx);
        inst.setOpnd(value(ctx.value(0), inst.getOpndType()));
        inst.setDefaultDest(bb(ctx.LOCAL_ID(0)));

        for (int i = 1; i < ctx.LOCAL_ID().size(); i++) {
            Value cas = value(ctx.value(i), inst.getOpndType());
            BasicBlock dst = bb(ctx.LOCAL_ID(i));
            inst.setDestFor(cas, dst);
        }
        return null;
    }

    @Override
    public Void visitInstPhi(InstPhiContext ctx) {
        InstPhi inst = ctxToInst(ctx);

        for (int i = 0; i < ctx.LOCAL_ID().size(); i++) {
            BasicBlock src = bb(ctx.LOCAL_ID(i));
            Value val = value(ctx.value(i), inst.getType());
            inst.setValueFrom(src, val);
        }
        return null;
    }

    private void populateCall(CallLike inst, FuncCallBodyContext ctx) {
        inst.setFunc(value(ctx.value()));
        List<ValueContext> valCtxs = ctx.args().value();
        List<Type> hints = inst.getSig().getParamTypes();
        for (int i = 0; i < valCtxs.size(); i++) {
            ValueContext vCtx = valCtxs.get(i);
            Type hint = hints.get(i);
            Value val = value(vCtx, hint);
            inst.addArg(val);
        }
    }

    private void populateNorExc(HandlesException inst, TerminalNode nor,
            TerminalNode exc) {
        inst.setNor(bb(nor));
        inst.setExc(bb(exc));
    }

    private void populateMaybeKeepAlives(HasKeepAlives inst,
            KeepAliveContext ctx) {
        if (ctx != null) {
            for (int i = 0; i < ctx.value().size(); i++) {
                Value val = localVal(ctx.value(i));
                inst.addKeepAlive(val);
            }
        }
    }

    @Override
    public Void visitInstCall(InstCallContext ctx) {
        InstCall inst = ctxToInst(ctx);
        populateCall(inst, ctx.funcCallBody());
        populateMaybeKeepAlives(inst, ctx.keepAlive());
        return null;
    }

    @Override
    public Void visitInstInvoke(InstInvokeContext ctx) {
        InstInvoke inst = ctxToInst(ctx);
        populateCall(inst, ctx.funcCallBody());
        populateNorExc(inst, ctx.LOCAL_ID(0), ctx.LOCAL_ID(1));
        populateMaybeKeepAlives(inst, ctx.keepAlive());
        return null;
    }

    @Override
    public Void visitInstTailCall(InstTailCallContext ctx) {
        InstTailCall inst = ctxToInst(ctx);
        populateCall(inst, ctx.funcCallBody());
        return null;
    }

    @Override
    public Void visitInstRet(InstRetContext ctx) {
        InstRet inst = ctxToInst(ctx);
        inst.setRetVal(value(ctx.value(), inst.getRetType()));
        return null;
    }

    @Override
    public Void visitInstRetVoid(InstRetVoidContext ctx) {
        // Do nothing.
        return null;
    }

    @Override
    public Void visitInstThrow(InstThrowContext ctx) {
        InstThrow inst = ctxToInst(ctx);
        inst.setException(value(ctx.value()));
        return null;
    }

    @Override
    public Void visitInstLandingPad(InstLandingPadContext ctx) {
        // Do nothing.
        return null;
    }

    @Override
    public Void visitInstExtractValue(InstExtractValueContext ctx) {
        InstExtractValue inst = ctxToInst(ctx);
        inst.setOpnd(value(ctx.value(), inst.getStructType()));
        return null;
    }

    @Override
    public Void visitInstInsertValue(InstInsertValueContext ctx) {
        InstInsertValue inst = ctxToInst(ctx);
        inst.setOpnd(value(ctx.value(0), inst.getStructType()));
        inst.setNewVal(value(ctx.value(1), inst.getFieldType()));
        return null;
    }

    @Override
    public Void visitInstNew(InstNewContext ctx) {
        // Do nothing.
        return null;
    }

    @Override
    public Void visitInstNewHybrid(InstNewHybridContext ctx) {
        InstNewHybrid inst = ctxToInst(ctx);
        inst.setLength(value(ctx.value(), WORD_TYPE));
        return null;
    }

    @Override
    public Void visitInstAlloca(InstAllocaContext ctx) {
        // Do nothing.
        return null;
    }

    @Override
    public Void visitInstAllocaHybrid(InstAllocaHybridContext ctx) {
        InstAllocaHybrid inst = ctxToInst(ctx);
        inst.setLength(value(ctx.value(), WORD_TYPE));
        return null;
    }

    @Override
    public Void visitInstGetIRef(InstGetIRefContext ctx) {
        InstGetIRef inst = ctxToInst(ctx);
        inst.setOpnd(value(ctx.value()));
        return null;
    }

    @Override
    public Void visitInstGetFieldIRef(InstGetFieldIRefContext ctx) {
        InstGetFieldIRef inst = ctxToInst(ctx);
        inst.setOpnd(value(ctx.value()));
        return null;
    }

    @Override
    public Void visitInstGetElemIRef(InstGetElemIRefContext ctx) {
        InstGetElemIRef inst = ctxToInst(ctx);
        inst.setOpnd(value(ctx.value(0)));
        inst.setIndex(value(ctx.value(1), WORD_TYPE));
        return null;
    }

    @Override
    public Void visitInstShiftIRef(InstShiftIRefContext ctx) {
        InstShiftIRef inst = ctxToInst(ctx);
        inst.setOpnd(value(ctx.value(0)));
        inst.setOffset(value(ctx.value(1), WORD_TYPE));
        return null;
    }

    @Override
    public Void visitInstGetFixedPartIRef(InstGetFixedPartIRefContext ctx) {
        InstGetFixedPartIRef inst = ctxToInst(ctx);
        inst.setOpnd(value(ctx.value()));
        return null;
    }

    @Override
    public Void visitInstGetVarPartIRef(InstGetVarPartIRefContext ctx) {
        InstGetVarPartIRef inst = ctxToInst(ctx);
        inst.setOpnd(value(ctx.value()));
        return null;
    }

    @Override
    public Void visitInstLoad(InstLoadContext ctx) {
        InstLoad inst = ctxToInst(ctx);
        inst.setLocation(value(ctx.value()));
        return null;
    }

    @Override
    public Void visitInstStore(InstStoreContext ctx) {
        InstStore inst = ctxToInst(ctx);
        inst.setLocation(value(ctx.value(0)));
        inst.setNewVal(value(ctx.value(1), inst.getReferentType()));
        return null;
    }

    @Override
    public Void visitInstCmpXchg(InstCmpXchgContext ctx) {
        InstCmpXchg inst = ctxToInst(ctx);
        inst.setLocation(value(ctx.value(0)));
        inst.setExpected(value(ctx.value(1), inst.getReferentType()));
        inst.setDesired(value(ctx.value(2), inst.getReferentType()));
        return null;
    }

    @Override
    public Void visitInstAtomicRMW(InstAtomicRMWContext ctx) {
        InstAtomicRMW inst = ctxToInst(ctx);
        inst.setLocation(value(ctx.value(0)));
        inst.setOpnd(value(ctx.value(1), inst.getReferentType()));
        return null;
    }

    @Override
    public Void visitInstFence(InstFenceContext ctx) {
        // Do nothing.
        return null;
    }

    @Override
    public Void visitInstTrap(InstTrapContext ctx) {
        InstTrap inst = ctxToInst(ctx);
        populateNorExc(inst, ctx.LOCAL_ID(0), ctx.LOCAL_ID(1));
        populateMaybeKeepAlives(inst, ctx.keepAlive());
        return null;
    }

    @Override
    public Void visitInstWatchPoint(InstWatchPointContext ctx) {
        InstWatchPoint inst = ctxToInst(ctx);
        inst.setDisabled(bb(ctx.LOCAL_ID(0)));
        populateNorExc(inst, ctx.LOCAL_ID(1), ctx.LOCAL_ID(2));
        populateMaybeKeepAlives(inst, ctx.keepAlive());
        return null;
    }

    @Override
    public Void visitInstCCall(InstCCallContext ctx) {
        InstCCall inst = ctxToInst(ctx);
        populateCall(inst, ctx.funcCallBody());
        return null;
    }

    @Override
    public Void visitInstNewStack(InstNewStackContext ctx) {
        InstNewStack inst = ctxToInst(ctx);
        populateCall(inst, ctx.funcCallBody());
        return null;
    }

    /**
     * Populate arguments for those instructions which has args but not
     * signatures. Argument types cannot be inferred (unless a better
     * implementation of intrinsic functions provide parameter type
     * information).
     */
    private void populateArgs(HasArgs inst, ArgsContext args) {
        for (int i = 0; i < args.value().size(); i++) {
            Value arg = value(args.value(i));
            inst.addArg(arg);
        }
    }

    @Override
    public Void visitInstICall(InstICallContext ctx) {
        InstICall inst = ctxToInst(ctx);
        populateArgs(inst, ctx.args());
        populateMaybeKeepAlives(inst, ctx.keepAlive());
        return null;
    }

    @Override
    public Void visitInstIInvoke(InstIInvokeContext ctx) {
        InstIInvoke inst = ctxToInst(ctx);
        populateArgs(inst, ctx.args());
        populateNorExc(inst, ctx.LOCAL_ID(0), ctx.LOCAL_ID(1));
        populateMaybeKeepAlives(inst, ctx.keepAlive());
        return null;
    }

}
