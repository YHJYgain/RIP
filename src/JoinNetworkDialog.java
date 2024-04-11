import javax.swing.*;
import java.awt.*;

/**
 * “网络加入”对话框类
 *
 * @author wzy
 * @date 2024-03-04 18:49:00
 */
public class JoinNetworkDialog extends JDialog {
    private RIPGUI ripgui; // UI
    private JTextField networkNameField, directlyConnectedRouterField; // 网络名称、直连路由器输入框
    private JButton joinButton, cancelButton; // 功能按钮
    private NetworkTopology networkTopology; // 网络拓扑

    public JoinNetworkDialog(Frame owner, NetworkTopology networkTopology) {
        super(owner, "网络加入（输入框有悬浮提示）", true);
        this.ripgui = (RIPGUI) owner;
        this.networkTopology = networkTopology;

        setSize(300, 150);
        setLocationRelativeTo(null); // 窗口居中显示
        setLayout(new GridLayout(3, 2));

        add(new JLabel("网络名称："));
        networkNameField = new JTextField();
        networkNameField.setToolTipText("例如：网1,Net1");
        add(networkNameField);

        add(new JLabel("直连路由器："));
        directlyConnectedRouterField = new JTextField();
        directlyConnectedRouterField.setToolTipText("例如：A,B");
        add(directlyConnectedRouterField);

        joinButton = new JButton("加入");
        joinButton.addActionListener(e -> joinNetwork());
        add(joinButton);

        cancelButton = new JButton("取消");
        cancelButton.addActionListener(e -> dispose());
        add(cancelButton);

        setLocationRelativeTo(owner);
    } // end JoinNetworkDialog()

    /**
     * 网络加入
     */
    private void joinNetwork() {
        String networkName = networkNameField.getText();
        String routers = directlyConnectedRouterField.getText();

        if (networkName.isEmpty() || routers.isEmpty()) {
            JOptionPane.showMessageDialog(null, "信息未输入完整！！", "输入警告",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        Network findNetwork = networkTopology.findNetwork(networkName);
        if (findNetwork != null) {
            JOptionPane.showMessageDialog(null, "网络拓扑中已存在该网络！！！", "加入错误",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        Network newNetwork = new Network(networkName);
        networkTopology.joinNetwork(newNetwork);
        String[] routerNames = routers.split(","); // 使用逗号分割路由器名称
        if (ripgui.checkRoutersExist(routerNames)) {
            for (String routerName : routerNames) {
                Router findRouter = networkTopology.findRouter(routerName);
                /* 直连到新网络的所有路由器互为相邻路由器 */
                for (String routerName1 : routerNames) {
                    Router findRouter1 = networkTopology.findRouter(routerName1);
                    if (!findRouter1.equals(findRouter)) {
                        findRouter.addNeighbor(findRouter1);
                        findRouter1.addNeighbor(findRouter);
                    }
                }
                findRouter.addDirectlyConnectedNetwork(newNetwork); // 路由器存在，则直连到网络
            }
        } else return;

        dispose();
        JOptionPane.showMessageDialog(null, "网络“" + networkName + "”已成功加入到网络拓扑中！");
    } // end joinNetwork()
} // end class JoinNetworkDialog
