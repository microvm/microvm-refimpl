package uvm.ir.text.input;

import parser.uIRBaseVisitor;
import parser.uIRParser.ConstDefContext;
import parser.uIRParser.FuncDeclContext;
import parser.uIRParser.FuncDefContext;
import parser.uIRParser.FuncSigDefContext;
import parser.uIRParser.GlobalDefContext;
import parser.uIRParser.IntLiteralContext;
import parser.uIRParser.IrContext;
import parser.uIRParser.TypeDefContext;
import uvm.Bundle;
import uvm.Function;
import uvm.FunctionSignature;
import uvm.GlobalData;
import uvm.IdentifiedHelper;
import uvm.ssavalue.Constant;
import uvm.ssavalue.FunctionConstant;
import uvm.ssavalue.GlobalDataConstant;
import uvm.type.Type;

/**
 * RecursiveUIRBuilder builds a uvm Bundle from a uir parse tree.
 * <p>
 * Not thread safe. Don't use from multiple threads.
 * <p>
 * One time use only. Please create a new instance for each bundle.
 */
public class RecursiveBundleBuilder {
    private static final Bundle EMPTY_BUNDLE = new Bundle();

    // Interface for the user.

    /**
     * The global bundle, used to resolve non-local IDs (for types, signatures,
     * constants, globals and functions).
     */
    Bundle globalBundle;

    /**
     * The result bundle.
     */
    Bundle bundle;

    public RecursiveBundleBuilder() {
        this(EMPTY_BUNDLE);
    }

    public RecursiveBundleBuilder(Bundle globalBundle) {
        bundle = new Bundle();
        this.globalBundle = globalBundle;
    }

    /**
     * @return The result bundle. Should only be called after building.
     */
    public Bundle getBundle() {
        return bundle;
    }

    /**
     * Build the bundle.
     * 
     * @param ir
     *            The IrContext object representing the "ir" non-terminal.
     */
    public void build(IrContext ir) {
        // Build types in two passes.

        // The first pass, create a stub for all .typedef and .funcsig.
        uIRBaseVisitor<Void> makeDeclaredTypes = new uIRBaseVisitor<Void>() {
            @Override
            public Void visitTypeDef(TypeDefContext ctx) {
                handleTypeDef(ctx);
                return null;
            }

            @Override
            public Void visitFuncSigDef(FuncSigDefContext ctx) {
                handleFuncSigDef(ctx);
                return null;
            }
        };
        makeDeclaredTypes.visit(ir);

        // The second pass, populate its dependencies.
        uIRBaseVisitor<Void> populateDeclaredTypes = new uIRBaseVisitor<Void>() {
            @Override
            public Void visitTypeDef(TypeDefContext ctx) {
                populateTypeDef(ctx);
                return null;
            }

            @Override
            public Void visitFuncSigDef(FuncSigDefContext ctx) {
                populateFuncSigDef(ctx);
                return null;
            }

        };
        populateDeclaredTypes.visit(ir);

        // Visit all global constant definitions, global data definitions,
        // function definitions and function declarations. They all depend on
        // types.
        uIRBaseVisitor<Void> otherDefHandler = new uIRBaseVisitor<Void>() {
            @Override
            public Void visitConstDef(ConstDefContext ctx) {
                handleConstDefShallow(ctx);
                return null;
            }

            @Override
            public Void visitGlobalDef(GlobalDefContext ctx) {
                handleGlobalData(ctx);
                return null;
            }

            @Override
            public Void visitFuncDef(FuncDefContext ctx) {
                handleFuncDefShallow(ctx);
                return null;
            }

            @Override
            public Void visitFuncDecl(FuncDeclContext ctx) {
                handleFuncDecl(ctx);
                return null;
            }
        };
        otherDefHandler.visit(ir);

        // Re-visit all global constant definitions to populate nested
        // constants. In this case they are all Struct constants.
        uIRBaseVisitor<Void> populateConstDef = new uIRBaseVisitor<Void>() {
            @Override
            public Void visitConstDef(ConstDefContext ctx) {
                populateConstDef(ctx);
                return null;
            }
        };
        populateConstDef.visit(ir);

        // Re-visit all function definitions (.funcdef) to make the function
        // bodys.
        uIRBaseVisitor<Void> buildFuncBody = new uIRBaseVisitor<Void>() {
            @Override
            public Void visitFuncDef(FuncDefContext ctx) {
                String name = ctx.GLOBAL_ID().getText();
                Function func = bundle.getFuncNs().getByName(name);
                FuncBuilder funcBuilder = new FuncBuilder(
                        RecursiveBundleBuilder.this, func);
                funcBuilder.handleFuncDef(ctx);
                return null;
            };
        };
        buildFuncBody.visit(ir);
    }

    int makeID() {
        return IDMakerForText.INSTANCE.makeID();
    }

    int getOldFuncID(String name) {
        Function func = globalBundle.getFuncNs().getByName(name);
        if (func != null) {
            return func.getID();
        } else {
            return 0;
        }
    }

    // Types and function signatures

    private void handleTypeDef(TypeDefContext ctx) {
        Type type = shallowTypeMaker.visit(ctx.typeConstructor());
        String name = ctx.GLOBAL_ID().getText();
        type.setName(name);
        bundle.getTypeNs().bind(type.getID(), name);
    }

    private void handleFuncSigDef(FuncSigDefContext ctx) {
        FunctionSignature sig = shallowFuncSigMaker.visit(ctx
                .funcSigConstructor());
        String name = ctx.GLOBAL_ID().getText();
        sig.setName(name);
        bundle.getFuncSigNs().bind(sig.getID(), name);
    }

    private void populateTypeDef(TypeDefContext ctx) {
        String name = ctx.GLOBAL_ID().getText();
        final Type oldType = bundle.getTypeNs().getByName(name);

        new PopulateDeclaredTypeAndSig(RecursiveBundleBuilder.this, oldType)
                .visit(ctx.typeConstructor());
    }

    private void populateFuncSigDef(FuncSigDefContext ctx) {
        String name = ctx.GLOBAL_ID().getText();
        FunctionSignature sig = bundle.getFuncSigNs().getByName(name);

        typeAndSigPopulator.visitFuncSigConstructor(sig,
                ctx.funcSigConstructor());
    }

    // Associated classes for type/funcSig creation and population.
    // They are externalised for their sizes.

    ShallowTypeMaker shallowTypeMaker = new ShallowTypeMaker(this);
    ShallowFuncSigMaker shallowFuncSigMaker = new ShallowFuncSigMaker(this);

    TypeAndSigPopulator typeAndSigPopulator = new TypeAndSigPopulator(this);

    DeepTypeMaker deepTypeMaker = new DeepTypeMaker(this);
    DeepFuncSigMaker deepFuncSigMaker = new DeepFuncSigMaker(this);

    // Literal parsers.
    // Literals may and may not be Constants.
    // The int type also takes a literal as a parameter.

    IntLiteralParser intLiteralParser = new IntLiteralParser();
    FloatLiteralParser floatLiteralParser = new FloatLiteralParser();
    DoubleLiteralParser doubleLiteralParser = new DoubleLiteralParser();

    /**
     * A convenient method that handles the cast.
     */
    long intLitToLong(IntLiteralContext ctx) {
        return (long) intLiteralParser.visit(ctx);
    }

    /**
     * A convenient method that handles the cast.
     */
    int intLitToInt(IntLiteralContext ctx) {
        return (int) ((long) intLiteralParser.visit(ctx));
    }

    // Constant processors

    /**
     * A general function to handle ".const" definitions, global or local.
     */
    private Constant handleConstDefShallow(ConstDefContext ctx) {
        String name = ctx.GLOBAL_ID().getText();

        Type type = deepTypeMaker.visit(ctx.type());
        Constant constant = new ShallowConstMaker(this, type).visit(ctx
                .constExpr());

        constant.setName(name);

        bundle.getGlobalValueNs().bind(constant.getID(), name);
        bundle.getDeclaredConstNs().bind(constant.getID(), name);

        return constant;
    }

    private void populateConstDef(ConstDefContext ctx) {
        String name = ctx.GLOBAL_ID().getText();
        Constant constant = bundle.getDeclaredConstNs().getByName(name);
        new PopulateDeclaredConst(this, constant).visit(ctx);
    }

    // The same shallow-populator-deep pattern as types.
    ConstPopulator constPopulator = new ConstPopulator(this);

    // Global data

    private GlobalData makeGlobalData(GlobalDefContext ctx) {
        GlobalData globalData = new GlobalData();
        int id = makeID();
        globalData.setID(id);
        String name = ctx.GLOBAL_ID().getText();
        globalData.setName(name);

        Type type = deepTypeMaker.visit(ctx.type());
        globalData.setType(type);

        bundle.getGlobalDataNs().put(id, name, globalData);
        return globalData;
    }

    private GlobalDataConstant makeGlobalDataConstant(GlobalData globalData) {
        GlobalDataConstant constant = new GlobalDataConstant();
        constant.setID(globalData.getID());
        constant.setName(globalData.getName());
        constant.setGlobalData(globalData);

        bundle.getGlobalValueNs().put(constant.getID(), constant.getName(),
                constant);

        return constant;
    }

    private void handleGlobalData(GlobalDefContext ctx) {
        GlobalData globalData = makeGlobalData(ctx);
        makeGlobalDataConstant(globalData);
    }

    // Functions

    private Function declareFunction(String name, FunctionSignature sig) {
        Function function = new Function();
        int oldID = getOldFuncID(name);
        int id = oldID == 0 ? makeID() : oldID;
        function.setID(id);
        function.setName(name);
        function.setSig(sig);

        bundle.getFuncNs().put(id, name, function);
        if (oldID == 0) {
            makeFunctionConstant(function);
        }
        return function;
    }

    private FunctionConstant makeFunctionConstant(Function function) {
        FunctionConstant constant = new FunctionConstant();
        constant.setID(function.getID());
        constant.setName(function.getName());
        constant.setFunction(function);

        bundle.getGlobalValueNs().put(constant.getID(), constant.getName(),
                constant);

        return constant;
    }

    /**
     * Only shallowly handle a function definition, i.e. treat it as a function
     * declaration. The function body will be constructed in the next phase.
     */
    private void handleFuncDefShallow(FuncDefContext ctx) {
        // If already declared, do not declare again.
        // Potential error: an erroneous µVM-IR program may contain multiple
        // definitions of the same function with different signatures. We do not
        // check this error.

        String name = ctx.GLOBAL_ID().getText();

        // If already declared, do not declare again.
        // Potential error: an erroneous µVM-IR program may contain multiple
        // definitions of the same function with different signatures. We do not
        // check this error.

        if (bundle.getFuncNs().getByName(name) != null) {
            return;
        }

        FunctionSignature sig = deepFuncSigMaker.visit(ctx.funcSig());

        declareFunction(name, sig);
    }

    private void handleFuncDecl(FuncDeclContext ctx) {
        String name = ctx.GLOBAL_ID().getText();

        // It does not make sense to declare a function multiple times.

        if (bundle.getFuncNs().getByName(name) != null) {
            ParserHelper.parseError(ctx, "Function " + name
                    + " declared multiple times.");
        }

        FunctionSignature sig = deepFuncSigMaker.visit(ctx.funcSig());

        declareFunction(name, sig);
    }

}
