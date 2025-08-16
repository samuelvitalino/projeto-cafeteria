package telas;

import classes.ProdutoEstoque;
import conexao.Conexao;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Estoque extends JFrame {

    private JTable tabela;
    private DefaultTableModel modeloTabela;

    public Estoque() {
        setTitle("Controle de Estoque");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10,10));

        JButton btnNovo = new JButton("Registrar Novo Produto");
        btnNovo.setBackground(Color.decode("#491a00"));
        btnNovo.setForeground(Color.WHITE);
        btnNovo.setFocusPainted(false);
        btnNovo.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnNovo.addActionListener(e -> abrirDialogNovoProduto());
        this.add(btnNovo, BorderLayout.NORTH);

        modeloTabela = new DefaultTableModel(
                new Object[]{"ID", "Código", "Nome", "Unidade", "Quantidade", "Tipo", "Ações"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 6;
            }
        };
        tabela = new JTable(modeloTabela);
        tabela.getColumn("Ações").setCellRenderer(new ButtonRenderer());
        tabela.getColumn("Ações").setCellEditor(new ButtonEditor(new JCheckBox()));
        JScrollPane scroll = new JScrollPane(tabela);
        this.add(scroll, BorderLayout.CENTER);

        carregarProdutos();
        setVisible(true);
    }

    private void carregarProdutos() {
        modeloTabela.setRowCount(0);
        try (Connection conn = Conexao.conectar()) {
            String sql = "SELECT * FROM produtos_estoque";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                modeloTabela.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("id_custom"),
                        rs.getString("nome"),
                        rs.getString("unidade"),
                        rs.getDouble("quantidade"),
                        rs.getString("tipo"),
                        "Gerenciar"
                });
            }
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar produtos: " + e.getMessage());
        }
    }

    private void abrirDialogNovoProduto() {
        JDialog dialog = new JDialog(this, "Registrar Novo Produto", true);
        dialog.setSize(400, 350);
        dialog.setLayout(new GridLayout(6, 2, 10,10));
        dialog.setLocationRelativeTo(this);

        JTextField txtCodigo = new JTextField();
        JTextField txtNome = new JTextField();
        JTextField txtUnidade = new JTextField();
        JTextField txtQuantidade = new JTextField();
        JComboBox<String> cmbTipo = new JComboBox<>(new String[]{"INGREDIENTE", "EMBALAGEM"});

        dialog.add(new JLabel("Código:"));
        dialog.add(txtCodigo);
        dialog.add(new JLabel("Nome:"));
        dialog.add(txtNome);
        dialog.add(new JLabel("Unidade:"));
        dialog.add(txtUnidade);
        dialog.add(new JLabel("Quantidade:"));
        dialog.add(txtQuantidade);
        dialog.add(new JLabel("Tipo:"));
        dialog.add(cmbTipo);

        JButton btnSalvar = new JButton("Salvar");
        btnSalvar.setBackground(Color.decode("#491a00"));
        btnSalvar.setForeground(Color.WHITE);
        btnSalvar.setFocusPainted(false);
        btnSalvar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSalvar.addActionListener(e -> {
            String codigo = txtCodigo.getText();
            String nome = txtNome.getText();
            String unidade = txtUnidade.getText();
            double quantidade;
            try {
                quantidade = Double.parseDouble(txtQuantidade.getText());
            } catch(Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Quantidade inválida.");
                return;
            }
            String tipo = (String)cmbTipo.getSelectedItem();
            try (Connection conn = Conexao.conectar()) {
                String sql = "INSERT INTO produtos_estoque (id_custom, nome, unidade, quantidade, tipo) VALUES (?,?,?,?,?)";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, codigo);
                stmt.setString(2, nome);
                stmt.setString(3, unidade);
                stmt.setDouble(4, quantidade);
                stmt.setString(5, tipo);
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(dialog, "Produto cadastrado!");
                carregarProdutos();
                dialog.dispose();
            } catch(Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Erro ao salvar: " + ex.getMessage());
            }
        });

        dialog.add(new JLabel());
        dialog.add(btnSalvar);
        dialog.setVisible(true);
    }

    private void abrirDialogAcoes(ProdutoEstoque produto) {
        JDialog dialog = new JDialog(this, "Gerenciar Produto", true);
        dialog.setSize(400, 350);
        dialog.setLayout(new GridLayout(7, 2, 10,10));
        dialog.setLocationRelativeTo(this);

        JTextField txtNome = new JTextField(produto.getNome());
        JTextField txtUnidade = new JTextField(produto.getUnidade());
        JComboBox<String> cmbTipo = new JComboBox<>(new String[]{"INGREDIENTE", "EMBALAGEM"});
        cmbTipo.setSelectedItem(produto.getTipo());
        JLabel lblQtd = new JLabel(String.valueOf(produto.getQuantidade()));

        dialog.add(new JLabel("Nome:")); dialog.add(txtNome);
        dialog.add(new JLabel("Unidade:")); dialog.add(txtUnidade);
        dialog.add(new JLabel("Tipo:")); dialog.add(cmbTipo);
        dialog.add(new JLabel("Quantidade Atual:")); dialog.add(lblQtd);

        JButton btnSalvar = new JButton("Salvar Alterações");
        btnSalvar.setBackground(Color.decode("#491a00"));
        btnSalvar.setForeground(Color.WHITE);
        btnSalvar.setFocusPainted(false);
        btnSalvar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSalvar.addActionListener(e -> {
            produto.setNome(txtNome.getText());
            produto.setUnidade(txtUnidade.getText());
            produto.setTipo((String)cmbTipo.getSelectedItem());
            try (Connection conn = Conexao.conectar()) {
                String sql = "UPDATE produtos_estoque SET nome=?, unidade=?, tipo=? WHERE id=?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, produto.getNome());
                stmt.setString(2, produto.getUnidade());
                stmt.setString(3, produto.getTipo());
                stmt.setInt(4, produto.getId());
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(dialog, "Produto atualizado.");
                carregarProdutos();
                dialog.dispose();
            } catch(Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Erro: " + ex.getMessage());
            }
        });

        JButton btnEntrada = new JButton("Adicionar Entrada");
        btnEntrada.setBackground(Color.decode("#491a00"));
        btnEntrada.setForeground(Color.WHITE);
        btnEntrada.setFocusPainted(false);
        btnEntrada.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnEntrada.addActionListener(e -> {
            JPanel painel = new JPanel(new GridLayout(3, 2, 5,5));
            JTextField txtQtd = new JTextField();
            JTextField txtNotaFiscal = new JTextField();
            JTextField txtData = new JTextField(new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
            painel.add(new JLabel("Quantidade:"));
            painel.add(txtQtd);
            painel.add(new JLabel("Nota Fiscal:"));
            painel.add(txtNotaFiscal);
            painel.add(new JLabel("Data Entrada (dd/MM/yyyy):"));
            painel.add(txtData);
            int opt = JOptionPane.showConfirmDialog(dialog, painel, "Adicionar Entrada", JOptionPane.OK_CANCEL_OPTION);
            if (opt == JOptionPane.OK_OPTION) {
                try {
                    double qtd = Double.parseDouble(txtQtd.getText());
                    String notaFiscal = txtNotaFiscal.getText();
                    Date data;
                    try {
                        data = new SimpleDateFormat("dd/MM/yyyy").parse(txtData.getText());
                    } catch(Exception ex) {
                        JOptionPane.showMessageDialog(dialog, "Data inválida.");
                        return;
                    }
                    double novaQtd = produto.getQuantidade() + qtd;
                    try (Connection conn = Conexao.conectar()) {
                        String sql = "UPDATE produtos_estoque SET quantidade=? WHERE id=?";
                        PreparedStatement stmt = conn.prepareStatement(sql);
                        stmt.setDouble(1, novaQtd);
                        stmt.setInt(2, produto.getId());
                        stmt.executeUpdate();
                        sql = "INSERT INTO entradas_estoque (produto_id, quantidade, nota_fiscal, data_entrada) VALUES (?,?,?,?)";
                        PreparedStatement stmt2 = conn.prepareStatement(sql);
                        stmt2.setInt(1, produto.getId());
                        stmt2.setDouble(2, qtd);
                        stmt2.setString(3, notaFiscal);
                        stmt2.setDate(4, new java.sql.Date(data.getTime()));
                        stmt2.executeUpdate();
                        JOptionPane.showMessageDialog(dialog, "Entrada registrada!");
                        carregarProdutos();
                    }
                } catch(Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "Erro: " + ex.getMessage());
                }
            }
        });

        JButton btnSaida = new JButton("Retirar Estoque");
        btnSaida.setBackground(Color.decode("#491a00"));
        btnSaida.setForeground(Color.WHITE);
        btnSaida.setFocusPainted(false);
        btnSaida.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSaida.addActionListener(e -> {
            String qtdStr = JOptionPane.showInputDialog(dialog, "Quantidade a retirar:");
            if (qtdStr != null && !qtdStr.isEmpty()) {
                try {
                    double qtd = Double.parseDouble(qtdStr);
                    if (qtd > produto.getQuantidade()) {
                        JOptionPane.showMessageDialog(dialog, "Estoque insuficiente.");
                        return;
                    }
                    double novaQtd = produto.getQuantidade() - qtd;
                    try (Connection conn = Conexao.conectar()) {
                        String sql = "UPDATE produtos_estoque SET quantidade=? WHERE id=?";
                        PreparedStatement stmt = conn.prepareStatement(sql);
                        stmt.setDouble(1, novaQtd);
                        stmt.setInt(2, produto.getId());
                        stmt.executeUpdate();
                        JOptionPane.showMessageDialog(dialog, "Retirada feita!");
                        carregarProdutos();
                    }
                } catch(Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "Erro: " + ex.getMessage());
                }
            }
        });

        JButton btnVisualizarEntradas = new JButton("Visualizar Entradas");
        btnVisualizarEntradas.setBackground(Color.decode("#491a00"));
        btnVisualizarEntradas.setForeground(Color.WHITE);
        btnVisualizarEntradas.setFocusPainted(false);
        btnVisualizarEntradas.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnVisualizarEntradas.addActionListener(e -> {
            JDialog historico = new JDialog(dialog, "Histórico de Entradas", true);
            historico.setSize(500, 400);
            historico.setLocationRelativeTo(dialog);
            DefaultTableModel modeloHist = new DefaultTableModel(
                    new Object[]{"Quantidade", "Nota Fiscal", "Data"}, 0
            );
            JTable tabelaHist = new JTable(modeloHist);
            try (Connection conn = Conexao.conectar()) {
                String sql = "SELECT * FROM entradas_estoque WHERE produto_id=? ORDER BY data_entrada DESC";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setInt(1, produto.getId());
                ResultSet rs = stmt.executeQuery();
                while(rs.next()) {
                    modeloHist.addRow(new Object[]{
                            rs.getDouble("quantidade"),
                            rs.getString("nota_fiscal"),
                            new SimpleDateFormat("dd/MM/yyyy").format(rs.getDate("data_entrada"))
                    });
                }
                rs.close();
            } catch(Exception ex) {
                JOptionPane.showMessageDialog(historico, "Erro: " + ex.getMessage());
            }
            historico.add(new JScrollPane(tabelaHist));
            historico.setVisible(true);
        });

        JButton btnExcluir = new JButton("Excluir Produto");
        btnExcluir.setBackground(Color.decode("#491a00"));
        btnExcluir.setForeground(Color.WHITE);
        btnExcluir.setFocusPainted(false);
        btnExcluir.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnExcluir.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(dialog, "Tem certeza que deseja excluir?", "Excluir", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                try (Connection conn = Conexao.conectar()) {
                    String sql = "DELETE FROM produtos_estoque WHERE id=?";
                    PreparedStatement stmt = conn.prepareStatement(sql);
                    stmt.setInt(1, produto.getId());
                    stmt.executeUpdate();
                    JOptionPane.showMessageDialog(dialog, "Produto excluído!");
                    carregarProdutos();
                    dialog.dispose();
                } catch(Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "Erro: " + ex.getMessage());
                }
            }
        });

        dialog.add(btnSalvar);
        dialog.add(btnEntrada);
        dialog.add(btnSaida);
        dialog.add(btnVisualizarEntradas);
        dialog.add(btnExcluir);
        dialog.setVisible(true);
    }

    class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() {
            setText("Gerenciar");
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
        private boolean clicked;
        private int row;
        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton("Gerenciar");
            button.setBackground(Color.decode("#491a00"));
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
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
                ProdutoEstoque p = new ProdutoEstoque();
                p.setId((int)modeloTabela.getValueAt(row,0));
                p.setIdCustom((String)modeloTabela.getValueAt(row,1));
                p.setNome((String)modeloTabela.getValueAt(row,2));
                p.setUnidade((String)modeloTabela.getValueAt(row,3));
                p.setQuantidade((double)modeloTabela.getValueAt(row,4));
                p.setTipo((String)modeloTabela.getValueAt(row,5));
                abrirDialogAcoes(p);
            }
            clicked = false;
            return "Gerenciar";
        }
        @Override
        public boolean stopCellEditing() {
            clicked = false;
            return super.stopCellEditing();
        }
    }

}
