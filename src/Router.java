import java.util.*;
import java.util.concurrent.*;

/**
 * 路由器类
 *
 * @author wzy
 * @date 2024-03-04 18:50:54
 */
public class Router {
    private String routerName; // 路由器名称
    private Map<Network, RouteTableEntry> routingTable; // 路由表
    private List<Router> neighbors; // 相邻路由器列表
    private ScheduledExecutorService scheduler; // 定时器
    private StringBuilder routingTableInfo; // 路由表信息

    public Router(String routerName) {
        this.routerName = routerName;
        this.routingTable = new HashMap<>();
        this.neighbors = new ArrayList<>();
        this.scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::sendUpdates, 0, 2, TimeUnit.SECONDS);
    } // end Router()

    /**
     * 添加相邻路由器
     *
     * @param neighbor 相邻路由器
     */
    public void addNeighbor(Router neighbor) {
        neighbors.add(neighbor);
    } // end addNeighbor()

    /**
     * 移除相邻路由器
     *
     * @param neighbor 相邻路由器
     */
    public void removeNeighbor(Router neighbor) {
        neighbors.remove(neighbor);
    } // end removeNeighbor()

    /**
     * 添加直连网络（直连网络的跳数为1）
     *
     * @param network 直连网络
     */
    public void addDirectlyConnectedNetwork(Network network) {
        /* 使用 Iterator 来避免在遍历时修改集合，避免 ConcurrentModificationException 异常 */
        Iterator<Map.Entry<Network, RouteTableEntry>> it = routingTable.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Network, RouteTableEntry> entry = it.next();
            if (entry.getKey().getNetworkName().equals(network.getNetworkName())) {
                it.remove();
            }
        }
        RouteTableEntry entry = new RouteTableEntry(network, 1, this);
        routingTable.put(network, entry);
    } // end addDirectlyConnectedNetwork()

    /**
     * 判断路由器是否直连网路
     *
     * @param network 网路
     * @return 判断结果
     */
    public boolean isDirectlyConnectedNetwork(Network network) {
        /* 使用 Iterator 来避免在遍历时修改集合，避免 ConcurrentModificationException 异常 */
        Iterator<Map.Entry<Network, RouteTableEntry>> it = routingTable.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Network, RouteTableEntry> entry = it.next();
            if (entry.getKey().equals(network)
                    && (entry.getValue().getHops() == 1 || entry.getValue().getHops() == 16)
                    && entry.getValue().getNextHop().equals(this)) {
                return true;
            }
        }

        return false;
    } // end isDirectlyConnectedNetwork()

    /**
     * 定时向所有相邻路由器发送路由更新
     */
    public void sendUpdates() {
        for (Router neighbor : neighbors) {
            neighbor.receiveUpdate(this, new HashMap<>(this.routingTable));
        }
    } // end sendUpdates()

    /**
     * 接收来自相邻路由器的路由更新
     *
     * @param sender               相邻路由器
     * @param receivedRoutingTable 路由更新表
     */
    public void receiveUpdate(Router sender, Map<Network, RouteTableEntry> receivedRoutingTable) {
        for (Map.Entry<Network, RouteTableEntry> entry : receivedRoutingTable.entrySet()) {
            Network destination = entry.getKey(); // 目的网络
            RouteTableEntry receivedEntry = entry.getValue(); // 路由表项
            int newHops = receivedEntry.getHops() + 1; // 计算新的跳数
            newHops = Math.min(newHops, 16); // 如果收到的跳数已经是16，或者加1后变为16，则直接使用16作为跳数

            RouteTableEntry currentEntry = routingTable.get(destination); // 原来的路由表项

            if (currentEntry == null) {
                // 原来的路由表中没有目的网络 destination，则把该项目添加到原来的路由表中
                routingTable.put(destination, new RouteTableEntry(destination, newHops, sender));
            } else { // 在原来的路由表中有目的网络 destination
                if (currentEntry.getNextHop().equals(sender)) {
                    // 若下一跳路由器是 sender，则把收到的项目替换原路由表中的项目
                    routingTable.put(destination, new RouteTableEntry(destination, newHops, sender));
                } else if (newHops < currentEntry.getHops()) { // 若下一跳不是 sender
                    // 若收到的项目中的跳数<原来的路由表中的跳数，则进行更新
                    routingTable.put(destination, new RouteTableEntry(destination, newHops, sender));
                }
            }
        }
    } // end receiveUpdate()

    /**
     * 从路由表中移除指定路由器的所有条目
     *
     * @param router 路由器
     */
    public void removeEntriesForRouter(Router router) {
        /* 使用 Iterator 来避免在遍历时修改集合，避免 ConcurrentModificationException 异常 */
        Iterator<Map.Entry<Network, RouteTableEntry>> it = routingTable.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Network, RouteTableEntry> entry = it.next();
            if (entry.getValue().getNextHop().equals(router)) {
                it.remove(); // 移除该条目
            }
        }
    } // end removeEntriesForRouter()

    /**
     * 从路由表中移除指定网络的所有条目
     *
     * @param network 网络
     */
    public void removeEntriesForNetwork(Network network) {
        /* 使用 Iterator 来避免在遍历时修改集合，避免 ConcurrentModificationException 异常 */
        Iterator<Map.Entry<Network, RouteTableEntry>> it = routingTable.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Network, RouteTableEntry> entry = it.next();
            Router nextHop = entry.getValue().getNextHop();
            if (entry.getKey().equals(network)) {
                entry.getValue().setHops(16);
            } else if (!nextHop.equals(this) && nextHop.isDirectlyConnectedNetwork(network)) {
                entry.getValue().setHops(16);
            }
        }
    } // end removeEntriesForNetwork()

    /**
     * 路由器故障
     */
    public void failure() {
        stopScheduler();

        for (RouteTableEntry entry : new ArrayList<>(routingTable.values())) {
            entry.setHops(16); // 使用16表示不可达
        }

        notifyNeighborsForFailure(); // 通知所有相邻路由器当前路由器故障
    } // end failure()

    /**
     * 通知所有相邻路由器当前路由器故障
     */
    private void notifyNeighborsForFailure() {
        for (Router neighbor : neighbors) {
            neighbor.removeNeighbor(this);
            neighbor.updateRoutingTableForFailedRouter(this);
        }
    } // end notifyNeighborsForFailure()

    /**
     * 当前路由器故障时，更新路由表
     *
     * @param failedRouter 故障路由器
     */
    public void updateRoutingTableForFailedRouter(Router failedRouter) {
        for (RouteTableEntry entry : new ArrayList<>(routingTable.values())) {
            if (entry.getNextHop().equals(failedRouter)) {
                entry.setHops(16); // 使用16表示不可达
            }
        }
    } // end updateRoutingTableForFailedRouter()

    /**
     * 停止定时器
     */
    public void stopScheduler() {
        if (scheduler != null && !scheduler.isShutdown()) {
            scheduler.shutdown(); // 停止发送路由更新
        }
    } // end stopScheduler()

    /**
     * 打印路由表信息
     *
     * @return 路由表信息
     */
    public String printRoutingTable() {
        routingTableInfo = new StringBuilder();

        routingTableInfo.append(routerName).append("的路由表：\n");
        routingTableInfo.append("目的网络\t\t跳数\t\t下一跳\n");

        /* 保证路由表无重复项 */
        Set<String> networkNames = new HashSet<>();
        Iterator<Map.Entry<Network, RouteTableEntry>> it = routingTable.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Network, RouteTableEntry> entry = it.next();
            RouteTableEntry routeEntry = entry.getValue();

            String networkName = routeEntry.getDestination().getNetworkName();
            if (networkNames.contains(networkName) && routeEntry.getHops() == 16) {
                it.remove();
            } else {
                networkNames.add(networkName);
            }
        }

        for (RouteTableEntry entry : routingTable.values()) {
            routingTableInfo.append(entry.getDestination().getNetworkName()).append("\t\t")
                    .append(entry.getHops()).append("\t\t");
            int hops = entry.getHops();
            if (hops == 1) {
                routingTableInfo.append("直接交付\n");
            } else if (hops < 16) {
                routingTableInfo.append(entry.getNextHop().getRouterName()).append("\n");
            } else {
                routingTableInfo.append("不可达\n");
            }
        }

        return routingTableInfo.toString();
    } // end printRoutingTable()

    public String getRouterName() {
        return routerName;
    }

    public void setRouterName(String routerName) {
        this.routerName = routerName;
    }

    public Map<Network, RouteTableEntry> getRoutingTable() {
        return routingTable;
    }

    public void setRoutingTable(Map<Network, RouteTableEntry> routingTable) {
        this.routingTable = routingTable;
    }

    public List<Router> getNeighbors() {
        return neighbors;
    }

    public void setNeighbors(List<Router> neighbors) {
        this.neighbors = neighbors;
    }
} // end class Router
