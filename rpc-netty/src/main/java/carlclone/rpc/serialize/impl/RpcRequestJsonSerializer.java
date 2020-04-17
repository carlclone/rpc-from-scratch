package carlclone.rpc.serialize.impl;

import carlclone.rpc.client.stubs.RpcRequest;
import carlclone.rpc.serialize.Serializer;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

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
        byte[] bytes = jstr.getBytes(StandardCharsets.UTF_8);
        return bytes.length;
    }

    @Override
    public void serialize(RpcRequest request, byte[] bytes, int offset, int length) {
        JSONObject object = new JSONObject();
        //string
        object.put("interfaceName",request.getInterfaceName());
        //int
        object.put("methodName",request.getMethodName());
        //boolean
        object.put("serializeArgs",request.getSerializedArguments());

        String jstr = object.toJSONString();
        bytes = jstr.getBytes(StandardCharsets.UTF_8);

        //array
//        List<Integer> integers = Arrays.asList(1,2,3);
//        object.put("list",integers);
        //null
//        object.put("null",null);​
//        System.out.println(object);

    }

    @Override
    public RpcRequest parse(byte[] bytes, int offset, int length) {
        String jstr = new String(bytes);
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
