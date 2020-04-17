package carlclone.rpc.nameservice;


import carlclone.rpc.NameService;
import carlclone.rpc.serialize.SerializeSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class JdbcNameService implements NameService {
    private static final Logger logger = LoggerFactory.getLogger(LocalFileNameService.class);
    private static final Collection<String> schemes = Collections.singleton("jdbc");
    private static Connection connection;

    @Override
    public Collection<String> supportedSchemes() {
        return schemes;
    }

    @Override
    public void connect(URI nameServiceUri) {
        if(schemes.contains(nameServiceUri.getScheme())) {
            //建立连接
            try {
                connection=DriverManager.getConnection(nameServiceUri.toString());
            } catch (SQLException e) {
                logger.error("connect jdbc fail !");
            }
        } else {
            throw new RuntimeException("Unsupported scheme!");
        }
    }

    @Override
    public void registerService(String serviceName, URI uri) throws IOException {
        Metadata readData=getAllServices();
        Metadata updateData=new Metadata();
        Metadata insertData=new Metadata();

        if(!readData.containsKey(serviceName)){
            insertData.put(serviceName, Collections.singletonList(uri));
        }else {
            Set<URI> uris = new HashSet<>(readData.get(serviceName));
            uris.add(uri);
            updateData.put(serviceName,new ArrayList<>(uris));
        }

        if(!updateData.isEmpty()){
            try {
                String update="update nameservice set URI=? where service_name=?";
                PreparedStatement sql=connection.prepareStatement(update);
                List<URI> list = updateData.get(serviceName);
                sql.setString(1, list.stream().map(URI::toString).collect(Collectors.joining("|")));
                sql.setString(2,serviceName);
                sql.executeUpdate();
            } catch (SQLException e) {
                logger.error("update data fail !"+e.getMessage());
            }
        }

        if(!insertData.isEmpty()){
            try {
                String insert="insert into nameservice values(default,?,?)";
                PreparedStatement sql=connection.prepareStatement(insert);
                List<URI> list = insertData.get(serviceName);
                sql.setString(1,serviceName);
                sql.setString(2,list.stream().map(URI::toString).collect(Collectors.joining("|")));
                sql.executeUpdate();
            } catch (SQLException e) {
                logger.error("insert data fail !"+e.getMessage());
            }
        }
    }

    private static Metadata getAllServices(){
        Metadata metadata=new Metadata();
        try {
            String read="select * from nameservice";
            PreparedStatement sql=connection.prepareStatement(read);
            ResultSet resultSet = sql.executeQuery();
            while(resultSet.next()){
                List<URI> uris = Arrays.stream(resultSet.getString("URI").split("\\|")).map(JdbcNameService::getURIInstance).collect(Collectors.toList());
                metadata.put(resultSet.getString("service_name"),uris);
            }
        } catch (Exception e) {
            logger.error("read data fail !"+e.getMessage());
        }
        return metadata;
    }

    private static URI getURIInstance(String URI){
        try {
            return new URI(URI);
        } catch (URISyntaxException e) {
            logger.error("create URI fail !"+e.getMessage());
            return null;
        }
    }

    @Override
    public URI lookupService(String serviceName) throws IOException {
        Metadata metadata=getAllServices();

        List<URI> uris = metadata.get(serviceName);
        if(null == uris || uris.isEmpty()) {
            return null;
        } else {
            return uris.get(ThreadLocalRandom.current().nextInt(uris.size()));
        }
    }
}