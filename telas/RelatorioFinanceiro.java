package telas;

import conexao.Conexao;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.math.BigDecimal;

public class RelatorioFinanceiro extends JFrame {

    private JTabbedPane abas;
    private JTable tabelaEntradas;
    private JTable tabelaSaidas;
    private DefaultTableModel modeloEntradas;
    private DefaultTableModel modeloSaidas;
    private JLabel lblResumo;
    private SimpleDateFormat formatoTelaEntrada = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private SimpleDateFormat formatoTelaSaida = new SimpleDateFormat("dd/MM/yyyy");

    public RelatorioFinanceiro() {
        setTitle("Relatórios Financeiros");
        setSize(1200, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10,10));

        abas = new JTabbedPane();

        modeloEntradas = new DefaultTableModel(
                new Object[]{"ID", "Cliente", "Status", "Data Pedido", "Valor Total"},
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        tabelaEntradas = new JTable(modeloEntradas);
        JScrollPane scrollEntradas = new JScrollPane(tabelaEntradas);
        abas.add("Entradas (Pedidos Prontos)", scrollEntradas);

        modeloSaidas = new DefaultTableModel(
                new Object[]{"ID", "Descrição", "Tipo", "Valor", "Status", "Data Vencimento", "Data Pagamento", "Editar"},
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 7;
            }
        };
        tabelaSaidas = new JTable(modeloSaidas);
        tabelaSaidas.getColumn("Editar").setCellRenderer(new ButtonRenderer());
        tabelaSaidas.getColumn("Editar").setCellEditor(new ButtonEditor(new JCheckBox()));
        JScrollPane scrollSaidas = new JScrollPane(tabelaSaidas);
        abas.add("Saídas (Contas PENDENTES/PAGAS)", scrollSaidas);

        add(abas, BorderLayout.CENTER);

        lblResumo = new JLabel("Entradas: R$ 0,00 | Saídas: R$ 0,00");
        lblResumo.setFont(new Font("Arial", Font.BOLD, 14));
        JPanel painelRodape = new JPanel(new FlowLayout(FlowLayout.LEFT));
        painelRodape.add(lblResumo);
        add(painelRodape, BorderLayout.SOUTH);

        carregarRelatorio();

        setVisible(true);
    }

    private void carregarRelatorio() {
        modeloEntradas.setRowCount(0);
        modeloSaidas.setRowCount(0);

        BigDecimal totalEntradas = BigDecimal.ZERO;
        BigDecimal totalSaidas = BigDecimal.ZERO;

        try (Connection conn = Conexao.conectar()) {
            String sqlPedidos = """
                SELECT 
                    p.id, p.cliente_nome, p.status, p.data_pedido,
                    IFNULL(SUM(ip.quantidade * c.preco), 0) AS valor_total
                FROM pedidos p
                LEFT JOIN itens_pedido ip ON p.id = ip.pedido_id
                LEFT JOIN cardapio c ON ip.cardapio_id = c.id
                WHERE p.status = 'PRONTO'
                GROUP BY p.id, p.cliente_nome, p.status, p.data_pedido
                ORDER BY p.data_pedido DESC
            """;

            PreparedStatement ps = conn.prepareStatement(sqlPedidos);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String cliente = rs.getString("cliente_nome");
                String status = rs.getString("status");
                Timestamp dataPedido = rs.getTimestamp("data_pedido");
                BigDecimal valorTotal = rs.getBigDecimal("valor_total");

                modeloEntradas.addRow(new Object[]{
                        id,
                        cliente,
                        status,
                        formatoTelaEntrada.format(dataPedido),
                        String.format("R$ %.2f", valorTotal.doubleValue())
                });

                if (valorTotal != null)
                    totalEntradas = totalEntradas.add(valorTotal);
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar pedidos: " + e.getMessage());
        }

        try (Connection conn = Conexao.conectar()) {
            String sqlContas = "SELECT * FROM contas WHERE status IN ('PENDENTE', 'PAGO') ORDER BY data_vencimento DESC";
            PreparedStatement ps = conn.prepareStatement(sqlContas);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String descricao = rs.getString("descricao");
                String tipo = rs.getString("tipo");
                BigDecimal valor = rs.getBigDecimal("valor");
                String status = rs.getString("status");
                Date dataVencimento = rs.getDate("data_vencimento");
                Date dataPagamento = rs.getDate("data_pagamento");

                modeloSaidas.addRow(new Object[]{
                        id,
                        descricao,
                        tipo,
                        valor,
                        status,
                        dataVencimento != null ? formatoTelaSaida.format(dataVencimento) : "",
                        dataPagamento != null ? formatoTelaSaida.format(dataPagamento) : "",
                        "Editar"
                });

                if (valor != null)
                    totalSaidas = totalSaidas.add(valor);
            }
            rs.close();
            ps.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar contas: " + e.getMessage());
        }

        lblResumo.setText(String.format(
                "Entradas: R$ %.2f | Saídas: R$ %.2f",
                totalEntradas, totalSaidas
        ));
    }

    private void abrirDialogDetalhesConta(int idConta) {
        try (Connection conn = Conexao.conectar()) {
            String sql = "SELECT * FROM contas WHERE id = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, idConta);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String descricao = rs.getString("descricao");
                String tipo = rs.getString("tipo");
                BigDecimal valor = rs.getBigDecimal("valor");
                String status = rs.getString("status");
                Date dataVencimento = rs.getDate("data_vencimento");
                Date dataPagamento = rs.getDate("data_pagamento");

                rs.close();
                stmt.close();

                JDialog dialog = new JDialog(this, "Editar Conta", true);
                dialog.setSize(400, 350);
                dialog.setLayout(new GridLayout(9,2,10,10));
                dialog.setLocationRelativeTo(this);

                JTextField txtDescricao = new JTextField(descricao);
                JTextField txtTipo = new JTextField(tipo);
                JTextField txtValor = new JTextField(valor.toString());
                JComboBox<String> cmbStatus = new JComboBox<>(new String[]{"PENDENTE", "PAGO"});
                cmbStatus.setSelectedItem(status);

                JTextField txtDataVenc = new JTextField(dataVencimento != null ? formatoTelaSaida.format(dataVencimento) : "");
                txtDataVenc.setEditable(false);

                JTextField txtDia = new JTextField(2);
                JTextField txtMes = new JTextField(2);
                JTextField txtAno = new JTextField(4);

                if (dataPagamento != null) {
                    String[] partes = new SimpleDateFormat("dd/MM/yyyy").format(dataPagamento).split("/");
                    txtDia.setText(partes[0]);
                    txtMes.setText(partes[1]);
                    txtAno.setText(partes[2]);
                }

                dialog.add(new JLabel("Descrição:")); dialog.add(txtDescricao);
                dialog.add(new JLabel("Tipo:")); dialog.add(txtTipo);
                dialog.add(new JLabel("Valor:")); dialog.add(txtValor);
                dialog.add(new JLabel("Status:")); dialog.add(cmbStatus);
                dialog.add(new JLabel("Data Vencimento:")); dialog.add(txtDataVenc);
                dialog.add(new JLabel("Data Pagamento (DD MM AAAA):"));
                JPanel painelDataPag = new JPanel(new FlowLayout(FlowLayout.LEFT,5,0));
                painelDataPag.add(txtDia);
                painelDataPag.add(new JLabel("/"));
                painelDataPag.add(txtMes);
                painelDataPag.add(new JLabel("/"));
                painelDataPag.add(txtAno);
                dialog.add(painelDataPag);

                JButton btnSalvar = new JButton("Salvar");
                btnSalvar.setBackground(Color.decode("#491a00"));
                btnSalvar.setForeground(Color.WHITE);
                btnSalvar.setFocusPainted(false);

                JButton btnFechar = new JButton("Fechar");
                btnFechar.setBackground(Color.decode("#491a00"));
                btnFechar.setForeground(Color.WHITE);
                btnFechar.setFocusPainted(false);

                JPanel painelBotoes = new JPanel();
                painelBotoes.add(btnSalvar);
                painelBotoes.add(btnFechar);

                dialog.add(new JLabel()); 
                dialog.add(painelBotoes);

                btnFechar.addActionListener(ev -> dialog.dispose());

                btnSalvar.addActionListener(ev -> {
                    String novoStatus = (String) cmbStatus.getSelectedItem();
                    String d = txtDia.getText().trim();
                    String m = txtMes.getText().trim();
                    String a = txtAno.getText().trim();
                    Date novaDataPagamento = null;
                    if (!d.isEmpty() && !m.isEmpty() && !a.isEmpty()) {
                        try {
                            novaDataPagamento = new SimpleDateFormat("dd/MM/yyyy").parse(d+"/"+m+"/"+a);
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(dialog, "Data inválida");
                            return;
                        }
                    }
                    try (Connection conn2 = Conexao.conectar()) {
                        String sqlUpdate = "UPDATE contas SET status=?, data_pagamento=? WHERE id=?";
                        PreparedStatement psUpdate = conn2.prepareStatement(sqlUpdate);
                        psUpdate.setString(1, novoStatus);
                        if (novaDataPagamento != null)
                            psUpdate.setDate(2, new java.sql.Date(novaDataPagamento.getTime()));
                        else
                            psUpdate.setNull(2, Types.DATE);
                        psUpdate.setInt(3, idConta);
                        psUpdate.executeUpdate();
                        psUpdate.close();
                        JOptionPane.showMessageDialog(dialog, "Atualizado com sucesso!");
                        dialog.dispose();
                        carregarRelatorio();
                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(dialog, "Erro ao atualizar: " + ex.getMessage());
                    }
                });

                dialog.setVisible(true);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao buscar conta: " + e.getMessage());
        }
    }

    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setText("Editar");
            setBackground(Color.decode("#491a00"));
            setForeground(Color.WHITE);
            setFocusPainted(false);
        }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        protected JButton button;
        private boolean clicado;
        private int linha;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton("Editar");
            button.setOpaque(true);
            button.setBackground(Color.decode("#491a00"));
            button.setForeground(Color.WHITE);
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int col) {
            linha = row;
            clicado = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (clicado) {
                int idConta = (int) modeloSaidas.getValueAt(linha, 0);
                abrirDialogDetalhesConta(idConta);
            }
            clicado = false;
            return "Editar";
        }

        @Override
        public boolean stopCellEditing() {
            clicado = false;
            return super.stopCellEditing();
        }
    }
}

