package org.example.newcarre.Study;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class tes extends JFrame {
    // 定义按钮组件
    JButton btn = new JButton("将窗口变成黄色");
    // 定义窗口容器（这里存在隐藏BUG）
    Container cp = getContentPane();

    // 构造方法：初始化整个窗口界面
    public tes(){
        // 调用父类JFrame构造，设置窗口标题
        super("Action Event");
        // 设置流式布局：组件从左到右水平摆放，自动换行
        cp.setLayout(new FlowLayout());

        // 给按钮绑定点击监听器（匿名内部类写法）
        btn.addActionListener(new ActionListener(){
            // 按钮点击触发的回调方法
            public void actionPerformed(ActionEvent e){
                setTitle("事件处理机制");  // 修改窗口标题
                cp.setBackground(Color.yellow); // 修改窗口容器背景为黄色
            }
        });
        // 把按钮添加到窗口容器中展示
        cp.add(btn);
        // 设置窗口宽800、高600
        setSize(800,600);
        // 显示窗口
        setVisible(true);
    }

    public static void main(String args[]){
        // 程序入口，创建Test实例，自动执行构造方法完成窗口渲染
        new tes();
    }
}