/*
 * Copyright (c) 2009-2021 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.jme3.renderer.opengl;

import com.jme3.util.IntMap;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.ShortBuffer;
import java.util.HashMap;

/**
 * Utility class that allows tracing of OpenGL calls generated by the engine.
 * 
 * @author Kirill Vainer
 */
public final class GLTracer implements InvocationHandler {
    
    private final Object obj;
    private final IntMap<String> constMap;
    private static final HashMap<String, IntMap<Void>> nonEnumArgMap = new HashMap<String, IntMap<Void>>();
    
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_BRIGHT = "\u001B[1m";
    private static final String ANSI_BLACK = "\u001B[30m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_MAGENTA = "\u001B[35m";
    private static final String ANSI_CYAN = "\u001B[36m";
    private static final String ANSI_WHITE = "\u001B[37m";

    private static void noEnumArgs(String method, int... argSlots) {
        IntMap<Void> argSlotsMap = new IntMap<>();
        for (int argSlot : argSlots) {
            argSlotsMap.put(argSlot, null);
        }
        nonEnumArgMap.put(method, argSlotsMap);
    }
    
    static {
        noEnumArgs("glViewport", 0, 1, 2, 3);
        noEnumArgs("glScissor", 0, 1, 2, 3);
        noEnumArgs("glClear", 0);
        noEnumArgs("glGetInteger", 1);
        noEnumArgs("glGetString", 1);
        
        noEnumArgs("glBindTexture", 1);
        noEnumArgs("glPixelStorei", 1);
//        noEnumArgs("glTexParameteri", 2);
        noEnumArgs("glTexImage2D", 1, 3, 4, 5);
        noEnumArgs("glTexImage3D", 1, 3, 4, 5, 6);
        noEnumArgs("glTexSubImage2D", 1, 2, 3, 4, 5);
        noEnumArgs("glTexSubImage3D", 1, 2, 3, 4, 5, 6, 7);
        noEnumArgs("glCompressedTexImage2D", 1, 3, 4, 5);
        noEnumArgs("glCompressedTexSubImage3D", 1, 2, 3, 4, 5, 6, 7);
        noEnumArgs("glDeleteTextures", 0);
        noEnumArgs("glReadPixels", 0, 1, 2, 3);
        
        noEnumArgs("glBindBuffer", 1);
        noEnumArgs("glEnableVertexAttribArray", 0);
        noEnumArgs("glDisableVertexAttribArray", 0);
        noEnumArgs("glVertexAttribPointer", 0, 1, 4, 5);
        noEnumArgs("glVertexAttribDivisorARB", 0, 1);
        noEnumArgs("glDrawRangeElements", 1, 2, 3, 5);
        noEnumArgs("glDrawArrays", 1, 2);
        noEnumArgs("glDeleteBuffers", 0);
        noEnumArgs("glBindVertexArray", 0);
        noEnumArgs("glGenVertexArrays", 0);
        
        noEnumArgs("glBindFramebufferEXT", 1);
        noEnumArgs("glBindRenderbufferEXT", 1);
        noEnumArgs("glRenderbufferStorageEXT", 2, 3);
        noEnumArgs("glRenderbufferStorageMultisampleEXT", 1, 3, 4);
        noEnumArgs("glFramebufferRenderbufferEXT", 3);
        noEnumArgs("glFramebufferTexture2DEXT", 3, 4);
        noEnumArgs("glFramebufferTextureLayerEXT", 2, 3, 4);
        noEnumArgs("glBlitFramebufferEXT", 0, 1, 2, 3, 4, 5, 6, 7, 8);
        
        noEnumArgs("glCreateProgram", -1);
        noEnumArgs("glCreateShader", -1);
        noEnumArgs("glShaderSource", 0);
        noEnumArgs("glCompileShader", 0);
        noEnumArgs("glGetShader", 0);
        noEnumArgs("glAttachShader", 0, 1);
        noEnumArgs("glLinkProgram", 0);
        noEnumArgs("glGetProgram", 0);
        noEnumArgs("glUseProgram", 0);
        noEnumArgs("glGetUniformLocation", 0, -1);
        noEnumArgs("glUniformMatrix3", 0);
        noEnumArgs("glUniformMatrix4", 0);
        noEnumArgs("glUniform1i", 0, 1);
        noEnumArgs("glUniform1f", 0);
        noEnumArgs("glUniform2f", 0);
        noEnumArgs("glUniform3f", 0);
        noEnumArgs("glUniform4", 0);
        noEnumArgs("glUniform4f", 0);
        noEnumArgs("glGetAttribLocation", 0, -1);
        noEnumArgs("glDetachShader", 0, 1);
        noEnumArgs("glDeleteShader", 0);
        noEnumArgs("glDeleteProgram", 0);
        noEnumArgs("glBindFragDataLocation", 0, 1);
    }
    
    public GLTracer(Object obj, IntMap<String> constMap) {
        this.obj = obj;
        this.constMap = constMap;
    }
    
    private static IntMap<String> generateConstantMap(Class<?> ... classes) {
        IntMap<String> constMap = new IntMap<>();
        for (Class<?> clazz : classes) {
            for (Field field : clazz.getFields()) {
                if (field.getType() == int.class) {
                    try {
                        int val = field.getInt(null);
                        String name = field.getName();
                        constMap.put(val, name);
                    } catch (IllegalArgumentException ex) {
                    } catch (IllegalAccessException ex) {
                    }
                }
            }
        }
        // GL_ONE is more common than GL_TRUE (which is a boolean anyway..)
        constMap.put(1, "GL_ONE");
        return constMap;
    }
    
    /**
     * Creates a tracer implementation that wraps OpenGL ES 2.
     *
     * @param glInterface OGL object to wrap
     * @param glInterfaceClasses The interface(s) to implement
     * @return A tracer that implements the given interface
     */
    public static Object createGlesTracer(Object glInterface, Class<?>... glInterfaceClasses) {
        IntMap<String> constMap = generateConstantMap(GL.class, GL2.class, GL3.class, GLFbo.class, GLExt.class);
        return Proxy.newProxyInstance(
                glInterface.getClass().getClassLoader(),
                glInterfaceClasses,
                new GLTracer(glInterface, constMap));
    }

    /**
     * Creates a tracer implementation that wraps OpenGL 2+.
     * 
     * @param glInterface OGL object to wrap
     * @param glInterfaceClasses The interface(s) to implement
     * @return A tracer that implements the given interface
     */
    public static Object createDesktopGlTracer(Object glInterface, Class<?> ... glInterfaceClasses) {
        IntMap<String> constMap = generateConstantMap(GL2.class, GL3.class, GL4.class, GLFbo.class, GLExt.class);
        return Proxy.newProxyInstance(glInterface.getClass().getClassLoader(),
                                      glInterfaceClasses, 
                                      new GLTracer(glInterface, constMap));
    }
    
    private void printStyle(String style, String string) {
        System.out.print(style + string + ANSI_RESET);
    }
    
    private void print(String string) {
        System.out.print(string);
    }
    
    private void printInt(int value) {
        print(Integer.toString(value));
    }
    
    private void printEnum(int value) {
        String enumName = constMap.get(value);
        if (enumName != null) {
            if (enumName.startsWith("GL_")) {
                enumName = enumName.substring(3);
            }
            if (enumName.endsWith("_EXT") || enumName.endsWith("_ARB")) {
                enumName = enumName.substring(0, enumName.length() - 4);
            }
            printStyle(ANSI_GREEN, enumName);
        } else {
            printStyle(ANSI_GREEN, "ENUM_" + Integer.toHexString(value));
        }
    }
    
    private void printIntOrEnum(String method, int value, int argIndex) {
        IntMap<Void> argSlotMap = nonEnumArgMap.get(method);
        if (argSlotMap != null && argSlotMap.containsKey(argIndex)) {
            printInt(value);
        } else {
            printEnum(value);
        }
    }
    
    private void printNewLine() {
        System.out.println();
    }
    
    private void printString(String value) {
        if (value.length() > 150) {
            value = value.substring(0, 150) + "...";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(ANSI_YELLOW);
        sb.append("\"");
        sb.append(ANSI_RESET);
        for (String line : value.split("\n")) {
            sb.append(ANSI_YELLOW);
            sb.append(line.replaceAll("\0", "\\\\0"));
            sb.append(ANSI_RESET);
            sb.append("\n");
        }
        if (sb.length() > 1 && sb.charAt(sb.length() - 1) == '\n') {
            sb.setLength(sb.length() - 1);
        }
        sb.append(ANSI_YELLOW);
        sb.append("\"");
        sb.append(ANSI_RESET);
        print(sb.toString());
    }
    
    private void printBoolean(boolean bool) {
        printStyle(ANSI_BLUE, bool ? "true" : "false");
    }
    
    private void printBuffer(Buffer buffer) {
        StringBuilder sb = new StringBuilder();
        sb.append(ANSI_MAGENTA);
        if (buffer instanceof ByteBuffer) {
            sb.append("byte");
        } else if (buffer instanceof ShortBuffer) {
            sb.append("short");
        } else if (buffer instanceof CharBuffer) { 
            sb.append("char");
        } else if (buffer instanceof FloatBuffer) {
            sb.append("float");
        } else if (buffer instanceof IntBuffer) {
            sb.append("int");
        } else if (buffer instanceof LongBuffer) {
            sb.append("long");
        } else if (buffer instanceof DoubleBuffer) {
            sb.append("double");
        } else {
            throw new UnsupportedOperationException();
        }
        sb.append(ANSI_RESET);
        sb.append("[");
        
        if (buffer.position() == 0
                && buffer.limit() == buffer.capacity()) {
            // Common case. Just print buffer size.
            sb.append(buffer.capacity());
        } else {
            sb.append("pos=").append(buffer.position());
            sb.append(" lim=").append(buffer.limit());
            sb.append(" cap=").append(buffer.capacity());
        }
        
        sb.append("]");
        print(sb.toString());
    }
    
    private void printMethodName(String methodName) {
        if (methodName.startsWith("gl")) {
            // GL calls which actually draw (as opposed to change state)
            // will be printed in darker color
            methodName = methodName.substring(2);
            if (methodName.equals("Clear")
                    || methodName.equals("DrawRangeElements")
                    || methodName.equals("DrawElementsInstancedARB")) {
                print(methodName);
            } else {
                if (methodName.endsWith("EXT")) {
                    methodName = methodName.substring(0, methodName.length() - 3);
                }
                printStyle(ANSI_BRIGHT, methodName);
            }
        } else if (methodName.equals("resetStats")) {
            printStyle(ANSI_RED, "-- frame boundary --");
        }
    }
    
    private void printArgsClear(int mask) {
        boolean needAPipe = false;
        print("(");
        if ((mask & GL.GL_COLOR_BUFFER_BIT) != 0) {
            printStyle(ANSI_GREEN, "COLOR_BUFFER_BIT");
            needAPipe = true;
        }
        if ((mask & GL.GL_DEPTH_BUFFER_BIT) != 0) {
            if (needAPipe) {
                print(" | ");
            }
            printStyle(ANSI_GREEN, "DEPTH_BUFFER_BIT");
        }
        if ((mask & GL.GL_STENCIL_BUFFER_BIT) != 0) {
            if (needAPipe) {
                print(" | ");
            }
            printStyle(ANSI_GREEN, "STENCIL_BUFFER_BIT");
        }
        print(")");
    }
    
    private void printArgsGetInteger(Object[] args) {
        print("(");
        int param = (Integer)args[0];
        IntBuffer ib = (IntBuffer) args[1];
        printEnum(param);
        print(", ");
        printOut();
        if (param == GL2.GL_DRAW_BUFFER || param == GL2.GL_READ_BUFFER) {
            printEnum(ib.get(0));
        } else {
            printInt(ib.get(0));
        }
        print(")");
    }
    
    private void printArgsTexParameter(Object[] args) {
        print("(");

        int target = (Integer) args[0];
        int param = (Integer) args[1];
        int value = (Integer) args[2];

        printEnum(target);
        print(", ");
        printEnum(param);
        print(", ");
        
        if (param == GL2.GL_TEXTURE_BASE_LEVEL
                || param == GL2.GL_TEXTURE_MAX_LEVEL) {
            printInt(value);
        } else {
            printEnum(value);
        }
        
        print(")");
    }
    
    private void printOut() {
        printStyle(ANSI_CYAN, "out=");
    }
    
    private void printResult(String methodName, Object result, Class<?> returnType) {
        if (returnType != void.class) {
            print(" = ");
            if (result instanceof String) {
                printString((String) result);
            } else if (returnType == int.class) {
                int val = (Integer) result;
                printIntOrEnum(methodName, val, -1);
            } else if (returnType == boolean.class) {
                printBoolean((Boolean)result);
            } else {
                print(" = ???");
            }
        }
    }
    
    private void printNull() {
        printStyle(ANSI_BLUE, "null");
    }
    
    private void printArgs(String methodName, Object[] args, Class<?>[] paramTypes) {
        if (methodName.equals("glClear")) {
            printArgsClear((Integer)args[0]);
            return;
        } else if (methodName.equals("glTexParameteri")) {
            printArgsTexParameter(args);
            return;
        } else if (methodName.equals("glGetInteger")) {
            printArgsGetInteger(args);
            return;
        }
        
        if (args == null) {
            print("()");
            return;
        }
        
        print("(");
        for (int i = 0; i < args.length; i++) {
            if (paramTypes[i] == int.class) {
                int val = (Integer)args[i];
                printIntOrEnum(methodName, val, i);
            } else if (paramTypes[i] == boolean.class) {
                printBoolean((Boolean)args[i]);
            } else if (paramTypes[i] == String.class) {
                printString((String)args[i]);
            } else if (paramTypes[i] == String[].class) {
                String[] arr = (String[]) args[i];
                if (arr.length == 1) {
                    printString(arr[0]);
                } else {
                    print("string[" + arr.length + "]");
                }
            } else if (args[i] instanceof IntBuffer) {
                IntBuffer buf = (IntBuffer) args[i];
                if (buf.capacity() == 16) {
                    int val = buf.get(0);
                    printOut();
                    printIntOrEnum(methodName, val, i);
                } else if (buf.capacity() == 1) {
                    printOut();
                    print(Integer.toString(buf.get(0)));
                } else {
                    printBuffer(buf);
                }
            } else if (args[i] instanceof ByteBuffer) {
                ByteBuffer bb = (ByteBuffer)args[i];
                if (bb.capacity() == 250) {
                    printOut();
                    printBoolean(bb.get(0) != 0);
                } else {
                    printBuffer(bb);
                }
            } else if (args[i] instanceof Buffer) {
                printBuffer((Buffer)args[i]);
            } else if (args[i] != null) {
                print(args[i].toString());
            } else {
                printNull();
            }

            if (i != args.length - 1) {
                System.out.print(", ");
            }
        }
        print(")");
    }
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        printMethodName(methodName);
        
        if (methodName.startsWith("gl")) {
            try {
                // Try to evaluate result first, so we can see output values.
                Object result = method.invoke(obj, args);
                printArgs(methodName, args, method.getParameterTypes());
                printResult(methodName, result, method.getReturnType());
                printNewLine();
                return result;
            } catch (Throwable ex) {
                // Execution failed, print args anyway
                // but output values will be incorrect.
                printArgs(methodName, args, method.getParameterTypes());
                printNewLine();
                System.out.println("\tException occurred!");
                System.out.println(ex.toString());
                throw ex;
            }
        } else {
            printNewLine();
            return method.invoke(obj, args);
        }
    }
}
