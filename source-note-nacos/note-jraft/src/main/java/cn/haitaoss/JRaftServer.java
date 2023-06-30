package cn.haitaoss;

import com.alipay.sofa.jraft.JRaftUtils;
import com.alipay.sofa.jraft.Node;
import com.alipay.sofa.jraft.RaftGroupService;
import com.alipay.sofa.jraft.entity.PeerId;
import com.alipay.sofa.jraft.option.NodeOptions;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * @author haitao.chen
 * email haitaoss@aliyun.com
 * date 2023-06-14 15:02
 *
 */
public class JRaftServer {
    
    public static void main(String[] args) throws Exception {
        
        // 命令行参数 D:\workspace\IDEA_Project\nacos jraft 127.0.0.1:8081 127.0.0.1:8081,127.0.0.1:8082,127.0.0.1:8083
        String dataPath = args[0];
        String groupId = args[1];
        String serverIdStr = args[2];
        String initConfStr = args[3];
        
        // 这样就为了本节点提供了 RPC Server 服务，其他节点可以连接本节点进行通讯，比如发起选举、心跳和复制等。
        PeerId serverId = JRaftUtils.getPeerId(serverIdStr);
        NodeOptions nodeOptions = new NodeOptions(); // 配置 node options
        
        // System.getProperty("user.dir");
        dataPath = dataPath + File.separator + groupId + File.separator + serverId.getPort() + File.separator;
        FileUtils.forceMkdir(new File(dataPath));
        // 日志, 必须
        nodeOptions.setLogUri(dataPath + "log");
        // 元信息, 必须
        nodeOptions.setRaftMetaUri(dataPath + "raft_meta");
        // snapshot, 可选, 一般都推荐
        nodeOptions.setSnapshotUri(dataPath + "snapshot");
        // 设置初始集群配置
        nodeOptions.setInitialConf(JRaftUtils.getConfiguration(initConfStr));
        RaftGroupService raftGroupService = new RaftGroupService(groupId, serverId, nodeOptions);
        Node node = raftGroupService.start();
        
        while (true) {
            System.out.printf("status: %s , leaderId is %s\n", node.getNodeState(), node.getLeaderId());
            TimeUnit.SECONDS.sleep(3);
        }
    }
    
}
