MicroVM Reference Implementation
================================

This is a reference implementation of a MicroVM.

MicroVM, a.k.a. µVM, or uvm, is a low-level virtual machine designed to support
high-level programming languages. It is designed to provide concurrency, JIT
compiling and garbage collection as its core functions while keeping the VM
itself minimal. The purpose of this reference implementation, however, is to
demonstrate its interface to its client, which is the program that compiles
high-level language programs to the MicroVM intermediate representation.

An implementation-neutral MicroVM interface specification is available at
https://github.com/microvm-project/microvm-spec/wiki

Implementation Details
----------------------

Given the purpose of demonstrating the interface, this MicroVM is not
implemented efficiently and realistically as a productional virtual machine. It
produces the desired computation results expected from a productional VM, but
there are some limitations:

- This implementation is written in the Java programming language and runs on
  the Hotspot JVM.
- It assumes 64-bit x86-64 architecture.
- This implementation uses green threads. MicroVM threads and MicroVM stacks can
  be created in the desired way as the interface specifies, but only one thread
  is running at any moment.
- The execution is done by interpreting rather than JIT compiling.
- It has a single-threaded stop-the-world garbage collector. It has a
  simplistic Immix-like mark-region GC for small objects and a mark-sweep GC for
  large objects.

Building and Running
--------------------

This reference implementation is written in the Java programming language. It is
recommended to use the Eclipse IDE.

This project depends on the `sun.misc.Unsafe` class for raw memory access. You
should use the Hotspot JVM.

It uses the Antlr parser generator for the MicroVM Intermediate Representation
parser. Antlr 4.2 is bundled in this project. To generate the parser, you should
install Ant and invoke:

    ant build-uir-parser

Then the project should compile. The source code is in the `src` directory and
the generated parser is in the `parser-generated` directory. 

Currently it still needs to know some implementation details about the MicroVM
to use it. There are many test cases in the `test` directory, especially the
`uvm.refimpl.TestMicroVMRefImpl` test suite. Please have a look at the source
code.

Authors
-------

This project is created by Kunshan Wang, Yi Lin, Steve Blackburn, Antony
Hosking, Michael Norrish. See the `LICENSE` file for the licence.

Contact
-------

Kunshan Wang <kunshan.wang@anu.edu.au>

<!--
vim: tw=80
-->
