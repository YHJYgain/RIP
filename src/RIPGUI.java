import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;

/**
 * RIP 主界面
 *
 * @author wzy
 * @date 2024-03-04 18:47:42
 */
public class RIPGUI extends JFrame {
    private static final String ROUTING_TABLE_INFO = "【路由表信息】\n";

    private JPanel mainPanel, leftPanel, rightPanel; // 主面板、右部面板
    private JButton initNetworkTopologyBtn, resetNetworkTopologyBtn; // 网络拓扑功能按钮
    private JButton joinRouterBtn, exitRouterBtn, failureRouterBtn; // 路由器功能按钮
    private JButton joinNetworkBtn, exitNetworkBtn; // 网络功能按钮
    private JoinRouterDialog joinRouterDialog; // “路由器加入”对话框
    private ExitRouterDialog exitRouterDialog; // “路由器退出”对话框
    private JoinNetworkDialog joinNetworkDialog; // "网络加入”对话框
    private ExitNetworkDialog exitNetworkDialog; // “网络退出”对话框
    private FailureRouterDialog failureRouterDialog; // “路由器故障”对话框
    private JTextArea routingTablesInfoTextArea; // 路由表信息文本框
    private NetworkTopology networkTopology; // 网络拓扑
    private ScheduledExecutorService scheduler; // 定时器

    public RIPGUI() {
        super("内部网关协议 RIP 模拟程序");
        this.setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH); // 窗口全屏显示
        setLocationRelativeTo(null); // 窗口居中显示
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // 设置"关闭"按钮操作

        mainPanel = new JPanel(new GridLayout(1, 2));
        scheduler = Executors.newScheduledThreadPool(1);
        initLeftComponents();
        initRightComponents();
        add(mainPanel, BorderLayout.CENTER);
    } // end RIPGUI()

    /**
     * 初始化主面板的左部组件
     */
    private void initLeftComponents() {
        leftPanel = new JPanel(new GridBagLayout());

        try {
            /* 从网络加载图片 */
            URL imageUrl = new URL("https://gitee.com/ReGinWZY/figure-bed/raw/master/TyporaImg/202403042041341.png");
            ImageIcon imageIcon = new ImageIcon(imageUrl);
            JLabel imageLabel = new JLabel(imageIcon);
            leftPanel.add(imageLabel);
        } catch (Exception e) {
            e.printStackTrace();
        }

        initNetworkTopologyBtn = new JButton("初始化网络拓扑");
        resetNetworkTopologyBtn = new JButton("重置网络拓扑");
        joinNetworkBtn = new JButton("网络加入");
        exitNetworkBtn = new JButton("网络退出");
        joinRouterBtn = new JButton("路由器加入");
        exitRouterBtn = new JButton("路由器退出");
        failureRouterBtn = new JButton("路由器故障");

        leftPanel.setPreferredSize(new Dimension(400, 400)); // 设置左侧面板的布局大小
        initNetworkTopologyBtn.addActionListener(e -> initNetworkTopology());
        resetNetworkTopologyBtn.addActionListener(e -> resetNetworkTopology());
        joinNetworkBtn.addActionListener(e -> joinNetwork());
        exitNetworkBtn.addActionListener(e -> exitNetwork());
        joinRouterBtn.addActionListener(e -> joinRouter());
        exitRouterBtn.addActionListener(e -> exitRouter());
        failureRouterBtn.addActionListener(e -> failureRouter());

        /* 在网络拓扑还未初始化时，禁止面板除初始化以外的所有操作 */
        resetNetworkTopologyBtn.setEnabled(false);
        joinNetworkBtn.setEnabled(false);
        exitNetworkBtn.setEnabled(false);
        joinRouterBtn.setEnabled(false);
        exitRouterBtn.setEnabled(false);
        failureRouterBtn.setEnabled(false);

        /* 设置按钮水平垂直居中 */
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; // 将组件放置在网格的第一列
        gbc.gridy = GridBagConstraints.RELATIVE; // 将组件放置在下一行
        gbc.fill = GridBagConstraints.BOTH; // 组件在其显示区域内扩展以填充空间
        gbc.insets = new Insets(20, 20, 20, 20); // 设置组件周围的空白区域

        leftPanel.add(initNetworkTopologyBtn, gbc);
        leftPanel.add(resetNetworkTopologyBtn, gbc);
        leftPanel.add(joinNetworkBtn, gbc);
        leftPanel.add(exitNetworkBtn, gbc);
        leftPanel.add(joinRouterBtn, gbc);
        leftPanel.add(exitRouterBtn, gbc);
        leftPanel.add(failureRouterBtn, gbc);

        mainPanel.add(leftPanel);
    } // end initLeftComponents()

    /**
     * 初始化主面板的右部面板组件
     */
    private void initRightComponents() {
        rightPanel = new JPanel(new BorderLayout());
        routingTablesInfoTextArea = new JTextArea();
        JScrollPane scrollPane = new JScrollPane(routingTablesInfoTextArea);

        routingTablesInfoTextArea.setEditable(false);
        routingTablesInfoTextArea.setFont(new Font("Serif", Font.PLAIN, 15));
        routingTablesInfoTextArea.append(ROUTING_TABLE_INFO);

        rightPanel.add(scrollPane);

        mainPanel.add(rightPanel);
    } // end initRightComponents()

    /**
     * 初始化网络拓扑
     */
    private void initNetworkTopology() {
        networkTopology = new NetworkTopology();

        /* 在网络拓扑初始化后，恢复面板的除初始化的其它操作 */
        initNetworkTopologyBtn.setEnabled(false);
        resetNetworkTopologyBtn.setEnabled(true);
        joinNetworkBtn.setEnabled(true);
        exitNetworkBtn.setEnabled(true);
        joinRouterBtn.setEnabled(true);
        exitRouterBtn.setEnabled(true);
        failureRouterBtn.setEnabled(true);

        Runnable printTask = () -> {
            routingTablesInfoTextArea.setText(ROUTING_TABLE_INFO);
            routingTablesInfoTextArea.append(networkTopology.getRoutingTablesInfo());
        };

        scheduler.scheduleAtFixedRate(printTask, 0, 2, TimeUnit.SECONDS);
    } // end initNetworkTopology()

    /**
     * 重置网络拓扑
     */
    private void resetNetworkTopology() {
        try {
            /* 获取当前 Java 应用的启动命令 */
            String java = System.getProperty("java.home") + "/bin/java";
            String classpath = System.getProperty("java.class.path");
            String mainClass = "RIPGUI"; // 这里需要替换为你的主类名

            /* 构建命令和参数 */
            List<String> command = new ArrayList<>();
            command.add(java);
            command.addAll(Arrays.asList("-cp", classpath, mainClass));

            new ProcessBuilder(command).start(); // 启动一个新的进程来运行命令

            dispose();
            System.exit(0); // 停止当前应用
        } catch (Exception e) {
            e.printStackTrace();
        }
    } // end resetNetworkTopology()

    /**
     * 网络加入
     */
    private void joinNetwork() {
        joinNetworkDialog = new JoinNetworkDialog(RIPGUI.this, networkTopology);
        joinNetworkDialog.setVisible(true);
    } // end joinNetwork()

    /**
     * 网络退出
     */
    private void exitNetwork() {
        exitNetworkDialog = new ExitNetworkDialog(RIPGUI.this, networkTopology);
        exitNetworkDialog.setVisible(true);
    } // end exitNetwork()

    /**
     * 路由器加入
     */
    private void joinRouter() {
        joinRouterDialog = new JoinRouterDialog(RIPGUI.this, networkTopology);
        joinRouterDialog.setVisible(true);
    }  // end joinRouter()

    /**
     * 路由器退出
     */
    private void exitRouter() {
        exitRouterDialog = new ExitRouterDialog(RIPGUI.this, networkTopology);
        exitRouterDialog.setVisible(true);
    } // end exitRouter()

    /**
     * 路由器故障
     */
    private void failureRouter() {
        failureRouterDialog = new FailureRouterDialog(RIPGUI.this, networkTopology);
        failureRouterDialog.setVisible(true);
    } // end failureRouter()

    /**
     * 检查多个路由器是否存在
     *
     * @param routerNames 多个路由器名称
     * @return 检查结果
     */
    public boolean checkRoutersExist(String[] routerNames) {
        StringBuilder notFoundRouters = new StringBuilder();
        boolean isRoutersExist = true;

        /* 对于每个路由器，检查是否存在于网络拓扑中 */
        for (String routerName : routerNames) {
            Router findRouter = networkTopology.findRouter(routerName.trim());
            if (findRouter == null) {
                if (!notFoundRouters.isEmpty()) {
                    notFoundRouters.append(", ");
                }
                notFoundRouters.append(routerName.trim());
                isRoutersExist = false;
            }
        }

        if (!isRoutersExist) {
            JOptionPane.showMessageDialog(null, "网络拓扑中不存在路由器：\n"
                            + notFoundRouters, "输入错误", JOptionPane.ERROR_MESSAGE);
        }

        return isRoutersExist;
    } // end checkRoutersExist()

    /**
     * 检查多个网络是否存在
     *
     * @param networkNames 多个网络名称
     * @return 检查结果
     */
    public boolean checkNetworksExist(String[] networkNames) {
        StringBuilder notFoundNetworks = new StringBuilder();
        boolean isNetworksExist = true;

        /* 对于每个网络，检查是否存在于网络拓扑中 */
        for (String networkName : networkNames) {
            Network findNetwork = networkTopology.findNetwork(networkName.trim());
            if (findNetwork == null) {
                if (!notFoundNetworks.isEmpty()) {
                    notFoundNetworks.append(", ");
                }
                notFoundNetworks.append(networkName.trim());
                isNetworksExist = false;
            }
        }

        if (!isNetworksExist) {
            JOptionPane.showMessageDialog(null, "网络拓扑中不存在以下网络：\n"
                            + notFoundNetworks, "输入错误", JOptionPane.ERROR_MESSAGE);
        }

        return isNetworksExist;
    } // end checkNetworksExist()

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new RIPGUI().setVisible(true));
    } // end main()
} // end class RIPGUI