package cn.haitaoss;

import com.alipay.sofa.jraft.JRaftUtils;
import com.alipay.sofa.jraft.RouteTable;
import com.alipay.sofa.jraft.conf.Configuration;
import com.alipay.sofa.jraft.entity.PeerId;
import com.alipay.sofa.jraft.option.CliOptions;
import com.alipay.sofa.jraft.rpc.InvokeCallback;
import com.alipay.sofa.jraft.rpc.impl.cli.CliClientServiceImpl;

/**
 * @author haitao.chen
 * email haitaoss@aliyun.com
 * date 2023-06-15 10:23
 *
 */
public class JRaftClient {
    
    public static void main(String[] args) throws Exception {
        // 命令行参数 jraft 127.0.0.1:8081,127.0.0.1:8082,127.0.0.1:8083
        final String groupId = args[0];
        final String confStr = args[1];
        
        // 更新路由表配置
        final Configuration conf = JRaftUtils.getConfiguration(confStr);
        boolean b = RouteTable.getInstance().updateConfiguration(groupId, conf);
        System.out.println("b = " + b);
        
        final CliClientServiceImpl cliClientService = new CliClientServiceImpl();
        cliClientService.init(new CliOptions());
        
        if (!RouteTable.getInstance().refreshLeader(cliClientService, groupId, 1000).isOk()) {
            throw new IllegalStateException("Refresh leader failed");
        }
        
        final PeerId leader = RouteTable.getInstance().selectLeader(groupId);
        System.out.println("Leader is " + leader);
        
        cliClientService.getRpcClient().invokeAsync(leader.getEndpoint(), null, new InvokeCallback() {
            @Override
            public void complete(Object result, Throwable err) {
                System.out.println("result = " + result);
            }
        }, 5000);
        
    }
    
}
