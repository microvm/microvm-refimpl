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
        sig.setID(rbb.makeID());
        rbb.bundle.registerFuncSig(sig.getID(), null, sig);
        return sig;
    }
}