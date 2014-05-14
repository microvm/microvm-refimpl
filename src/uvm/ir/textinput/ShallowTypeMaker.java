package uvm.ir.textinput;

import parser.uIRBaseVisitor;
import parser.uIRParser.ArrayTypeContext;
import parser.uIRParser.DoubleTypeContext;
import parser.uIRParser.FloatTypeContext;
import parser.uIRParser.FuncTypeContext;
import parser.uIRParser.HybridTypeContext;
import parser.uIRParser.IRefTypeContext;
import parser.uIRParser.IntTypeContext;
import parser.uIRParser.RefTypeContext;
import parser.uIRParser.StackTypeContext;
import parser.uIRParser.StructTypeContext;
import parser.uIRParser.TagRef64TypeContext;
import parser.uIRParser.ThreadTypeContext;
import parser.uIRParser.VoidTypeContext;
import parser.uIRParser.WeakRefTypeContext;
import uvm.type.Func;
import uvm.type.Hybrid;
import uvm.type.IRef;
import uvm.type.Int;
import uvm.type.Ref;
import uvm.type.Struct;
import uvm.type.TagRef64;
import uvm.type.Type;
import uvm.type.WeakRef;

/**
 * Private for RecursiveBundleBuilder use.
 * <p>
 * Make types according to the grammar, but never handle recursive types ( which
 * takes other types as parameters).
 */
class ShallowTypeMaker extends uIRBaseVisitor<Type> {
    /**
     * The associated RecursiveBundleBuilder instance.
     */
    protected final RecursiveBundleBuilder rbb;

    /**
     * @param recursiveBundleBuilder
     */
    ShallowTypeMaker(RecursiveBundleBuilder recursiveBundleBuilder) {
        this.rbb = recursiveBundleBuilder;
    }

    @Override
    public Type visitIntType(IntTypeContext ctx) {
        int bitSize = this.rbb.intLitToInt(ctx.intLiteral());
        Int type = new Int(bitSize);
        makeIDAndRegister(type);
        return type;
    }

    @Override
    public uvm.type.Float visitFloatType(FloatTypeContext ctx) {
        uvm.type.Float type = new uvm.type.Float();
        makeIDAndRegister(type);
        return type;
    }

    @Override
    public uvm.type.Double visitDoubleType(DoubleTypeContext ctx) {
        uvm.type.Double type = new uvm.type.Double();
        makeIDAndRegister(type);
        return type;
    }

    @Override
    public Ref visitRefType(RefTypeContext ctx) {
        Ref type = new Ref();
        makeIDAndRegister(type);
        return type;
    }

    @Override
    public IRef visitIRefType(IRefTypeContext ctx) {
        IRef type = new IRef();
        makeIDAndRegister(type);
        return type;
    }

    @Override
    public WeakRef visitWeakRefType(WeakRefTypeContext ctx) {
        WeakRef type = new WeakRef();
        makeIDAndRegister(type);
        return type;
    }

    @Override
    public Struct visitStructType(StructTypeContext ctx) {
        Struct type = new Struct();
        makeIDAndRegister(type);
        return type;
    }

    @Override
    public uvm.type.Array visitArrayType(ArrayTypeContext ctx) {
        uvm.type.Array type = new uvm.type.Array();
        type.setLength(this.rbb.intLitToInt(ctx.intLiteral()));
        makeIDAndRegister(type);
        return type;
    }

    @Override
    public Hybrid visitHybridType(HybridTypeContext ctx) {
        Hybrid type = new Hybrid();
        makeIDAndRegister(type);
        return type;
    }

    @Override
    public uvm.type.Void visitVoidType(VoidTypeContext ctx) {
        uvm.type.Void type = new uvm.type.Void();
        makeIDAndRegister(type);
        return type;
    }

    @Override
    public Func visitFuncType(FuncTypeContext ctx) {
        Func type = new Func();
        makeIDAndRegister(type);
        return type;
    }

    @Override
    public uvm.type.Thread visitThreadType(ThreadTypeContext ctx) {
        uvm.type.Thread type = new uvm.type.Thread();
        makeIDAndRegister(type);
        return type;
    }

    @Override
    public uvm.type.Stack visitStackType(StackTypeContext ctx) {
        uvm.type.Stack type = new uvm.type.Stack();
        makeIDAndRegister(type);
        return type;
    }

    @Override
    public TagRef64 visitTagRef64Type(TagRef64TypeContext ctx) {
        TagRef64 type = new TagRef64();
        makeIDAndRegister(type);
        return type;
    }

    private void makeIDAndRegister(Type type) {
        int id = this.rbb.makeID();
        type.setID(id);
        this.rbb.bundle.getTypeNs().put(id, null, type);
    }
}