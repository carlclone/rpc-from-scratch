/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package carlclone.rpc.client;

import carlclone.rpc.transport.Transport;
import com.itranswarp.compiler.JavaStringCompiler;


import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author LiYue
 * Date: 2019/9/27
 */
public class DynamicStubFactory implements StubFactory{
    //类模板
    private final static String STUB_SOURCE_TEMPLATE =
            "package carlclone.rpc.client.stubs;\n" +
                    "import carlclone.rpc.serialize.SerializeSupport;\n" +
                    "\n" +
                    "public class %s extends AbstractStub implements %s {\n" +
                    "%s" +
                    "}";
    //方法模板
    private final static String STUB_SOURCE_TEMPLATE2 =
            "    @Override\n" +
                    "    public %s %s( %s ) {\n" +
                    "%s"+
                    "        return SerializeSupport.parse(\n" +
                    "                invokeRemote(\n" +
                    "                        new RpcRequest(\n" +
                    "                                \"%s\",\n" +
                    "                                \"%s\",\n" +
                    "                                arguments\n" +
                    "                        )\n" +
                    "                )\n" +
                    "        );\n" +
                    "    }\n";

    @Override
    @SuppressWarnings("unchecked")
    public <T> T createStub(Transport transport, Class<T> serviceClass) {
        try {
            // 填充模板
            String stubSimpleName = serviceClass.getSimpleName() + "Stub";
            String classFullName = serviceClass.getName();
            String stubFullName = "carlclone.rpc.client.stubs." + stubSimpleName;
            StringBuilder methodSources=new StringBuilder();

            Method[] methods = serviceClass.getMethods();
            for(Method method:methods){
                String returnType = method.getReturnType().getTypeName();
                String methodName = method.getName();
                StringBuilder parameters=new StringBuilder();
                StringBuilder fourthPlace=new StringBuilder();

                int count=0;
                Class<?>[] parameterTypes = method.getParameterTypes();
                fourthPlace.append("Argument[] arguments=new Argument[").append(parameterTypes.length).append("];\n");
                for(Class<?> parameter:parameterTypes){
                    String name = parameter.getName();
                    parameters.append(name).append(" arg").append(count).append(",");

                    fourthPlace.append("arguments[").append(count).append("]=new Argument();\n");
                    fourthPlace.append("arguments[").append(count).append("].setType(").append(name).append(".class);\n");
                    fourthPlace.append("arguments[").append(count).append("].setValue(SerializeSupport.serialize(arg")
                            .append(count).append("));\n");
                    count++;
                }
                parameters.deleteCharAt(parameters.length()-1);


                String methodSource=String.format(STUB_SOURCE_TEMPLATE2,returnType,
                        methodName,parameters,fourthPlace,classFullName,methodName);
                methodSources.append(methodSource);
            }

            String source = String.format(STUB_SOURCE_TEMPLATE, stubSimpleName, classFullName,methodSources);
            // 编译源代码
            JavaStringCompiler compiler = new JavaStringCompiler();
            Map<String, byte[]> results = compiler.compile(stubSimpleName + ".java", source);
            // 加载编译好的类
            Class<?> clazz = compiler.loadClass(stubFullName, results);

            // 把Transport赋值给桩
            ServiceStub stubInstance = (ServiceStub) clazz.newInstance();
            stubInstance.setTransport(transport);
            // 返回这个桩
            return (T) stubInstance;
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}