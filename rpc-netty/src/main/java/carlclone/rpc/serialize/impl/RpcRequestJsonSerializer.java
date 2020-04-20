package carlclone.rpc.serialize.impl;

import carlclone.rpc.client.stubs.Argument;
import carlclone.rpc.client.stubs.RpcRequest;
import carlclone.rpc.serialize.Serializer;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
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
        String str = JSON.toJSONString(request);
        return str.getBytes(StandardCharsets.UTF_8).length;
    }

    @Override
    public void serialize(RpcRequest request, byte[] bytes, int offset, int length) {
        String str = JSON.toJSONString(request);
        byte[] strBytes = str.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(strBytes, 0, bytes, offset, strBytes.length);

    }

    @Override
    public RpcRequest parse(byte[] bytes, int offset, int length) {

//        byte[] objBytes = new byte[length];
//        System.arraycopy(bytes, offset, objBytes, 0, length);
//        String jsonStr = new String(objBytes, StandardCharsets.UTF_8);
//        return (RpcRequest) JSON.parse(jsonStr);
        JSONObject o = JSON.parseObject(bytes, offset, length, StandardCharsets.UTF_8, Object.class);
        JSONArray serializedArguments = o.getJSONArray("serializedArguments");
        Argument[] arguments=new Argument[serializedArguments.size()];
        for(int i=0;i<serializedArguments.size();i++){
            arguments[i]=new Argument();
            JSONObject o1 = (JSONObject) serializedArguments.get(i);
            try {
                Class<?> type = Class.forName(o1.getString("type"));
                arguments[i].setType(type);
                arguments[i].setValue(o1.getBytes("value"));
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        RpcRequest rpcRequest = new RpcRequest(o.getString("interfaceName"),o.getString("methodName"),arguments);
        return rpcRequest;
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
