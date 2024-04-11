import java.util.*;
import java.util.concurrent.*;

/**
 * 网络拓扑类
 *
 * @author wzy
 * @date 2024-03-04 18:50:36
 */
public class NetworkTopology {
    private static final String SEPARATOR1 = "-------------------------------".repeat(5) + "\n"; // 分割符
    private static final String SEPARATOR2 = "-------------------------------".repeat(3) + "\n"; // 分割符

    private List<Router> routers; // 路由器列表
    private List<Network> networks; // 网络列表
    private StringBuilder routingTablesInfo; // 所有路由表信息

    public NetworkTopology() {
        this.routers = new ArrayList<>();
        this.networks = new ArrayList<>();
        this.routingTablesInfo = new StringBuilder();
        initNetworkTopology();
    } // end NetworkTopology()

    /**
     * 初始化网络拓扑
     */
    public void initNetworkTopology() {
        Router routerA = new Router("A");
        Router routerB = new Router("B");
        Router routerC = new Router("C");
        Router routerD = new Router("D");
        Router routerE = new Router("E");
        Router routerF = new Router("F");

        Network network1 = new Network("网1");
        Network network2 = new Network("网2");
        Network network3 = new Network("网3");
        Network network4 = new Network("网4");
        Network network5 = new Network("网5");
        Network network6 = new Network("网6");

        routerA.addNeighbor(routerB);
        routerA.addNeighbor(routerD);
        routerA.addNeighbor(routerE);

        routerB.addNeighbor(routerA);
        routerB.addNeighbor(routerC);

        routerC.addNeighbor(routerB);
        routerC.addNeighbor(routerF);

        routerD.addNeighbor(routerA);
        routerD.addNeighbor(routerE);
        routerD.addNeighbor(routerF);

        routerE.addNeighbor(routerA);
        routerE.addNeighbor(routerD);
        routerE.addNeighbor(routerF);

        routerF.addNeighbor(routerC);
        routerF.addNeighbor(routerD);
        routerF.addNeighbor(routerE);

        routerA.addDirectlyConnectedNetwork(network1);
        routerA.addDirectlyConnectedNetwork(network2);
        routerA.addDirectlyConnectedNetwork(network3);

        routerB.addDirectlyConnectedNetwork(network3);
        routerB.addDirectlyConnectedNetwork(network4);

        routerC.addDirectlyConnectedNetwork(network4);
        routerC.addDirectlyConnectedNetwork(network5);

        routerD.addDirectlyConnectedNetwork(network2);
        routerD.addDirectlyConnectedNetwork(network6);

        routerE.addDirectlyConnectedNetwork(network1);
        routerE.addDirectlyConnectedNetwork(network6);

        routerF.addDirectlyConnectedNetwork(network5);
        routerF.addDirectlyConnectedNetwork(network6);

        joinRouter(routerA);
        joinRouter(routerB);
        joinRouter(routerC);
        joinRouter(routerD);
        joinRouter(routerE);
        joinRouter(routerF);

        joinNetwork(network1);
        joinNetwork(network2);
        joinNetwork(network3);
        joinNetwork(network4);
        joinNetwork(network5);
        joinNetwork(network6);
    } // end initNetworkTopology()

    /**
     * 路由器加入
     *
     * @param router 路由器
     */
    public void joinRouter(Router router) {
        routers.add(router);
    } // end joinRouter()

    /**
     * 路由器退出
     *
     * @param router 路由器
     */
    public void exitRouter(Router router) {
        routers.remove(router);

        for (Router router1 : routers) {
            router.removeNeighbor(router1);
            router1.removeNeighbor(router);
            router1.removeEntriesForRouter(router);
        }
    } // end exitRouter()

    /**
     * 网络加入
     *
     * @param network 网络
     */
    public void joinNetwork(Network network) {
        networks.add(network);
    } // end joinNetwork()

    /**
     * 网络退出
     *
     * @param network 网络
     */
    public void exitNetwork(Network network) {
        networks.remove(network);

        for (Router router : routers) {
            /* 直连同一网路的路由器移除相邻关系 */
            if (router.isDirectlyConnectedNetwork(network)) {
                for (Router router1 : routers) {
                    if (!router1.equals(router) && router1.isDirectlyConnectedNetwork(network)) {
                        router.removeNeighbor(router1);
                        router1.removeNeighbor(router);
                    }
                }
            }
            router.removeEntriesForNetwork(network);
        }
    } // end exitNetwork()

    /**
     * 查找路由器
     *
     * @param routerName 路由器名称
     * @return 查找结果
     */
    public Router findRouter(String routerName) {
        return routers.stream()
                .filter(router -> routerName.equals(router.getRouterName()))
                .findFirst()
                .orElse(null);
    } // end findRouter()

    /**
     * 查找网络
     *
     * @param networkName 网络名称
     * @return 查找结果
     */
    public Network findNetwork(String networkName) {
        return networks.stream()
                .filter(network -> networkName.equals(network.getNetworkName()))
                .findFirst()
                .orElse(null);
    } // end findNetwork()

    /**
     * 获取最新的路由表信息
     */
    public String getRoutingTablesInfo() {
        int index = 0;
        int size = routers.size();
        routingTablesInfo = new StringBuilder();

        routingTablesInfo.append(SEPARATOR1);

        for (Router router : routers) {
            routingTablesInfo.append(router.printRoutingTable());
            if (index != size - 1) {
                routingTablesInfo.append(SEPARATOR2);
            }
            index++;
        }

        return routingTablesInfo.toString();
    } // end getRoutingTablesInfo()

    public List<Router> getRouters() {
        return routers;
    }

    public void setRouters(List<Router> routers) {
        this.routers = routers;
    }

    public List<Network> getNetworks() {
        return networks;
    }

    public void setNetworks(List<Network> networks) {
        this.networks = networks;
    }
} // end class NetworkTopology
