package classes;

public class ItemPedido {
    private int id;
    private int pedidoId;
    private int cardapioId;
    private double quantidade;

    public ItemPedido() {}

    public ItemPedido(int id, int pedidoId, int cardapioId, double quantidade) {
        this.id = id;
        this.pedidoId = pedidoId;
        this.cardapioId = cardapioId;
        this.quantidade = quantidade;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPedidoId() { return pedidoId; }
    public void setPedidoId(int pedidoId) { this.pedidoId = pedidoId; }

    public int getCardapioId() { return cardapioId; }
    public void setCardapioId(int cardapioId) { this.cardapioId = cardapioId; }

    public double getQuantidade() { return quantidade; }
    public void setQuantidade(double quantidade) { this.quantidade = quantidade; }
}
