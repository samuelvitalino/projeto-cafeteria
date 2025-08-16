package telas;

import classes.Pedido;
import classes.ItemPedido;
import conexao.Conexao;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.*;
import java.text.SimpleDateFormat;

public class NovoPedido extends JFrame {

    private JPanel painelProdutos;
    private JTextField txtCliente;
    private JTextField txtPesquisa;
    private Map<Integer, Double> carrinho = new HashMap<>();

    public NovoPedido() {
        setTitle("Novo Pedido");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10,10));

        JPanel topo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topo.add(new JLabel("Cliente:"));
        txtCliente = new JTextField(20);
        topo.add(txtCliente);

        JButton btnFinalizar = new JButton("Finalizar Pedido");
        btnFinalizar.addActionListener(e -> finalizarPedido());
        topo.add(btnFinalizar);

        txtPesquisa = new JTextField(20);
        txtPesquisa.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                carregarCardapio(txtPesquisa.getText());
            }
        });
        topo.add(new JLabel("Pesquisar:"));
        topo.add(txtPesquisa);

        add(topo, BorderLayout.NORTH);

        painelProdutos = new JPanel();
        painelProdutos.setLayout(new GridLayout(0,3,10,10));
        JScrollPane scroll = new JScrollPane(painelProdutos);
        add(scroll, BorderLayout.CENTER);

        carregarCardapio("");
        setVisible(true);
    }

    private void carregarCardapio(String filtro) {
        painelProdutos.removeAll();

        try (Connection conn = Conexao.conectar()) {
            String sql = "SELECT * FROM cardapio";
            if (filtro != null && !filtro.isEmpty()) {
                sql += " WHERE nome LIKE ?";
            }
            PreparedStatement stmt = conn.prepareStatement(sql);
            if (filtro != null && !filtro.isEmpty()) {
                stmt.setString(1, "%" + filtro + "%");
            }
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                JPanel card = new JPanel();
                card.setLayout(new BorderLayout(5,5));
                card.setBorder(BorderFactory.createLineBorder(Color.GRAY,1));

                byte[] imgBytes = rs.getBytes("imagem");
                JLabel lblImagem = new JLabel();
                lblImagem.setHorizontalAlignment(SwingConstants.CENTER);
                if (imgBytes != null && imgBytes.length > 0) {
                    ImageIcon icon = new ImageIcon(imgBytes);
                    Image scaled = icon.getImage().getScaledInstance(150,150, Image.SCALE_SMOOTH);
                    lblImagem.setIcon(new ImageIcon(scaled));
                } else {
                    lblImagem.setText("Sem Imagem");
                }
                card.add(lblImagem, BorderLayout.NORTH);

                String nome = rs.getString("nome");
                double preco = rs.getDouble("preco");
                JLabel lblNome = new JLabel("<html><b>" + nome + "</b><br>R$ " + preco + "</html>");
                lblNome.setHorizontalAlignment(SwingConstants.CENTER);
                card.add(lblNome, BorderLayout.CENTER);

                JPanel painelQtd = new JPanel(new FlowLayout());
                JButton btnMenos = new JButton("-");
                JButton btnMais = new JButton("+");
                JLabel lblQtd = new JLabel("0");

                int idCardapio = rs.getInt("id");

                btnMais.addActionListener(e -> {
                    double qtdAtual = carrinho.getOrDefault(idCardapio, 0.0);
                    carrinho.put(idCardapio, qtdAtual + 1);
                    lblQtd.setText(String.valueOf((int)(qtdAtual + 1)));
                });
                btnMenos.addActionListener(e -> {
                    double qtdAtual = carrinho.getOrDefault(idCardapio, 0.0);
                    if (qtdAtual > 0) {
                        carrinho.put(idCardapio, qtdAtual - 1);
                        lblQtd.setText(String.valueOf((int)(qtdAtual - 1)));
                    }
                });

                painelQtd.add(btnMenos);
                painelQtd.add(lblQtd);
                painelQtd.add(btnMais);
                card.add(painelQtd, BorderLayout.SOUTH);

                painelProdutos.add(card);
            }

        } catch(Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao carregar cardápio: " + ex.getMessage());
        }

        painelProdutos.revalidate();
        painelProdutos.repaint();
    }

    private void finalizarPedido() {
        String cliente = txtCliente.getText();
        if (cliente.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Informe o nome do cliente!");
            return;
        }
        if (carrinho.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Carrinho vazio!");
            return;
        }

        try (Connection conn = Conexao.conectar()) {
            String idCustom = gerarCodigoPedido(conn);

            String sql = "INSERT INTO pedidos (id_custom, cliente_nome) VALUES (?,?)";
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, idCustom);
            stmt.setString(2, cliente);
            stmt.executeUpdate();

            ResultSet rs = stmt.getGeneratedKeys();
            rs.next();
            int idPedido = rs.getInt(1);

            for (Map.Entry<Integer,Double> entry : carrinho.entrySet()) {
                if (entry.getValue() > 0) {
                    sql = "INSERT INTO itens_pedido (pedido_id, cardapio_id, quantidade) VALUES (?,?,?)";
                    PreparedStatement stmtItem = conn.prepareStatement(sql);
                    stmtItem.setInt(1, idPedido);
                    stmtItem.setInt(2, entry.getKey());
                    stmtItem.setDouble(3, entry.getValue());
                    stmtItem.executeUpdate();
                }
            }

            mostrarCarrinho(idPedido, cliente);

        } catch(Exception ex) {
            JOptionPane.showMessageDialog(this, "Erro ao salvar pedido: " + ex.getMessage());
        }
    }

    private void mostrarCarrinho(int idPedido, String cliente) {
        JDialog carrinhoDialog = new JDialog(this, "Carrinho e Pagamento", true);
        carrinhoDialog.setSize(600, 400);
        carrinhoDialog.setLocationRelativeTo(this);
        carrinhoDialog.setLayout(new BorderLayout(10,10));

        JTextArea resumo = new JTextArea();
        resumo.setEditable(false);

        try (Connection conn = Conexao.conectar()) {
            String sql = """
                    SELECT c.nome, i.quantidade, c.preco
                    FROM itens_pedido i
                    JOIN cardapio c ON i.cardapio_id = c.id
                    WHERE i.pedido_id=?
                    """;
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, idPedido);
            ResultSet rs = stmt.executeQuery();
            double total = 0;
            StringBuilder sb = new StringBuilder();
            while(rs.next()) {
                String nome = rs.getString("nome");
                double qtd = rs.getDouble("quantidade");
                double preco = rs.getDouble("preco");
                double subtotal = qtd * preco;
                sb.append(nome).append(" x").append((int)qtd).append(" = R$ ").append(subtotal).append("\n");
                total += subtotal;
            }
            sb.append("\nTOTAL: R$ ").append(total);
            resumo.setText(sb.toString());
        } catch(Exception ex) {
            resumo.setText("Erro ao carregar resumo: " + ex.getMessage());
        }

        carrinhoDialog.add(new JScrollPane(resumo), BorderLayout.CENTER);

        JPanel pagamento = new JPanel();
        pagamento.setLayout(new FlowLayout());
        pagamento.add(new JLabel("Forma de Pagamento:"));
        JComboBox<String> cmbPagamento = new JComboBox<>(new String[]{"Dinheiro","Pix","Cartão"});
        pagamento.add(cmbPagamento);

        JButton btnConfirmar = new JButton("Confirmar Pedido");
        btnConfirmar.addActionListener(e -> {
            JOptionPane.showMessageDialog(carrinhoDialog, "Pedido confirmado!\nPagamento: " + cmbPagamento.getSelectedItem());
            carrinhoDialog.dispose();
            dispose();
        });
        pagamento.add(btnConfirmar);

        carrinhoDialog.add(pagamento, BorderLayout.SOUTH);
        carrinhoDialog.setVisible(true);
    }

    private String gerarCodigoPedido(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*) FROM pedidos";
        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();
        rs.next();
        int qtd = rs.getInt(1) + 1;
        return String.format("PED%05d", qtd);
    }

}
