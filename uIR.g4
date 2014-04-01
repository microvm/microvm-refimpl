/*
 * defines uVM IR text form
 */

grammar uIR;

ir
    :   metaData*
    ;

metaData
    :   constDef
    |   funcSigDef
    |   funcDecl
    |   funcDef
    |   typeDef
    ;

constDef
    :   '.const' IDENTIFIER '<' type '>' '=' immediate
    ;

funcSigDef
    :   '.funcsig' IDENTIFIER funcSig
    ;

funcDecl
    :   '.funcdecl' IDENTIFIER '<' funcSig '>'
    ;
    
funcDef
    :   '.funcdef' IDENTIFIER '<' funcSig '>' funcBody
    ;

typeDef
    :   '.typedef' IDENTIFIER type
    ;

funcSig
    :   IDENTIFIER          # ReferencedFuncSig
    |   type '(' type* ')'  # InLineFuncSig
    ;

funcBody
    :   '{' funcBodyInst+ '}'
    ;

funcBodyInst
    :   constDef
    |   label
    |   inst
    ;

label
    :   '.label' IDENTIFIER ':'
    ;

type
    :   IDENTIFIER          # ReferencedType
    |   typeConstructor     # InLineType
    ;

immediate
    :   intImmediate
    |   fpImmediate
    ;

typeConstructor
    :   'int' '<' intImmediate '>'          # IntType
    |   'float'                             # FloatType
    |   'double'                            # DoubleType
    |   'ref' '<' type '>'                  # RefType
    |   'iref' '<' type '>'                 # IRefType
    |   'struct' '<' type+ '>'              # StructType
    |   'array' '<' type intImmediate '>'   # ArrayType
    |   'hybrid' '<' type type '>'          # HybridType
    |   'void'                              # VoidType
    |   'func' '<' funcSig '>'              # FuncType
    |   'thread'                            # ThreadType
    |   'stack'                             # StackType
    ;

inst
    :   IDENTIFIER '=' instBody             # NamedInstruction
    |   instBody                            # AnonymousInstruction
    ;

instBody
    :   'PARAM' intImmediate                    # InstParam

    // Integer/FP Arithmetic
    |   BINOPS '<' type '>' value value         # InstBinOp

    // Integer/FP Comparison
    |   CMPOPS '<' type '>' value value         # InstCmp
    
    // Select
    |   'SELECT' '<' type '>' value value value     # InstSelect

    // Conversions
    |   CONVOPS  '<' type type '>' value            # InstConversion

    // Intra-function Control Flow
    |   'BRANCH' IDENTIFIER                         # InstBranch
    |   'BRANCH2' value IDENTIFIER IDENTIFIER       # InstBranch2
    |   'SWITCH' '<' type '>' value IDENTIFIER '{'
            (value ':' IDENTIFIER ';')* '}'         # InstSwitch
    |   'PHI' '<' type '>' '{'
            (IDENTIFIER ':' value ';')* '}'         # InstPhi

    // Inter-function Control Flow
    |   'CALL' funcCallBody                         # InstCall
    |   'INVOKE' funcCallBody IDENTIFIER IDENTIFIER # InstInvoke
    |   'TAILCALL' funcCallBody                     # InstTailCall

    |   'RET' '<' type '>' value                    # InstRet
    |   'RETVOID'                                   # InstRetVoid
    |   'THROW' value                               # InstThrow
    |   'LANDINGPAD'                                # InstLandingPad

    // Aggregate Operations
    |   'EXTRACTVALUE' '<' type intImmediate '>' value  # InstExtractValue
    |   'INSERTVALUE' '<' type intImmediate '>' value value # InstInsertValue

    // Memory Operations
    |   'NEW'           '<' type '>'                # InstNew
    |   'NEWHYBRID'     '<' type '>' value          # InstNewHybrid
    |   'ALLOCA'        '<' type '>'                # InstAlloca
    |   'ALLOCAHYBRID'  '<' type '>' value          # InstAllocaHybrid
    
    |   'GETIREF'       '<' type '>' value              # InstGetIRef

    |   'GETFIELDIREF'  '<' type intImmediate '>' value # InstGetFieldIRef
    |   'GETELEMIREF'   '<' type '>' value value        # InstGetElemIRef
    |   'SHIFTIREF'     '<' type '>' value value        # InstShiftIRef
    |   'GETFIXEDPARTIREF'  '<' type '>' value          # InstGetFixedPartIRef
    |   'GETVARPARTIREF'    '<' type '>' value          # InstGetVarPartIRef
    
    |   'LOAD' ATOMICDECL? '<' type '>' value           # InstLoad
    |   'STORE' ATOMICDECL? '<' type '>' value value    # InstStore
    |   'CMPXCHG' ATOMICDECL? '<' type '>' value value value   # InstCmpXChg
    |   'ATOMICRMW' ATOMICDECL? ATOMICRMWOP
                '<' type '>' value value                # InstAtomicRMW

    // Thread and Stack Operations
    |   'NEWSTACK'  funcCallBody                        # InstNewStack
    |   'NEWTHREAD' value                               # InstNewThread
    |   'SWAPSTACK' value                               # InstSwapStack
    |   'KILLSTACK' value                               # InstKillStack
    |   'SWAPANDKILL' value                             # InstSwapAndKill
    |   'THREADEXIT'                                    # InstThreadExit

    // Trap
    |   'TRAP' args                                     # InstTrap
    |   'TRAPCALL' '<' type '>' args                    # InstTrapCall

    // Foreign Function Interface
    |   'CCALL' CALLCONV funcCallBody                   # InstCCall
    ;

funcCallBody
    :   '<' funcSig '>' value args
    ;

args
    :   '(' value* ')'
    ;

CALLCONV : 'DEFAULT' ;

BINOPS : IBINOPS | FBINOPS ;

IBINOPS
    : 'ADD'
    | 'SUB'
    | 'MUL'
    | 'UDIV'
    | 'SDIV'
    | 'UREM'
    | 'SREM'
    | 'SHL'
    | 'LSHR'
    | 'ASHR'
    | 'AND'
    | 'OR'
    | 'XOR'
    ;
    
FBINOPS
    : 'FADD' | 'FSUB' | 'FMUL' | 'FDIV' | 'FREM'
    ;

CMPOPS : ICMPOPS | FCMPOPS ;

ICMPOPS
    : 'EQ'
    | 'NE'
    | 'SGT'
    | 'SLT'
    | 'SGE'
    | 'SLE'
    | 'UGT'
    | 'ULT'
    | 'UGE'
    | 'ULE'
    ;

FCMPOPS
    : 'FTRUE' | 'FFALSE' 
    | 'FUNO' | 'FUEQ' | 'FUNE' | 'FUGT' | 'FULT' | 'FUGE' | 'FULE'
    | 'FORD' | 'FOEQ' | 'FONE' | 'FOGT' | 'FOLT' | 'FOGE' | 'FOLE'
    ;
    
CONVOPS
    : 'TRUNC' | 'ZEXT' | 'SEXT' | 'FPTRUNC' | 'FPEXT'
    | 'FPTOUI' | 'FPTOSI' | 'UITOFP' | 'SITOFP' | 'BITCAST'
    | 'REFCAST' | 'IREFCAST'
    ;

ATOMICDECL
    : 'NOT_ATOMIC' | 'UNORDERED' | 'MONOTONIC' | 'AQUIRE' | 'RELEASE'
    | 'ACQ_REL' | 'SQL_CST'
    ;

ATOMICRMWOP
    : 'XCHG' | 'ADD' | 'SUB' | 'AND' | 'NAND' | 'OR' | 'XOR'
    | 'MAX' | 'MIN' | 'UMAX' | 'UMIN'
    ;
value
    :   IDENTIFIER      # ReferencedValue
    |   immediate       # ImmediateValue
    ;

intImmediate
    :   ('+'|'-')? DIGITS
    ;

fpImmediate
    :   ('+'|'-')? DIGITS '.' DIGITS ('e' ('+'|'-')? DIGITS)?
    ;

// LEXER

DIGITS
    : DIGIT+
    ;

fragment
DIGIT
    :   [0-9]
    ;

IDENTIFIER
    :   GLOBAL_ID_PREFIX IDCHAR+
    |   LOCAL_ID_PREFIX IDCHAR+
    ;

GLOBAL_ID_PREFIX: '@';
LOCAL_ID_PREFIX: '%';

fragment
IDCHAR
    :   [a-z]
    |   [A-Z]
    |   [0-9]
    |   '-'
    |   '_'
    |   '.'
    ;

WS : [ \t\r\n]+ -> skip ; // skip spaces, tabs, newlines

LINE_COMMENT
    :   '//' ~[\r\n]* -> skip
    ;
