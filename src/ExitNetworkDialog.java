import javax.swing.*;
import java.awt.*;

/**
 * “网络退出”对话框类
 *
 * @author wzy
 * @date 2024-03-04 18:48:04
 */
public class ExitNetworkDialog extends JDialog {
    private RIPGUI ripgui; // UI
    private JTextField networkNameField; // 网络名称输入框
    private JButton exitButton, cancelButton; // 功能按钮
    private NetworkTopology networkTopology; // 网络拓扑

    public ExitNetworkDialog(Frame owner, NetworkTopology networkTopology) {
        super(owner, "网络退出（输入框有悬浮提示）", true);
        this.ripgui = (RIPGUI) owner;
        this.networkTopology = networkTopology;

        setSize(300, 110);
        setLocationRelativeTo(null); // 窗口居中显示
        setLayout(new GridLayout(2, 2));

        add(new JLabel("网络名称："));
        networkNameField = new JTextField();
        networkNameField.setToolTipText("例如：网1,Net1");
        add(networkNameField);

        exitButton = new JButton("退出");
        exitButton.addActionListener(e -> exitNetwork());
        add(exitButton);

        cancelButton = new JButton("取消");
        cancelButton.addActionListener(e -> dispose());
        add(cancelButton);

        setLocationRelativeTo(owner);
    } // end ExitNetworkDialog()

    /**
     * 网路退出
     */
    private void exitNetwork() {
        String networks = networkNameField.getText();

        if (networks.isEmpty()) {
            JOptionPane.showMessageDialog(null, "信息未输入完整！！", "输入警告",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String[] networkNames = networks.split(","); // 使用逗号分割网络名称
        if (ripgui.checkNetworksExist(networkNames)) {
            for (String networkName : networkNames) {
                Network findNetwork = networkTopology.findNetwork(networkName.trim());
                networkTopology.exitNetwork(findNetwork); // 网络存在，则从网络拓扑中移除
            }
        } else return;

        dispose();
        JOptionPane.showMessageDialog(null, "选定的网络已成功退出网络拓扑！");
    } // end exitNetwork()
} // end class ExitNetworkDialog
