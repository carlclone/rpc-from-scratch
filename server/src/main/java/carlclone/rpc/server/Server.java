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
package carlclone.rpc.server;

import carlclone.rpc.NameService;
import carlclone.rpc.RpcAccessPoint;
import carlclone.rpc.spi.ServiceSupport;
import carlclone.rpc.hello.HelloService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.File;
import java.net.URI;

/**
 * @author LiYue
 * Date: 2019/9/20
 */
public class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    public static void main(String [] args) throws Exception {


        //获取标准化的服务名   carlclone.rpc.hello.HelloService
        String serviceName = HelloService.class.getCanonicalName();

        //由于是基于本地文件的注册中心, 先生成临时文件
        File tmpDirFile = new File(System.getProperty("java.io.tmpdir"));
        File file = new File(tmpDirFile, "simple_rpc_name_service.data");

        //获取 HelloService 的实现实例
        HelloService helloService = new HelloServiceImpl();


        logger.info("创建并启动RpcAccessPoint...");

        try(
                //通过 ServiceSupport 门面,获取 RPCAP 实例,通过 SPI 依赖注入的  , 定义在resource的service里
                RpcAccessPoint rpcAccessPoint = ServiceSupport.load(RpcAccessPoint.class);
                //服务器启动 serverSocket 监听客户端请求,这里的底层用 Netty 实现 , NettyRpcAccessPoint
                Closeable ignored = rpcAccessPoint.startServer()
        ) {

            //这里是注册服务的内容, nameService 是基于文件的
            NameService nameService = rpcAccessPoint.getNameService(file.toURI()); //FileNameService , 传入文件的uri
            assert nameService != null;
            logger.info("向RpcAccessPoint注册{}服务...", serviceName);
            //向 RPCAP 添加对应服务实例
            URI uri = rpcAccessPoint.addServiceProvider(helloService, HelloService.class); // NettyRpcAccessPoint的addServiceProvider
            logger.info("服务名: {}, 向NameService注册...", serviceName);
            //告知 NameService 服务地址
            nameService.registerService(serviceName, uri); //RpcRequestHandler
            logger.info("开始提供服务，按任何键退出.");
            //noinspection ResultOfMethodCallIgnored
            System.in.read();
            logger.info("Bye!");
        }
    }

}
