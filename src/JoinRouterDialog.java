import javax.swing.*;
import java.awt.*;

/**
 * “路由器加入”对话框类
 *
 * @author wzy
 * @date 2024-03-04 18:50:10
 */
public class JoinRouterDialog extends JDialog {
    private RIPGUI ripgui; // UI
    private JTextField routerNameField, neighborField, directlyConnectedNetworkField; // 路由器名称、相邻路由器、直连网络输入框
    private JButton joinButton, cancelButton; // 功能按钮
    private NetworkTopology networkTopology; // 网络拓扑

    public JoinRouterDialog(Frame owner, NetworkTopology networkTopology) {
        super(owner, "路由器加入（输入框有悬浮提示）", true);
        this.ripgui = (RIPGUI) owner;
        this.networkTopology = networkTopology;

        setSize(300, 150);
        setLocationRelativeTo(null); // 窗口居中显示
        setLayout(new GridLayout(4, 2));

        add(new JLabel("路由器名称："));
        routerNameField = new JTextField();
        routerNameField.setToolTipText("例如：A,B");
        add(routerNameField);

        add(new JLabel("相邻路由器："));
        neighborField = new JTextField();
        neighborField.setToolTipText("例如：A,B");
        add(neighborField);

        add(new JLabel("直连网络："));
        directlyConnectedNetworkField = new JTextField();
        directlyConnectedNetworkField.setToolTipText("例如：网1,Net1");
        add(directlyConnectedNetworkField);

        joinButton = new JButton("加入");
        joinButton.addActionListener(e -> joinRouter());
        add(joinButton);

        cancelButton = new JButton("取消");
        cancelButton.addActionListener(e -> dispose());
        add(cancelButton);

        setLocationRelativeTo(owner);
    } // end JoinRouterDialog()

    /**
     * 路由器加入
     */
    private void joinRouter() {
        String routerName = routerNameField.getText();
        String neighbors = neighborField.getText();
        String networks = directlyConnectedNetworkField.getText();

        if (routerName.isEmpty() || neighbors.isEmpty() || networks.isEmpty()) {
            JOptionPane.showMessageDialog(null, "信息未输入完整！！", "输入警告",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Router findRouter = networkTopology.findRouter(routerName.trim());
        if (findRouter != null) {
            JOptionPane.showMessageDialog(null, "网络拓扑中已存在该路由器！！！", "加入错误",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        Router newRouter = new Router(routerName);
        networkTopology.joinRouter(newRouter);
        String[] neighborNames = neighbors.split(",");
        String[] networkNames = networks.split(",");
        if (ripgui.checkRoutersExist(neighborNames) && ripgui.checkNetworksExist(networkNames)) {
            for (String neighborName : neighborNames) {
                Router findNeighbor = networkTopology.findRouter(neighborName);
                newRouter.addNeighbor(findNeighbor); // 路由器存在，则添加为相邻路由器
                findNeighbor.addNeighbor(newRouter);
            }
            for (String networkName : networkNames) {
                Network findNetwork = networkTopology.findNetwork(networkName.trim());
                newRouter.addDirectlyConnectedNetwork(findNetwork); // 网络存在，则直连到路由器
            }
        } else return;

        dispose();
        JOptionPane.showMessageDialog(null, "路由器“" + routerName + "”已成功加入到网络拓扑中！");
    } // end joinRouter()
} // end class JoinRouterDialog
