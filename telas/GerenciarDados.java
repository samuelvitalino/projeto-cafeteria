package telas;

import classes.Usuario;
import conexao.Conexao;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class GerenciarDados extends JFrame {

    private JTable tabela;
    private DefaultTableModel modeloTabela;

    public GerenciarDados() {
        setTitle("Gerenciar Usuários");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10,10));

        JButton btnNovo = new JButton("Registrar Novo Usuário");
        btnNovo.setBackground(Color.decode("#491a00"));
        btnNovo.setForeground(Color.WHITE);
        btnNovo.setFocusPainted(false);
        btnNovo.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnNovo.addActionListener(e -> abrirDialogRegistrarUsuario());
        this.add(btnNovo, BorderLayout.NORTH);

        modeloTabela = new DefaultTableModel(new Object[]{"ID", "ID Usuário", "Nome", "Email", "Tipo", "Editar"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5;
            }
        };

        tabela = new JTable(modeloTabela);
        tabela.getColumn("Editar").setCellRenderer(new ButtonRenderer());
        tabela.getColumn("Editar").setCellEditor(new ButtonEditor(new JCheckBox()));

        JScrollPane scroll = new JScrollPane(tabela);
        this.add(scroll, BorderLayout.CENTER);

        carregarUsuarios();
        setVisible(true);
    }

    private void carregarUsuarios() {
        modeloTabela.setRowCount(0);
        try (Connection conn = Conexao.conectar()) {
            String sql = "SELECT * FROM usuarios";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                modeloTabela.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("id_usuario"),
                        rs.getString("nome"),
                        rs.getString("email"),
                        rs.getString("tipo"),
                        "Editar"
                });
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar usuários: " + e.getMessage());
        }
    }

    private void abrirDialogRegistrarUsuario() {
        JDialog dialog = new JDialog(this, "Registrar Novo Usuário", true);
        dialog.setSize(450, 400);
        dialog.setLocationRelativeTo(this);

        JPanel painel = new JPanel(new GridLayout(6, 2, 10, 10));
        painel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField txtNome = new JTextField();
        JTextField txtEmail = new JTextField();
        JPasswordField txtSenha = new JPasswordField();
        JComboBox<String> cmbTipo = new JComboBox<>(new String[]{"admin", "financeiro", "atendente"});

        JButton btnSalvar = new JButton("Salvar");
        btnSalvar.setBackground(Color.decode("#491a00"));
        btnSalvar.setForeground(Color.WHITE);
        btnSalvar.setFocusPainted(false);
        btnSalvar.setCursor(new Cursor(Cursor.HAND_CURSOR));

        painel.add(new JLabel("Nome:")); painel.add(txtNome);
        painel.add(new JLabel("Email:")); painel.add(txtEmail);
        painel.add(new JLabel("Senha:")); painel.add(txtSenha);
        painel.add(new JLabel("Tipo:")); painel.add(cmbTipo);
        painel.add(new JLabel());
        painel.add(btnSalvar);

        btnSalvar.addActionListener(e -> {
            String nome = txtNome.getText();
            String email = txtEmail.getText();
            String senha = new String(txtSenha.getPassword());
            String tipo = (String)cmbTipo.getSelectedItem();

            if (nome.isEmpty() || email.isEmpty() || senha.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Preencha todos os campos!");
                return;
            }

            String prefixo = switch (tipo) {
                case "admin" -> "A";
                case "financeiro" -> "F";
                case "atendente" -> "O";
                default -> "";
            };
            String idGerado = prefixo + String.format("%04d", (int)(Math.random() * 10000));

            Usuario novo = new Usuario();
            novo.setIdUsuario(idGerado);
            novo.setNome(nome);
            novo.setEmail(email);
            novo.setSenha(senha);
            novo.setTipo(tipo);

            try (Connection conn = Conexao.conectar()) {
                String sql = "INSERT INTO usuarios (id_usuario, nome, email, senha, tipo) VALUES (?,?,?,?,?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, novo.getIdUsuario());
                stmt.setString(2, novo.getNome());
                stmt.setString(3, novo.getEmail());
                stmt.setString(4, novo.getSenha());
                stmt.setString(5, novo.getTipo());
                stmt.executeUpdate();
                stmt.close();
                JOptionPane.showMessageDialog(dialog, "Usuário registrado com sucesso!");
                dialog.dispose();
                carregarUsuarios();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Erro ao salvar: " + ex.getMessage());
            }
        });

        dialog.setContentPane(painel);
        dialog.setVisible(true);
    }

    private void abrirDialogEditarUsuario(Usuario usuario) {
        JDialog dialog = new JDialog(this, "Editar Usuário", true);
        dialog.setSize(450, 400);
        dialog.setLocationRelativeTo(this);

        JPanel painel = new JPanel(new GridLayout(6, 2, 10, 10));
        painel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField txtNome = new JTextField(usuario.getNome());
        JTextField txtEmail = new JTextField(usuario.getEmail());
        JPasswordField txtSenha = new JPasswordField(usuario.getSenha());
        JComboBox<String> cmbTipo = new JComboBox<>(new String[]{"admin", "financeiro", "atendente"});
        cmbTipo.setSelectedItem(usuario.getTipo());

        JButton btnSalvar = new JButton("Salvar Alterações");
        btnSalvar.setBackground(Color.decode("#491a00"));
        btnSalvar.setForeground(Color.WHITE);
        btnSalvar.setFocusPainted(false);
        btnSalvar.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton btnApagar = new JButton("Apagar Usuário");
        btnApagar.setBackground(Color.decode("#491a00"));
        btnApagar.setForeground(Color.WHITE);
        btnApagar.setFocusPainted(false);
        btnApagar.setCursor(new Cursor(Cursor.HAND_CURSOR));

        painel.add(new JLabel("Nome:")); painel.add(txtNome);
        painel.add(new JLabel("Email:")); painel.add(txtEmail);
        painel.add(new JLabel("Senha:")); painel.add(txtSenha);
        painel.add(new JLabel("Tipo:")); painel.add(cmbTipo);
        painel.add(btnApagar); painel.add(btnSalvar);

        btnSalvar.addActionListener(e -> {
            usuario.setNome(txtNome.getText());
            usuario.setEmail(txtEmail.getText());
            usuario.setSenha(new String(txtSenha.getPassword()));
            usuario.setTipo((String)cmbTipo.getSelectedItem());
            try (Connection conn = Conexao.conectar()) {
                String sql = "UPDATE usuarios SET nome=?, email=?, senha=?, tipo=? WHERE id=?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, usuario.getNome());
                stmt.setString(2, usuario.getEmail());
                stmt.setString(3, usuario.getSenha());
                stmt.setString(4, usuario.getTipo());
                stmt.setInt(5, usuario.getId());
                stmt.executeUpdate();
                stmt.close();
                JOptionPane.showMessageDialog(dialog, "Usuário atualizado!");
                dialog.dispose();
                carregarUsuarios();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Erro ao atualizar: " + ex.getMessage());
            }
        });

        btnApagar.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(dialog, "Tem certeza que deseja excluir este usuário?", "Confirmação", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try (Connection conn = Conexao.conectar()) {
                    String sql = "DELETE FROM usuarios WHERE id=?";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setInt(1, usuario.getId());
                    stmt.executeUpdate();
                    stmt.close();
                    JOptionPane.showMessageDialog(dialog, "Usuário removido!");
                    dialog.dispose();
                    carregarUsuarios();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(dialog, "Erro ao remover: " + ex.getMessage());
                }
            }
        });

        dialog.setContentPane(painel);
        dialog.setVisible(true);
    }

    class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() {
            setText("Editar");
            setBackground(Color.decode("#491a00"));
            setForeground(Color.WHITE);
            setFocusPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean clicked;
        private int row;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton("Editar");
            button.setBackground(Color.decode("#491a00"));
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.row = row;
            this.label = (value == null) ? "Editar" : value.toString();
            button.setText(label);
            clicked = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (clicked) {
                Usuario usuario = new Usuario();
                usuario.setId((int)modeloTabela.getValueAt(row,0));
                usuario.setIdUsuario((String)modeloTabela.getValueAt(row,1));
                usuario.setNome((String)modeloTabela.getValueAt(row,2));
                usuario.setEmail((String)modeloTabela.getValueAt(row,3));
                usuario.setTipo((String)modeloTabela.getValueAt(row,4));
                try (Connection conn = Conexao.conectar()) {
                    String sql = "SELECT senha FROM usuarios WHERE id=?";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setInt(1, usuario.getId());
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) usuario.setSenha(rs.getString("senha"));
                } catch(Exception ex) {
                    ex.printStackTrace();
                }
                abrirDialogEditarUsuario(usuario);
            }
            clicked = false;
            return label;
        }

        @Override
        public boolean stopCellEditing() {
            clicked = false;
            return super.stopCellEditing();
        }
    }

}


