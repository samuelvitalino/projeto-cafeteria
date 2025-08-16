package telas;

import conexao.Conexao;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class Login extends JFrame {

    private JLabel titulo = new JLabel("Login");
    private JLabel id = new JLabel("ID:");
    private JTextField displayID = new JTextField();
    private JLabel senha = new JLabel("SENHA:");
    private JPasswordField displaySenha = new JPasswordField();
    private JButton btnEnviar = new JButton("ENTRAR");

    private Font fonte1 = new Font("Arial", Font.BOLD, 18);
    private Font fonte3 = new Font("Arial", Font.BOLD, 16);
    Color background = Color.decode("#491a00");
    Color btnColor = new Color(0, 0, 0);

    public Login() {
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTitle("Café & Brisa");
        this.setBounds(500, 300, 400, 250);
        this.setLayout(null);
        this.setLocationRelativeTo(null);
        this.setUndecorated(true);
        this.setResizable(false);
        this.getContentPane().setBackground(background);

        titulo.setBounds(160, 10, 80, 30);
        titulo.setForeground(Color.WHITE);
        titulo.setFont(fonte1);
        add(titulo);

        id.setBounds(80, 60, 120, 20);
        id.setForeground(Color.WHITE);
        id.setFont(fonte3);
        add(id);

        displayID.setBounds(150, 60, 150, 20);
        displayID.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
        add(displayID);

        senha.setBounds(80, 100, 120, 20);
        senha.setForeground(Color.WHITE);
        senha.setFont(fonte3);
        add(senha);

        displaySenha.setBounds(150, 100, 150, 20);
        displaySenha.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY, 2));
        add(displaySenha);

        btnEnviar.setBounds(130, 160, 120, 30);
        btnEnviar.setFont(fonte3);
        btnEnviar.setForeground(Color.WHITE);
        btnEnviar.setBackground(btnColor);
        add(btnEnviar);

        btnEnviar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                autenticarUsuario();
            }
        });

        this.setVisible(true);
    }

    private void autenticarUsuario() {
        String usuario = displayID.getText();
        String senha = new String(displaySenha.getPassword());

        if (usuario.isEmpty() || senha.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Preencha todos os campos!");
            return;
        }

        Connection conn = Conexao.conectar();
        if (conn == null) {
            JOptionPane.showMessageDialog(this, "Erro de conexão com o banco!");
            return;
        }

        try {
            String sql = "SELECT * FROM usuarios WHERE id_usuario = ? AND senha = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, usuario);
            stmt.setString(2, senha);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String tipo = rs.getString("tipo");
                String nome = rs.getString("nome");
                abrirMenu(tipo, nome);
            } else {
                JOptionPane.showMessageDialog(this, "Credenciais inválidas!");
            }

            rs.close();
            stmt.close();
            conn.close();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Erro ao consultar: " + ex.getMessage());
        }
    }

    private void abrirMenu(String tipoUsuario, String nomeUsuario) {
        new Home(tipoUsuario, nomeUsuario);  
        this.dispose();
    }

    public static void main(String[] args) {
        new Login();
    }
}


