package parser;

import parser.uIRParser.FuncSigConstructorContext;
import uvm.FunctionSignature;

/**
 * Private for RecursiveBundleBuilder use.
 * <p>
 * Similar to ShallowTypeMaker, but works on ".funcsig" definitions.
 */
class ShallowFuncSigMaker extends uIRBaseVisitor<FunctionSignature> {
    final RecursiveBundleBuilder rbb;

    ShallowFuncSigMaker(RecursiveBundleBuilder recursiveBundleBuilder) {
        rbb = recursiveBundleBuilder;
    }

    @Override
    public FunctionSignature visitFuncSigConstructor(
            FuncSigConstructorContext ctx) {
        FunctionSignature sig = new FunctionSignature();
        makeIDAndRegister(sig);
        return sig;
    }

    private void makeIDAndRegister(FunctionSignature sig) {
        int id = rbb.makeID();
        sig.setID(id);
        rbb.bundle.getFuncSigNs().put(id, null, sig);
    }
}