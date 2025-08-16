package telas;

import conexao.Conexao;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.math.BigDecimal;

public class Saidas extends JFrame {

    private JTable tabelaSaidas;
    private DefaultTableModel modeloSaidas;
    private JButton btnNovaConta;
    private SimpleDateFormat formatoData = new SimpleDateFormat("dd/MM/yyyy");

    public Saidas() {
        setTitle("Contas Saídas - Gerenciamento");
        setSize(900, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        modeloSaidas = new DefaultTableModel(
                new Object[]{"ID", "Descrição", "Valor (R$)", "Status", "Data Vencimento", "Data Pagamento", "Editar"},
                0
        ) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return col == 6; 
            }
        };

        tabelaSaidas = new JTable(modeloSaidas);
        tabelaSaidas.getColumn("Editar").setCellRenderer(new ButtonRenderer());
        tabelaSaidas.getColumn("Editar").setCellEditor(new ButtonEditor(new JCheckBox()));

        JScrollPane scroll = new JScrollPane(tabelaSaidas);
        add(scroll, BorderLayout.CENTER);

        btnNovaConta = new JButton("Nova Conta");
        btnNovaConta.setBackground(Color.decode("#491a00"));
        btnNovaConta.setForeground(Color.WHITE);
        btnNovaConta.setFocusPainted(false);
        btnNovaConta.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnNovaConta.addActionListener(e -> abrirDialogNovaConta());

        JPanel painelTopo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        painelTopo.add(btnNovaConta);

        add(painelTopo, BorderLayout.NORTH);

        carregarSaidas();

        setVisible(true);
    }

    private void carregarSaidas() {
        modeloSaidas.setRowCount(0);

        try (Connection conn = Conexao.conectar()) {
            String sql = "SELECT * FROM contas WHERE tipo = 'PAGAR' ORDER BY data_vencimento ASC";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                String descricao = rs.getString("descricao");
                BigDecimal valor = rs.getBigDecimal("valor");
                String status = rs.getString("status");
                Date dataVenc = rs.getDate("data_vencimento");
                Date dataPag = rs.getDate("data_pagamento");

                modeloSaidas.addRow(new Object[]{
                        id,
                        descricao,
                        String.format("%.2f", valor),
                        status,
                        dataVenc != null ? formatoData.format(dataVenc) : "",
                        dataPag != null ? formatoData.format(dataPag) : "",
                        "Editar"
                });
            }

            rs.close();
            ps.close();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar contas: " + e.getMessage());
        }
    }

    private void abrirDialogNovaConta() {
        JDialog dialog = new JDialog(this, "Nova Conta Saída", true);
        dialog.setSize(400, 350);
        dialog.setLayout(new GridLayout(7, 2, 10, 10));
        dialog.setLocationRelativeTo(this);

        JTextField txtDescricao = new JTextField();
        JTextField txtValor = new JTextField();
        JComboBox<String> cmbStatus = new JComboBox<>(new String[]{"PENDENTE", "PAGO"});
        JTextField txtDataVenc = new JTextField();
        JTextField txtDia = new JTextField(2);
        JTextField txtMes = new JTextField(2);
        JTextField txtAno = new JTextField(4);

        dialog.add(new JLabel("Descrição:")); dialog.add(txtDescricao);
        dialog.add(new JLabel("Valor (R$):")); dialog.add(txtValor);
        dialog.add(new JLabel("Status:")); dialog.add(cmbStatus);
        dialog.add(new JLabel("Data Vencimento (dd/mm/aaaa):")); dialog.add(txtDataVenc);

        dialog.add(new JLabel("Data Pagamento (DD MM AAAA):"));
        JPanel painelDataPag = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
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

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setBackground(Color.decode("#491a00"));
        btnCancelar.setForeground(Color.WHITE);
        btnCancelar.setFocusPainted(false);

        dialog.add(btnSalvar);
        dialog.add(btnCancelar);

        btnCancelar.addActionListener(e -> dialog.dispose());

        btnSalvar.addActionListener(e -> {
            String descricao = txtDescricao.getText().trim();
            String valorStr = txtValor.getText().trim();
            String status = (String) cmbStatus.getSelectedItem();
            String dataVencStr = txtDataVenc.getText().trim();

            if (descricao.isEmpty() || valorStr.isEmpty() || dataVencStr.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Preencha todos os campos obrigatórios.");
                return;
            }

            BigDecimal valor;
            try {
                valor = new BigDecimal(valorStr.replace(",", "."));
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Valor inválido.");
                return;
            }

            Date dataVenc = null;
            try {
                dataVenc = formatoData.parse(dataVencStr + " 00:00");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Data de vencimento inválida. Use dd/MM/yyyy");
                return;
            }

            Date dataPag = null;
            String d = txtDia.getText().trim();
            String m = txtMes.getText().trim();
            String a = txtAno.getText().trim();

            if (!d.isEmpty() && !m.isEmpty() && !a.isEmpty()) {
                try {
                    dataPag = new SimpleDateFormat("dd/MM/yyyy").parse(d + "/" + m + "/" + a);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(dialog, "Data de pagamento inválida.");
                    return;
                }
            }

            try (Connection conn = Conexao.conectar()) {
                String sql = "INSERT INTO contas (tipo, descricao, valor, status, data_vencimento, data_pagamento) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, "PAGAR");
                ps.setString(2, descricao);
                ps.setBigDecimal(3, valor);
                ps.setString(4, status);
                ps.setDate(5, new java.sql.Date(dataVenc.getTime()));
                if (dataPag != null) {
                    ps.setDate(6, new java.sql.Date(dataPag.getTime()));
                } else {
                    ps.setNull(6, Types.DATE);
                }
                ps.executeUpdate();
                ps.close();

                JOptionPane.showMessageDialog(dialog, "Conta cadastrada com sucesso!");
                dialog.dispose();
                carregarSaidas();

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dialog, "Erro ao salvar conta: " + ex.getMessage());
            }
        });

        dialog.setVisible(true);
    }

    private void abrirDialogEditarConta(int idConta) {
        try (Connection conn = Conexao.conectar()) {
            String sql = "SELECT * FROM contas WHERE id = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, idConta);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String descricao = rs.getString("descricao");
                BigDecimal valor = rs.getBigDecimal("valor");
                String status = rs.getString("status");
                Date dataVenc = rs.getDate("data_vencimento");
                Date dataPag = rs.getDate("data_pagamento");

                rs.close();
                ps.close();

                JDialog dialog = new JDialog(this, "Editar Conta Saída", true);
                dialog.setSize(400, 350);
                dialog.setLayout(new GridLayout(7, 2, 10, 10));
                dialog.setLocationRelativeTo(this);

                JTextField txtDescricao = new JTextField(descricao);
                JTextField txtValor = new JTextField(valor.toString());
                JComboBox<String> cmbStatus = new JComboBox<>(new String[]{"PENDENTE", "PAGO"});
                cmbStatus.setSelectedItem(status);
                JTextField txtDataVenc = new JTextField(dataVenc != null ? formatoData.format(dataVenc) : "");
                txtDataVenc.setEditable(false);

                JTextField txtDia = new JTextField(2);
                JTextField txtMes = new JTextField(2);
                JTextField txtAno = new JTextField(4);

                if (dataPag != null) {
                    String[] partes = new SimpleDateFormat("dd/MM/yyyy").format(dataPag).split("/");
                    txtDia.setText(partes[0]);
                    txtMes.setText(partes[1]);
                    txtAno.setText(partes[2]);
                }

                dialog.add(new JLabel("Descrição:")); dialog.add(txtDescricao);
                dialog.add(new JLabel("Valor (R$):")); dialog.add(txtValor);
                dialog.add(new JLabel("Status:")); dialog.add(cmbStatus);
                dialog.add(new JLabel("Data Vencimento:")); dialog.add(txtDataVenc);
                dialog.add(new JLabel("Data Pagamento (DD MM AAAA):"));
                JPanel painelDataPag = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
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

                JButton btnCancelar = new JButton("Cancelar");
                btnCancelar.setBackground(Color.decode("#491a00"));
                btnCancelar.setForeground(Color.WHITE);
                btnCancelar.setFocusPainted(false);

                dialog.add(btnSalvar);
                dialog.add(btnCancelar);

                btnCancelar.addActionListener(e -> dialog.dispose());

                btnSalvar.addActionListener(e -> {
                    String novaDescricao = txtDescricao.getText().trim();
                    String valorStr = txtValor.getText().trim();
                    String novoStatus = (String) cmbStatus.getSelectedItem();

                    if (novaDescricao.isEmpty() || valorStr.isEmpty()) {
                        JOptionPane.showMessageDialog(dialog, "Preencha todos os campos obrigatórios.");
                        return;
                    }

                    BigDecimal novoValor;
                    try {
                        novoValor = new BigDecimal(valorStr.replace(",", "."));
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(dialog, "Valor inválido.");
                        return;
                    }

                    Date novaDataPagamento = null;
                    String d = txtDia.getText().trim();
                    String m = txtMes.getText().trim();
                    String a = txtAno.getText().trim();

                    if (!d.isEmpty() && !m.isEmpty() && !a.isEmpty()) {
                        try {
                            novaDataPagamento = new SimpleDateFormat("dd/MM/yyyy").parse(d + "/" + m + "/" + a);
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(dialog, "Data de pagamento inválida.");
                            return;
                        }
                    }

                    try (Connection conn2 = Conexao.conectar()) {
                        String sqlUpdate = "UPDATE contas SET descricao=?, valor=?, status=?, data_pagamento=? WHERE id=?";
                        PreparedStatement psUpdate = conn2.prepareStatement(sqlUpdate);
                        psUpdate.setString(1, novaDescricao);
                        psUpdate.setBigDecimal(2, novoValor);
                        psUpdate.setString(3, novoStatus);

                        if (novaDataPagamento != null) {
                            psUpdate.setDate(4, new java.sql.Date(novaDataPagamento.getTime()));
                        } else {
                            psUpdate.setNull(4, Types.DATE);
                        }

                        psUpdate.setInt(5, idConta);
                        psUpdate.executeUpdate();
                        psUpdate.close();

                        JOptionPane.showMessageDialog(dialog, "Conta atualizada com sucesso!");
                        dialog.dispose();
                        carregarSaidas();

                    } catch (SQLException ex) {
                        JOptionPane.showMessageDialog(dialog, "Erro ao atualizar conta: " + ex.getMessage());
                    }
                });

                dialog.setVisible(true);

            } else {
                JOptionPane.showMessageDialog(this, "Conta não encontrada.");
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
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                                                       boolean hasFocus, int row, int column) {
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
            button.setFocusPainted(false);
            button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            linha = row;
            clicado = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (clicado) {
                int idConta = (int) modeloSaidas.getValueAt(linha, 0);
                abrirDialogEditarConta(idConta);
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
