package uvm.type;

public interface TypeVisitor<T> {
    T visitInt(Int type);
    T visitFloat(Float type);
    T visitDouble(Double type);
    T visitRef(Ref type);
    T visitIRef(IRef type);
    T visitWeakRef(WeakRef type);
    T visitStruct(Struct type);
    T visitArray(Array type);
    T visitHybrid(Hybrid type);
    T visitVoid(Void type);
    T visitFunc(Func type);
    T visitThread(Thread type);
    T visitStack(Stack type);
    T visitTagRef64(TagRef64 type);
}
