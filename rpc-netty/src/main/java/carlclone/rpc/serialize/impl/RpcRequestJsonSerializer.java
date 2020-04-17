package carlclone.rpc.serialize.impl;

import carlclone.rpc.client.stubs.RpcRequest;
import carlclone.rpc.serialize.Serializer;
import com.alibaba.fastjson.JSONObject;

import java.util.Arrays;
import java.util.List;


public class RpcRequestJsonSerializer implements Serializer<RpcRequest> {

    @Override
    public int size(RpcRequest request) {
        return 0;
    }

    @Override
    public void serialize(RpcRequest entry, byte[] bytes, int offset, int length) {


        JSONObject object = new JSONObject();
        //string
        object.put("string","string");
        //int
        object.put("int",2);
        //boolean
        object.put("boolean",true);
        //array
        List<Integer> integers = Arrays.asList(1,2,3);
        object.put("list",integers);
        //null
//        object.put("null",null);​
//        System.out.println(object);

    }

    @Override
    public RpcRequest parse(byte[] bytes, int offset, int length) {
        return null;
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
