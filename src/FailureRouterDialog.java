import javax.swing.*;
import java.awt.*;

/**
 * “路由器故障”对话框类
 *
 * @author wzy
 * @date 2024-03-04 18:48:49
 */
public class FailureRouterDialog extends JDialog {
    private RIPGUI ripgui; // UI
    private JTextField routerNameField; // 路由器名称输入框
    private JButton failureButton, cancelButton; // 功能按钮
    private NetworkTopology networkTopology; // 网络拓扑

    public FailureRouterDialog(Frame owner, NetworkTopology networkTopology) {
        super(owner, "路由器故障（输入框有悬浮提示）", true);
        this.ripgui = (RIPGUI) owner;
        this.networkTopology = networkTopology;

        setSize(300, 110);
        setLocationRelativeTo(null); // 窗口居中显示
        setLayout(new GridLayout(2, 2));

        add(new JLabel("路由器名称："));
        routerNameField = new JTextField();
        routerNameField.setToolTipText("例如：A,B");
        add(routerNameField);

        failureButton = new JButton("故障");
        failureButton.addActionListener(e -> failureRouter());
        add(failureButton);

        cancelButton = new JButton("取消");
        cancelButton.addActionListener(e -> dispose());
        add(cancelButton);

        setLocationRelativeTo(owner);
    } // end FailureRouterDialog()

    /**
     * 路由器故障
     */
    private void failureRouter() {
        String routers = routerNameField.getText();

        if (routers.isEmpty()) {
            JOptionPane.showMessageDialog(null, "信息未输入完整！！", "输入警告",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String[] routerNames = routers.split(","); // 使用逗号分割路由器名称
        if (ripgui.checkRoutersExist(routerNames)) {
            for (String routerName : routerNames) {
                Router findRouter = networkTopology.findRouter(routerName.trim());
                findRouter.failure(); // 路由器存在，则设置为故障状态
            }
        } else return;

        dispose();
        JOptionPane.showMessageDialog(null, "选定的路由器已成功设置为故障状态！");
    } // end failureRouter()
} // end class FailureRouterDialog
