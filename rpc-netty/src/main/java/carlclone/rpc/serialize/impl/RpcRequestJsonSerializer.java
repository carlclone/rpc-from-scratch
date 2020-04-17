package carlclone.rpc.serialize.impl;

import carlclone.rpc.client.stubs.RpcRequest;
import carlclone.rpc.serialize.Serializer;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;


public class RpcRequestJsonSerializer implements Serializer<RpcRequest> {
    @Override
    public int size(RpcRequest request) {
        JSONObject object = new JSONObject();
        //string
        object.put("interfaceName",request.getInterfaceName());
        //int
        object.put("methodName",request.getMethodName());
        //boolean
        object.put("serializeArgs",request.getSerializedArguments());

        String jstr = object.toJSONString();

        return jstr.getBytes().length;

    }

    @Override
    public void serialize(RpcRequest request, byte[] bytes, int offset, int length) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes, offset, length);
        JSONObject object = new JSONObject();
        //string
        object.put("interfaceName",request.getInterfaceName());
        //int
        object.put("methodName",request.getMethodName());
        //boolean
        object.put("serializeArgs",new String(request.getSerializedArguments()));

        String jstr = object.toJSONString();
        bytes = jstr.getBytes();
        buffer.put(bytes);

        //array
//        List<Integer> integers = Arrays.asList(1,2,3);
//        object.put("list",integers);
        //null
//        object.put("null",null);​
//        System.out.println(object);

    }

    @Override
    public RpcRequest parse(byte[] bytes, int offset, int length) {

//        byte[] objBytes = new byte[length];
//        System.arraycopy(bytes, offset, objBytes, 0, length);
//        String jsonStr = new String(objBytes, StandardCharsets.UTF_8);
//        JSONObject object = JSON.parseObject(jsonStr);
//        return new RpcRequest(object.getString("interfaceName"),object.getString("methodName"),object.getString("serializeArgs").getBytes());
//
        String jstr = new String(bytes);
        jstr = jstr.substring(1);
        JSONObject object = JSON.parseObject(jstr);
        return new RpcRequest(object.getString("interfaceName"),object.getString("methodName"),object.getString("serializeArgs").getBytes());
    }

    @Override
    public byte type() {
        return Types.TYPE_RPC_REQUEST;
    }

    @Override
    public Class<RpcRequest> getSerializeClass() {
        return RpcRequest.class;
    }
}
