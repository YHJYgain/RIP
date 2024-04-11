import javax.swing.*;
import java.awt.*;

/**
 * “路由器退出”对话框类
 *
 * @author wzy
 * @date 2024-03-04 20:15:36
 */
public class ExitRouterDialog extends JDialog {
    private RIPGUI ripgui; // UI
    private JTextField routerNameField; // 路由器名称输入框
    private JButton exitButton, cancelButton; // 功能按钮
    private NetworkTopology networkTopology; // 网络拓扑

    public ExitRouterDialog(Frame owner, NetworkTopology networkTopology) {
        super(owner, "路由器退出（输入框有悬浮提示）", true);
        this.ripgui = (RIPGUI) owner;
        this.networkTopology = networkTopology;

        setSize(300, 110);
        setLocationRelativeTo(null); // 窗口居中显示
        setLayout(new GridLayout(2, 2));

        add(new JLabel("路由器名称："));
        routerNameField = new JTextField();
        routerNameField.setToolTipText("例如：A,B");
        add(routerNameField);

        exitButton = new JButton("退出");
        exitButton.addActionListener(e -> exitRouter());
        add(exitButton);

        cancelButton = new JButton("取消");
        cancelButton.addActionListener(e -> dispose());
        add(cancelButton);

        setLocationRelativeTo(owner);
    } // end ExitRouterDialog()

    /**
     * 路由器退出
     */
    private void exitRouter() {
        String routers = routerNameField.getText();

        if (routers.isEmpty()) {
            JOptionPane.showMessageDialog(null, "信息未输入完整！！", "输入警告",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String[] routerNames = routers.split(","); // 使用逗号分割路由器名称
        if (ripgui.checkRoutersExist(routerNames)) {
            for (String routerName : routerNames) {
                Router findRouter = networkTopology.findRouter(routerName);
                networkTopology.exitRouter(findRouter);
            }
        } else return;

        dispose();
        JOptionPane.showMessageDialog(null, "选定的路由器已成功退出网络拓扑！");
    } // end exitRouter()
} // end class ExitRouterDialog
