import javax.swing.*; //swing图形界面库
import java.awt.*; //AWT图形组件库
import java.awt.geom.Rectangle2D;//几何图形类
import java.awt.geom.RoundRectangle2D;//圆角矩形类

// 计算器主类，继承JFrame实现窗口功能
public class Calculator extends JFrame {
    // 新增成员变量声明
    private final JTextField see;
    private boolean isResultDisplayed = false;
    private double operand1 = 0;
    private String operator = "";

    public Calculator() {
        // 计算器窗口设置
        setTitle("计算器");                //标题
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //关闭窗口，终止程序
        setSize(650, 500);// 设置窗口尺寸
        setLocationRelativeTo(null);   // 让窗口出现在桌面中间
        setLayout(new BorderLayout());   // 边界布局管理器

        // 上边的菜单栏
        JMenuBar menu = new JMenuBar();
        menu.add(new JMenu("编辑(E)"));
        menu.add(new JMenu("查看(V)"));
        menu.add(new JMenu("帮助(H)"));
        setJMenuBar(menu);
        // 文本框
        /*JTextField*/see = new JTextField("0"); // 初始化内容(移除局部变量声明)
        see.setFont(new Font("微软雅黑", Font.PLAIN, 30)); // 设置字体
        see.setHorizontalAlignment(JTextField.RIGHT); // 右对齐
        add(see, BorderLayout.NORTH);                 // 添加到窗口顶部

        //文本框下面的面板
        JPanel mainPanel = new JPanel(new GridLayout(5, 1, 5, 5)); // 5行1列
        // 按钮内容
        String[][] onbutton = {
                {"Backspace",    "CE",     "C"},
                {"MC", "7", "8", "9", "/", "√"},
                {"MR", "4", "5", "6", "X", "%"},
                {"MS", "1", "2", "3","-","1/x"},
                {"M+", "0", "±", ".", "+", "="}
        };

        // 创建按钮行
        for (String[] config : onbutton) {
            JPanel row = new JPanel(new GridLayout(1, config.length, 5, 5)); // 每行按钮
            for (String text : config) {
                Rounded(row, text); // 调用自定义方法添加圆角
            }
            mainPanel.add(row);
        }
        add(mainPanel, BorderLayout.CENTER); // 将主面板添加到窗口中心
        setVisible(true); // 显示窗口
    }

    //自定义圆角按钮方法
    private void Rounded(JPanel panel, String text) {
        JButton button = new JButton(text) {
            private final int R = 10;    // 圆角半径
            private Color BColor = Color.WHITE; // 背景颜色

            // 自定义绘制方法
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                // 启用抗锯齿
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                // 绘制圆角背景
                g2.setColor(BColor);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), R, R));

                // 绘制文字
                FontMetrics fm = g2.getFontMetrics();
                Rectangle2D textBounds = fm.getStringBounds(getText(), g2);
                int x = (int) ((getWidth() - textBounds.getWidth()) / 2);
                int y = (int) ((getHeight() - textBounds.getHeight()) / 2 + fm.getAscent());
                g2.setColor(Color.BLACK);
                g2.drawString(getText(), x, y);
                // 绘制边框
                g2.setColor(new Color(200, 200, 200));
                g2.draw(new RoundRectangle2D.Double(0, 0, getWidth()-1, getHeight()-1, R, R));
                g2.dispose();
            }

            // 鼠标事件监听，状态变化处理，让按钮颜色改变
            {
                addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseEntered(java.awt.event.MouseEvent evt) {
                        BColor = new Color(245, 245, 245);
                        repaint();
                    }

                    public void mouseExited(java.awt.event.MouseEvent evt) {
                        BColor = Color.WHITE;
                        repaint();
                    }

                    public void mousePressed(java.awt.event.MouseEvent evt) {
                        BColor = new Color(230, 230, 230);
                        repaint();
                    }

                    public void mouseReleased(java.awt.event.MouseEvent evt) {
                        /* BColor = Color.WHITE; */ //这是原来的代码，直接把按钮颜色改为白色，但是会引起一些问题
                        // 修改为：
                        // 通过事件源获取按钮实例，避免使用外部变量 button
                        JButton sourceButton = (JButton) evt.getSource();
                        if (sourceButton.contains(evt.getPoint())) {
                            // 鼠标仍在按钮上 → 显示hover状态
                            BColor = new Color(245, 245, 245);
                        } else {
                            BColor = Color.WHITE;
                        }
                        repaint();
                    }
                });
            }


        };

        // 按钮基础设置
        button.setFont(new Font("微软雅黑", Font.BOLD, 16)); // 字体
        button.setContentAreaFilled(false);    // 禁用默认填充
        button.setFocusPainted(false);         // 禁用焦点边框
        button.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10)); // 内边距
        button.setOpaque(false);// 透明背景

        // 按钮事件处理
        button.addActionListener(e -> {
            try {
                if (text.matches("\\d|\\.")) handleNumber(text);
                else if (text.equals("±")) handleSign();
                else if (text.equals("%")) handlePercent();
                else if (text.equals("√")) handleSqrt();
                else if (text.equals("1/x")) handleReciprocal();
                else if (text.equals("C") || text.equals("CE")) handleClear(text);
                else if (text.equals("Backspace")) handleBackspace();
                else if (text.equals("=")) handleEqual();
                else if (text.equals("+") || text.equals("-") || text.equals("X") || text.equals("/")) handleOperator(text);
            } catch (Exception ex) {
                showError("输入错误");
            }
        });
        panel.add(button);                     // 添加按钮到面板
    }

    // 数字/小数点处理
    private void handleNumber(String num) {
        if (isResultDisplayed || see.getText().equals("0")) {
            see.setText(num);
        } else {
            see.setText(see.getText() + num);
        }
    }
    // 正负号切换
    private void handleSign() {
        String text = see.getText();
        if (!text.equals("0")) {
            double num = Double.parseDouble(text);
            see.setText(String.valueOf(num * -1));
        }
    }
    // 百分比处理
    private void handlePercent() {
        double num = Double.parseDouble(see.getText());
        see.setText(String.valueOf(num / 100));
    }

    // 平方根计算
    private void handleSqrt() {
        try {
            double num = Double.parseDouble(see.getText());
            see.setText(String.valueOf(Math.sqrt(num)));
        } catch (Exception e) {
            showError("无效输入");
        }
    }

    // 倒数计算
    private void handleReciprocal() {
        try {
            double num = Double.parseDouble(see.getText());
            see.setText(String.valueOf(1 / num));
        } catch (Exception e) {
            showError("除以零错误");
        }
    }

    // 清除操作
    private void handleClear(String type) {
        operand1 = 0;
        operator = "";
        see.setText("0");
        isResultDisplayed = false;
    }

    // 退格键
    private void handleBackspace() {
        String text = see.getText();
        if (text.length() > 1) {
            see.setText(text.substring(0, text.length()-1));
        } else {
            see.setText("0");
        }
    }
    // 等号计算
    private void handleEqual() {
        try {
            double operand2 = Double.parseDouble(see.getText());
            switch (operator) {
                case "+": operand1 += operand2; break;
                case "-": operand1 -= operand2; break;
                case "X": operand1 *= operand2; break;
                case "/": operand1 /= operand2; break;
            }
            see.setText(String.valueOf(operand1));
            operator = "";
            isResultDisplayed = true;
        } catch (Exception e) {
            showError("计算错误");
        }
    }

    // 运算符处理
    private void handleOperator(String op) {
        operand1 = Double.parseDouble(see.getText());
        operator = op;
        isResultDisplayed = true;
    }

    // 错误提示
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "错误", JOptionPane.ERROR_MESSAGE);
        see.setText("0");
        isResultDisplayed = false;
    }

    public static void main(String[] args) {
       new Calculator(); // 启动！！！
    }
}
