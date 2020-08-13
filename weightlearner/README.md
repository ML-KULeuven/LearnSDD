
# SDD WEIGHT LEARNER

This weightlearner intents to optimize the weights of an SDD such that the SDD becomes as similar to the data as possible. To this ends it uses L-BFGS optimization.

The library can be used through jni. This part is implemented in weightlearner_jni.c


## Compilation


1. Find the folders that contain jni.h and jni_md.h:
  
  ```
  JNI_DIR=$JAVA_HOME/include/
  JNI_MD_DIR=$JAVA_HOME/include/linux/
  ```

2. Compile the code and make a shared library:
  On linux:
  
  ```
  gcc -c -fpic -std=c99 lbfgs.c weightlearner.c weightlearner_jni.c -Iinclude -I${JNI_DIR} -I${JNI_MD_DIR}
  ld -shared lbfgs.o weightlearner.o weightlearner_jni.o -L../lib -lsdd  -o ../lib/libweightlearner.so
  ```
  
  On mac:
  
  ```
  gcc -c -fpic -std=c99 lbfgs.c weightlearner.c weightlearner_jni.c -Iinclude -I${JNI_DIR} -I${JNI_MD_DIR}
  g++ -dynamiclib *.o  -L../lib/  -lsdd -o ../lib/libweightlearner.dylib
  ```


## Testing

wltest.c is a small program that uses the weightlearner library. Compile and run it as follows:

```
gcc -std=c99 -o wltest wltest.c  -Iinclude -L../lib -lweightlearner -lsdd
./wltest
```

The expected output is:

```
weights 
1.841546 
-3.033633 
2.345154 
 
probabilities 
-0.104929 
-1.185350 
-1.200432 
```
