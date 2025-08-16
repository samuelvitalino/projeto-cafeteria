package telas;

import classes.Pedido;
import conexao.Conexao;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.text.SimpleDateFormat;

public class ConsultarPedido extends JFrame {

    private JTabbedPane abas;
    private JTable tabelaEmPreparo, tabelaConcluidos;
    private DefaultTableModel modeloEmPreparo, modeloConcluidos;

    public ConsultarPedido() {
        setTitle("Consultar Pedidos");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        abas = new JTabbedPane();

        modeloEmPreparo = new DefaultTableModel(
                new Object[]{"ID", "Código", "Cliente", "Status", "Data", "Ações"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5;             }
        };
        tabelaEmPreparo = new JTable(modeloEmPreparo);
        tabelaEmPreparo.getColumn("Ações").setCellRenderer(new ButtonRenderer());
        tabelaEmPreparo.getColumn("Ações").setCellEditor(new ButtonEditor(new JCheckBox()));
        abas.add("Em Preparo", new JScrollPane(tabelaEmPreparo));

        modeloConcluidos = new DefaultTableModel(
                new Object[]{"ID", "Código", "Cliente", "Status", "Data"}, 0
        ) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabelaConcluidos = new JTable(modeloConcluidos);
        abas.add("Concluídos / Cancelados", new JScrollPane(tabelaConcluidos));

        add(abas, BorderLayout.CENTER);

        carregarPedidos();

        setVisible(true);
    }

    private void carregarPedidos() {
        modeloEmPreparo.setRowCount(0);
        modeloConcluidos.setRowCount(0);

        try (Connection conn = Conexao.conectar()) {
            String sql = "SELECT * FROM pedidos ORDER BY data_pedido DESC";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

            while (rs.next()) {
                int id = rs.getInt("id");
                String idCustom = rs.getString("id_custom");
                String cliente = rs.getString("cliente_nome");
                String status = rs.getString("status");
                Timestamp dataPedido = rs.getTimestamp("data_pedido");
                String dataFormatada = sdf.format(dataPedido);

                if ("EM PREPARO".equals(status)) {
                    modeloEmPreparo.addRow(new Object[]{id, idCustom, cliente, status, dataFormatada, "Editar Status"});
                } else {
                    modeloConcluidos.addRow(new Object[]{id, idCustom, cliente, status, dataFormatada});
                }
            }

            rs.close();
            stmt.close();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar pedidos: " + e.getMessage());
        }
    }

    private void editarStatus(Pedido pedido) {
        String[] opcoes = {"EM PREPARO", "PRONTO", "CANCELADO"};
        String novoStatus = (String) JOptionPane.showInputDialog(
                this,
                "Selecione o novo status do pedido:",
                "Alterar Status",
                JOptionPane.PLAIN_MESSAGE,
                null,
                opcoes,
                pedido.getStatus()
        );

        if (novoStatus != null) {
            try (Connection conn = Conexao.conectar()) {
                String sql = "UPDATE pedidos SET status=? WHERE id=?";
                PreparedStatement stmt = conn.prepareStatement(sql);
                stmt.setString(1, novoStatus);
                stmt.setInt(2, pedido.getId());
                stmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Status atualizado com sucesso!");
                carregarPedidos();
            } catch (SQLException e) {
                JOptionPane.showMessageDialog(this, "Erro ao atualizar status: " + e.getMessage());
            }
        }
    }

    class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() {
            setText("Editar Status");
            setBackground(new Color(0, 102, 204)); 
            setForeground(Color.WHITE);
        }
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus,
                                                       int row, int column) {
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private int row;
        private boolean clicked;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton("Editar Status");
            button.setBackground(new Color(0, 102, 204));
            button.setForeground(Color.WHITE);
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            this.row = row;
            this.clicked = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (clicked) {
                Pedido p = new Pedido();
                p.setId((int) modeloEmPreparo.getValueAt(row, 0));
                p.setIdCustom((String) modeloEmPreparo.getValueAt(row, 1));
                p.setClienteNome((String) modeloEmPreparo.getValueAt(row, 2));
                p.setStatus((String) modeloEmPreparo.getValueAt(row, 3));
                editarStatus(p);
            }
            clicked = false;
            return "Editar Status";
        }

        @Override
        public boolean stopCellEditing() {
            clicked = false;
            return super.stopCellEditing();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ConsultarPedido::new);
    }
}
