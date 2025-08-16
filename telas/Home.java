package telas;

import conexao.Conexao;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Home extends JFrame {

    private JLabel lblData;
    private JLabel lblInfo1;
    private JLabel lblInfo2;
    private JTable tabela;
    private DefaultTableModel modeloTabela;

    private JLabel lblEntradasTotal;
    private JLabel lblSaidasTotal;
    private DefaultTableModel modeloEntradas;
    private DefaultTableModel modeloSaidas;

    public Home(String tipoUsuario, String nomeUsuario) {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Café & Brisa - Home");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(true);
        setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Color.decode("#491a00"));
        header.setPreferredSize(new Dimension(0, 80));

        JLabel logo = new JLabel("CB", SwingConstants.CENTER);
        logo.setOpaque(true);
        logo.setBackground(Color.decode("#491a00"));
        logo.setForeground(Color.WHITE);
        logo.setFont(new Font("Arial", Font.BOLD, 24));
        logo.setPreferredSize(new Dimension(60, 60));
        logo.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
        logo.setUI(new javax.swing.plaf.basic.BasicLabelUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setClip(new java.awt.geom.Ellipse2D.Float(0, 0, c.getWidth(), c.getHeight()));
                super.paint(g2, c);
            }
        });

        JLabel nomeSistema = new JLabel(" Café & Brisa ");
        nomeSistema.setForeground(Color.WHITE);
        nomeSistema.setFont(new Font("Arial", Font.BOLD, 28));

        JPanel painelEsquerda = new JPanel(new FlowLayout(FlowLayout.LEFT));
        painelEsquerda.setOpaque(false);
        painelEsquerda.add(logo);
        painelEsquerda.add(nomeSistema);

        JPanel painelDireita = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        painelDireita.setOpaque(false);

        JButton btnUsuarios = new JButton("Gerenciar Usuários");
        JButton btnRelatorios = new JButton("Relatórios Financeiros");
        JButton btnEstoque = new JButton("Controle de Estoque");
        JButton btnCardapio = new JButton("Gerenciar Cardápio");
        JButton btnSaidas = new JButton("Saídas");
        JButton btnSair = new JButton("Sair");
        btnSair.setBackground(Color.RED);
        btnSair.setForeground(Color.WHITE);

        switch (tipoUsuario.toLowerCase()) {
            case "admin":
                painelDireita.add(btnUsuarios);
                painelDireita.add(btnRelatorios);
                painelDireita.add(btnEstoque);
                painelDireita.add(btnCardapio);

                btnUsuarios.addActionListener(e -> new GerenciarDados());
                btnRelatorios.addActionListener(e -> new RelatorioFinanceiro());
                btnEstoque.addActionListener(e -> new Estoque());
                btnCardapio.addActionListener(e -> new GerenciarCardapio());
                break;

            case "financeiro":
                painelDireita.add(btnSaidas);
                painelDireita.add(btnRelatorios);

                btnSaidas.addActionListener(e -> new Saidas());
                btnRelatorios.addActionListener(e -> new RelatorioFinanceiro());
                break;

            case "atendente":
                break;

            default:
                painelDireita.add(new JLabel("Perfil desconhecido"));
        }

        painelDireita.add(btnSair);
        btnSair.addActionListener(e -> System.exit(0));

        header.add(painelEsquerda, BorderLayout.WEST);
        header.add(painelDireita, BorderLayout.EAST);
        add(header, BorderLayout.NORTH);

        JPanel painelCentral = new JPanel(new BorderLayout());
        painelCentral.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        lblData = new JLabel();
        lblData.setFont(new Font("Arial", Font.PLAIN, 16));
        lblData.setHorizontalAlignment(SwingConstants.LEFT);
        painelCentral.add(lblData, BorderLayout.NORTH);

        JPanel conteudo = new JPanel();
        painelCentral.add(conteudo, BorderLayout.CENTER);

        if (tipoUsuario.equalsIgnoreCase("admin")) {
            conteudo.setLayout(new BorderLayout(10, 10));

            JPanel painelInfoAdmin = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 5));
            lblInfo1 = new JLabel();
            lblInfo2 = new JLabel();
            painelInfoAdmin.add(lblInfo1);
            painelInfoAdmin.add(lblInfo2);
            conteudo.add(painelInfoAdmin, BorderLayout.NORTH);

            modeloTabela = new DefaultTableModel(new String[]{"ID Pedido", "Cliente", "Status", "Data"}, 0);
            tabela = new JTable(modeloTabela);
            JScrollPane scroll = new JScrollPane(tabela);
            conteudo.add(scroll, BorderLayout.CENTER);
        }
        else if (tipoUsuario.equalsIgnoreCase("financeiro")) {
            conteudo.setLayout(new BorderLayout(10, 10));

            JTabbedPane abas = new JTabbedPane();

            JPanel painelEntradas = new JPanel(new BorderLayout());
            lblEntradasTotal = new JLabel();
            painelEntradas.add(lblEntradasTotal, BorderLayout.NORTH);
            modeloEntradas = new DefaultTableModel(new String[]{"ID Pedido", "Cliente", "Valor", "Data"}, 0);
            JTable tabelaEntradas = new JTable(modeloEntradas);
            painelEntradas.add(new JScrollPane(tabelaEntradas), BorderLayout.CENTER);
            abas.addTab("Entradas", painelEntradas);

            JPanel painelSaidas = new JPanel(new BorderLayout());
            lblSaidasTotal = new JLabel();
            painelSaidas.add(lblSaidasTotal, BorderLayout.NORTH);
            modeloSaidas = new DefaultTableModel(new String[]{"ID", "Descrição", "Valor", "Data Pagamento"}, 0);
            JTable tabelaSaidas = new JTable(modeloSaidas);
            painelSaidas.add(new JScrollPane(tabelaSaidas), BorderLayout.CENTER);
            abas.addTab("Saídas", painelSaidas);

            conteudo.add(abas, BorderLayout.CENTER);
        }
        else if (tipoUsuario.equalsIgnoreCase("atendente")) {
            conteudo.setLayout(new FlowLayout(FlowLayout.CENTER, 50, 100));

            JButton btnNovoPedido = new JButton("Novo Pedido");
            JButton btnConsultarPedido = new JButton("Consultar Pedido");
            btnNovoPedido.setPreferredSize(new Dimension(150, 60));
            btnConsultarPedido.setPreferredSize(new Dimension(150, 60));

            conteudo.add(btnNovoPedido);
            conteudo.add(btnConsultarPedido);

            btnNovoPedido.addActionListener(e -> new NovoPedido());
            btnConsultarPedido.addActionListener(e -> new ConsultarPedido());
        }
        else {
            conteudo.add(new JLabel("Perfil desconhecido"));
        }

        add(painelCentral, BorderLayout.CENTER);
        setVisible(true);

        carregarDados(tipoUsuario.toLowerCase());
    }

    private void carregarDados(String tipoUsuario) {
        lblData.setText("Data: " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
        try (Connection conn = Conexao.conectar()) {
            if (conn == null) {
                JOptionPane.showMessageDialog(this, "Erro de conexão!");
                return;
            }

            if (tipoUsuario.equals("admin")) {
                String sqlTotalPedidos = """
                    SELECT COUNT(*) FROM pedidos
                    WHERE DATE(data_pedido) = CURDATE() AND status = 'PRONTO'
                    """;
                PreparedStatement psTotal = conn.prepareStatement(sqlTotalPedidos);
                ResultSet rsTotal = psTotal.executeQuery();
                if (rsTotal.next()) {
                    lblInfo1.setText("Total de pedidos prontos hoje: " + rsTotal.getInt(1));
                }
                rsTotal.close();
                psTotal.close();

                String sqlTotalBruto = """
                    SELECT SUM(ip.quantidade * c.preco)
                    FROM pedidos p
                    JOIN itens_pedido ip ON p.id = ip.pedido_id
                    JOIN cardapio c ON ip.cardapio_id = c.id
                    WHERE DATE(p.data_pedido) = CURDATE()
                      AND p.status = 'PRONTO'
                    """;
                PreparedStatement psBruto = conn.prepareStatement(sqlTotalBruto);
                ResultSet rsBruto = psBruto.executeQuery();
                if (rsBruto.next()) {
                    BigDecimal totalBruto = rsBruto.getBigDecimal(1);
                    lblInfo2.setText(String.format("Total bruto de hoje: R$ %.2f", (totalBruto != null ? totalBruto : BigDecimal.ZERO)));
                }
                rsBruto.close();
                psBruto.close();

                String sqlPedidos = """
                    SELECT p.id_custom, p.cliente_nome, p.status, p.data_pedido
                    FROM pedidos p
                    WHERE DATE(p.data_pedido) = CURDATE()
                      AND p.status = 'PRONTO'
                    ORDER BY p.data_pedido DESC
                    """;
                PreparedStatement psPedidos = conn.prepareStatement(sqlPedidos);
                ResultSet rsPedidos = psPedidos.executeQuery();
                modeloTabela.setRowCount(0);
                while (rsPedidos.next()) {
                    modeloTabela.addRow(new Object[]{
                            rsPedidos.getString("id_custom"),
                            rsPedidos.getString("cliente_nome"),
                            rsPedidos.getString("status"),
                            rsPedidos.getTimestamp("data_pedido").toLocalDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                    });
                }
                rsPedidos.close();
                psPedidos.close();
            }
            else if (tipoUsuario.equals("financeiro")) {
                String sqlEntradas = """
                    SELECT p.id_custom, p.cliente_nome, 
                        SUM(ip.quantidade * c.preco) AS total_pedido,
                        p.data_pedido
                    FROM pedidos p
                    JOIN itens_pedido ip ON p.id = ip.pedido_id
                    JOIN cardapio c ON ip.cardapio_id = c.id
                    WHERE p.status = 'PRONTO'
                    GROUP BY p.id, p.cliente_nome, p.data_pedido
                    ORDER BY p.data_pedido DESC
                    """;
                PreparedStatement psE = conn.prepareStatement(sqlEntradas);
                ResultSet rsE = psE.executeQuery();
                modeloEntradas.setRowCount(0);
                BigDecimal totalEntradas = BigDecimal.ZERO;
                while (rsE.next()) {
                    BigDecimal valor = rsE.getBigDecimal("total_pedido");
                    totalEntradas = totalEntradas.add(valor != null ? valor : BigDecimal.ZERO);
                    modeloEntradas.addRow(new Object[]{
                            rsE.getString("id_custom"),
                            rsE.getString("cliente_nome"),
                            String.format("R$ %.2f", valor),
                            rsE.getTimestamp("data_pedido").toLocalDateTime().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                    });
                }
                lblEntradasTotal.setText("Total de Entradas (Pedidos Prontos): R$ " + String.format("%.2f", totalEntradas));
                rsE.close();
                psE.close();

                String sqlSaidas = """
                    SELECT id, descricao, valor, data_pagamento
                    FROM contas
                    WHERE tipo = 'PAGAR'
                      AND status = 'PAGO'
                    ORDER BY data_pagamento DESC
                    """;
                PreparedStatement psS = conn.prepareStatement(sqlSaidas);
                ResultSet rsS = psS.executeQuery();
                modeloSaidas.setRowCount(0);
                BigDecimal totalSaidas = BigDecimal.ZERO;
                while (rsS.next()) {
                    BigDecimal valor = rsS.getBigDecimal("valor");
                    totalSaidas = totalSaidas.add(valor != null ? valor : BigDecimal.ZERO);
                    modeloSaidas.addRow(new Object[]{
                            rsS.getInt("id"),
                            rsS.getString("descricao"),
                            String.format("R$ %.2f", valor),
                            rsS.getDate("data_pagamento") != null
                                ? rsS.getDate("data_pagamento").toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                                : "-"
                    });
                }
                lblSaidasTotal.setText("Total de Saídas Pagas: R$ " + String.format("%.2f", totalSaidas));
                rsS.close();
                psS.close();
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar dados: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Home("financeiro", "Financeiro"));
    }
}

