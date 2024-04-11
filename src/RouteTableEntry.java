/**
 * 路由表项类
 * 
 * @author wzy
 * @date 2024-03-04 18:51:05
 */
public class RouteTableEntry {
    private Network destination; // 目的网络
    private int hops; // 跳数（距离）
    private Router nextHop; // 下一跳（路由器）

    public RouteTableEntry(Network destination, int hops, Router nextHop) {
        this.destination = destination;
        this.hops = hops;
        this.nextHop = nextHop;
    } // end RouteTable()

    public Network getDestination() {
        return destination;
    } // end getDestination()

    public void setDestination(Network destination) {
        this.destination = destination;
    } // end setDestination()

    public int getHops() {
        return hops;
    } // end getHops()

    public void setHops(int hops) {
        this.hops = hops;
    } // end setHops()

    public Router getNextHop() {
        return nextHop;
    } // end getNextHop()

    public void setNextHop(Router nextHop) {
        this.nextHop = nextHop;
    } // end setNextHop()
} // end class RouteTableEntry
