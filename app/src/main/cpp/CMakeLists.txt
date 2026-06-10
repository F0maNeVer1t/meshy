cmake_minimum_required(VERSION 3.22.1)
project(fasttext_jni CXX)

set(CMAKE_CXX_STANDARD 17)

set(FASTTEXT_SRC_DIR ${CMAKE_CURRENT_SOURCE_DIR}/fasttext-src/src)

add_library(fasttext_jni SHARED
    fasttext_jni.cpp
    ${FASTTEXT_SRC_DIR}/args.cc
    ${FASTTEXT_SRC_DIR}/autotune.cc
    ${FASTTEXT_SRC_DIR}/densematrix.cc
    ${FASTTEXT_SRC_DIR}/dictionary.cc
    ${FASTTEXT_SRC_DIR}/fasttext.cc
    ${FASTTEXT_SRC_DIR}/loss.cc
    ${FASTTEXT_SRC_DIR}/matrix.cc
    ${FASTTEXT_SRC_DIR}/meter.cc
    ${FASTTEXT_SRC_DIR}/model.cc
    ${FASTTEXT_SRC_DIR}/productquantizer.cc
    ${FASTTEXT_SRC_DIR}/quantmatrix.cc
    ${FASTTEXT_SRC_DIR}/utils.cc
    ${FASTTEXT_SRC_DIR}/vector.cc
)

target_include_directories(fasttext_jni PRIVATE ${FASTTEXT_SRC_DIR})

target_link_options(fasttext_jni PRIVATE "-Wl,-z,max-page-size=16384")

find_library(log-lib log)
target_link_libraries(fasttext_jni ${log-lib})
