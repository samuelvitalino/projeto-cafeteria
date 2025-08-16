package telas;

import classes.Cardapio;
import conexao.Conexao;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableCellEditor;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.EventObject;

public class GerenciarCardapio extends JFrame {

    private JTable tabela;
    private DefaultTableModel modeloTabela;

    public GerenciarCardapio() {
        setTitle("Gerenciar Cardápio");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10,10));

        JButton btnNovo = new JButton("Novo Item");
        btnNovo.setBackground(Color.decode("#491a00"));
        btnNovo.setForeground(Color.WHITE);
        btnNovo.addActionListener(e -> abrirDialogoCardapio(null));
        add(btnNovo, BorderLayout.NORTH);

        modeloTabela = new DefaultTableModel(new Object[]{"ID", "Nome", "Preço", "Custo", "Ativo", "Descrição", "Editar"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 6;
            }
        };

        tabela = new JTable(modeloTabela);
        tabela.getColumn("Editar").setCellRenderer(new ButtonRenderer());
        tabela.getColumn("Editar").setCellEditor(new ButtonEditor(new JCheckBox()));

        JScrollPane scroll = new JScrollPane(tabela);
        add(scroll, BorderLayout.CENTER);

        carregarCardapio();
        setVisible(true);
    }

    private void carregarCardapio() {
        modeloTabela.setRowCount(0);
        try (Connection conn = Conexao.conectar()) {
            String sql = "SELECT id, nome, preco, custo, ativo, descricao FROM cardapio";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                modeloTabela.addRow(new Object[] {
                    rs.getInt("id"),
                    rs.getString("nome"),
                    rs.getDouble("preco"),
                    rs.getDouble("custo"),
                    rs.getBoolean("ativo"),
                    rs.getString("descricao"),
                    "Editar"
                });
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar cardápio: " + e.getMessage());
        }
    }

    private void abrirDialogoCardapio(Cardapio item) {
        boolean editar = item != null;

        JDialog dialog = new JDialog(this, editar ? "Editar Item" : "Novo Item", true);
        dialog.setSize(500, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new GridBagLayout());
        dialog.setResizable(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblNome = new JLabel("Nome:");
        JTextField txtNome = new JTextField(editar ? item.getNome() : "");

        JLabel lblPreco = new JLabel("Preço:");
        JTextField txtPreco = new JTextField(editar ? String.valueOf(item.getPreco()) : "");

        JLabel lblCusto = new JLabel("Custo:");
        JTextField txtCusto = new JTextField(editar ? String.valueOf(item.getCusto()) : "");

        JLabel lblDescricao = new JLabel("Descrição:");
        JTextArea txtDescricao = new JTextArea(editar ? item.getDescricao() : "", 5, 20);
        JScrollPane scrollDesc = new JScrollPane(txtDescricao);

        JCheckBox chkAtivo = new JCheckBox("Ativo", editar ? item.isAtivo() : true);

        JLabel lblImagem = new JLabel("Imagem:");
        JLabel lblPreviewImagem = new JLabel();
        lblPreviewImagem.setPreferredSize(new Dimension(120, 120));
        lblPreviewImagem.setBorder(BorderFactory.createLineBorder(Color.BLACK));

        final byte[][] imagemSelecionada = new byte[1][];

        if (editar && item.getImagem() != null) {
            ImageIcon icon = new ImageIcon(item.getImagem());
            Image img = icon.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH);
            lblPreviewImagem.setIcon(new ImageIcon(img));
        } else {
            lblPreviewImagem.setText("Nenhuma imagem");
            lblPreviewImagem.setHorizontalAlignment(SwingConstants.CENTER);
        }

        JButton btnSelecionarImagem = new JButton("Selecionar Imagem");
        btnSelecionarImagem.setBackground(Color.decode("#491a00"));
        btnSelecionarImagem.setForeground(Color.WHITE);
        btnSelecionarImagem.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            int result = chooser.showOpenDialog(dialog);
            if (result == JFileChooser.APPROVE_OPTION) {
                File arquivo = chooser.getSelectedFile();
                try (FileInputStream fis = new FileInputStream(arquivo)) {
                    imagemSelecionada[0] = fis.readAllBytes();
                    ImageIcon icon = new ImageIcon(imagemSelecionada[0]);
                    Image img = icon.getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH);
                    lblPreviewImagem.setIcon(new ImageIcon(img));
                    lblPreviewImagem.setText("");
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(dialog, "Erro ao carregar imagem: " + ex.getMessage());
                }
            }
        });

        JButton btnSalvar = new JButton(editar ? "Salvar Alterações" : "Cadastrar");
        btnSalvar.setBackground(Color.decode("#491a00"));
        btnSalvar.setForeground(Color.WHITE);
        gbc.gridx = 0; gbc.gridy = 0; dialog.add(lblNome, gbc);
        gbc.gridx = 1; gbc.gridy = 0; dialog.add(txtNome, gbc);

        gbc.gridx = 0; gbc.gridy = 1; dialog.add(lblPreco, gbc);
        gbc.gridx = 1; gbc.gridy = 1; dialog.add(txtPreco, gbc);

        gbc.gridx = 0; gbc.gridy = 2; dialog.add(lblCusto, gbc);
        gbc.gridx = 1; gbc.gridy = 2; dialog.add(txtCusto, gbc);

        gbc.gridx = 0; gbc.gridy = 3; dialog.add(lblDescricao, gbc);
        gbc.gridx = 1; gbc.gridy = 3; dialog.add(scrollDesc, gbc);

        gbc.gridx = 1; gbc.gridy = 4; dialog.add(chkAtivo, gbc);

        gbc.gridx = 0; gbc.gridy = 5; dialog.add(lblImagem, gbc);
        gbc.gridx = 1; gbc.gridy = 5; dialog.add(lblPreviewImagem, gbc);

        gbc.gridx = 1; gbc.gridy = 6; dialog.add(btnSelecionarImagem, gbc);
        gbc.gridx = 1; gbc.gridy = 7; dialog.add(btnSalvar, gbc);

        btnSalvar.addActionListener(e -> {
            String nome = txtNome.getText().trim();
            String precoStr = txtPreco.getText().trim();
            String custoStr = txtCusto.getText().trim();
            String descricao = txtDescricao.getText().trim();
            boolean ativo = chkAtivo.isSelected();

            if (nome.isEmpty() || precoStr.isEmpty() || custoStr.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Nome, preço e custo são obrigatórios.");
                return;
            }

            double preco, custo;
            try {
                preco = Double.parseDouble(precoStr);
                custo = Double.parseDouble(custoStr);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Preço e custo devem ser números válidos.");
                return;
            }

            try (Connection conn = Conexao.conectar()) {
                String sql;
                if (editar) {
                    sql = "UPDATE cardapio SET nome=?, preco=?, custo=?, descricao=?, ativo=?, imagem=? WHERE id=?";
                } else {
                    sql = "INSERT INTO cardapio (nome, preco, custo, descricao, ativo, imagem) VALUES (?, ?, ?, ?, ?, ?)";
                }
                PreparedStatement stmt = conn.prepareStatement(sql);

                stmt.setString(1, nome);
                stmt.setDouble(2, preco);
                stmt.setDouble(3, custo);
                stmt.setString(4, descricao);
                stmt.setBoolean(5, ativo);

                if (imagemSelecionada[0] != null) {
                    stmt.setBytes(6, imagemSelecionada[0]);
                } else if (editar && item.getImagem() != null) {
                    stmt.setBytes(6, item.getImagem());
                } else {
                    stmt.setNull(6, Types.BLOB);
                }

                if (editar) {
                    stmt.setInt(7, item.getId());
                }

                stmt.executeUpdate();
                stmt.close();

                JOptionPane.showMessageDialog(dialog, editar ? "Item atualizado!" : "Item cadastrado!");
                dialog.dispose();
                carregarCardapio();
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Erro ao salvar item: " + ex.getMessage());
            }
        });

        dialog.setVisible(true);
    }

    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setText("Editar");
            setBackground(Color.decode("#491a00"));
            setForeground(Color.WHITE);
            setFocusPainted(false);
            setBorder(BorderFactory.createLineBorder(Color.decode("#491a00")));
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private boolean clicked;
        private int row;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton("Editar");
            button.setBackground(Color.decode("#491a00"));
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            button.setBorder(BorderFactory.createLineBorder(Color.decode("#491a00")));
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.row = row;
            clicked = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (clicked) {
                Cardapio item = new Cardapio();
                item.setId((int) modeloTabela.getValueAt(row, 0));
                item.setNome((String) modeloTabela.getValueAt(row, 1));
                item.setPreco((double) modeloTabela.getValueAt(row, 2));
                item.setCusto((double) modeloTabela.getValueAt(row, 3));
                item.setAtivo((boolean) modeloTabela.getValueAt(row, 4));
                item.setDescricao((String) modeloTabela.getValueAt(row, 5));
                try (Connection conn = Conexao.conectar()) {
                    String sql = "SELECT imagem FROM cardapio WHERE id = ?";
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setInt(1, item.getId());
                    ResultSet rs = ps.executeQuery();
                    if (rs.next()) {
                        item.setImagem(rs.getBytes("imagem"));
                    }
                    rs.close();
                    ps.close();
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(null, "Erro ao carregar imagem do banco: " + ex.getMessage());
                }
                abrirDialogoCardapio(item);
            }
            clicked = false;
            return "Editar";
        }

        @Override
        public boolean stopCellEditing() {
            clicked = false;
            return super.stopCellEditing();
        }

        @Override
        public boolean isCellEditable(EventObject e) {
            return true;
        }
    }

}
