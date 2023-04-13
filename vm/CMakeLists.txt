cmake_minimum_required(VERSION 3.23)
project(vm C)

set(CMAKE_C_STANDARD 99)

add_executable(vm src/main.c src/vm/memory/mem.c src/vm/memory/mem.h src/vm/types.h src/vm/cpu/cpu.c src/vm/cpu/cpu.h src/vm/cpu/stack.c src/vm/cpu/opcodes/opcodes.h src/vm/cpu/opcodes/opcodes.c src/vm/cpu/opcodes/opcodes_impls_movs.c src/vm/cpu/opcodes/opcodes_impls_math.c src/vm/cpu/opcodes/opcodes_impls_stack.c src/vm/cpu/opcodes/opcodes_impls_jmps.c src/vm/cpu/opcodes/opcodes_impls_special.c src/vm/cpu/opcodes/opcodes_impls_tests.c src/vm/debug.h src/vm/memory/memmap.c src/vm/memory/memmap.h)
