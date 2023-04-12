cmake_minimum_required(VERSION 3.23)
project(vm C)

set(CMAKE_C_STANDARD 99)

add_executable(vm src/main.c src/vm/mem.c src/vm/mem.h src/vm/types.h src/vm/cpu.c src/vm/cpu.h)
